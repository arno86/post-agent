package dev.arno.linkedin.postagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.arno.linkedin.postagent.dto.*;
import dev.arno.linkedin.postagent.error.ApiException;
import dev.arno.linkedin.postagent.llm.LlmClient;
import dev.arno.linkedin.postagent.llm.LlmMessage;
import dev.arno.linkedin.postagent.service.PackagingService;
import dev.arno.linkedin.postagent.service.Prompts;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PostsController {

    private final LlmClient llm;
    private final ObjectMapper mapper;

    public PostsController(LlmClient llm, ObjectMapper mapper) {
        this.llm = llm;
        this.mapper = mapper;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true);
    }

    @PostMapping("/posts/ideas")
    public IdeasOutput ideas(@Valid @RequestBody IdeasInput input) throws Exception {
        String row = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user(Prompts.ideasPrompt(input))
        ));
        String out = unwrapJsonCodeFence(row);

        IdeaItem[] items = mapper.readValue(out, IdeaItem[].class);
        return new IdeasOutput(List.of(items));
    }

    @PostMapping("/posts/outline")
    public OutlineOutput outline(@Valid @RequestBody OutlineInput input) throws Exception {
        String content = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user(Prompts.outlinePrompt(
                        input.ideaId(),
                        input.format(),
                        input.keyPoints(),
                        input.audienceLevel()
                ))
        ));
        var node = mapper.readTree(content).get("outline");
        if (node == null) throw new ApiException(500, "LLM did not return 'outline'");
        return mapper.treeToValue(mapper.createObjectNode().set("outline", node), OutlineOutput.class);
    }

    @PostMapping("/posts/draft")
    public DraftOutput draft(@Valid @RequestBody DraftInput input) throws Exception {
        String content = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user(Prompts.draftPrompt(
                        input.outline(),
                        input.brief(),
                        input.topic(),
                        input.tone(),
                        input.constraints()
                ))
        ));
        return new DraftOutput(content, PackagingService.countChars(content));
    }

    @PostMapping("/posts/polish")
    public PolishOutput polish(@Valid @RequestBody PolishInput input) throws Exception {
        String content = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user(Prompts.polishPrompt(
                        input.draft(),
                        input.tightenByPercent(),
                        input.editRules()
                ))
        ));
        return mapper.readValue(content, PolishOutput.class);
    }

    @PostMapping("/posts/hashtagize")
    public HashtagizeOutput hashtags(@Valid @RequestBody HashtagizeInput input) throws Exception {
        String content = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user(Prompts.hashtagsPrompt(
                        input.text(),
                        input.maxTags(),
                        input.strategy()
                ))
        ));
        return mapper.readValue(content, HashtagizeOutput.class);
    }

    @PostMapping("/posts/image-prompts")
    public ImagePromptsOutput imagePrompts(@Valid @RequestBody ImagePromptsInput input) throws Exception {
        String content = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user(Prompts.imagePrompt(
                        input.text(),
                        input.style()
                ))
        ));
        return mapper.readValue(content, ImagePromptsOutput.class);
    }

    @PostMapping("/posts/package")
    public PackageOutput pack(@Valid @RequestBody PackageInput input) throws Exception {
        String content = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user(Prompts.packagePrompt(
                        input.text(),
                        input.hashtags(),
                        input.imagePrompt(),
                        input.constraints()
                ))
        ));
        return mapper.readValue(content, PackageOutput.class);
    }

    private String unwrapJsonCodeFence(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.startsWith("```")) {
            // Skip the first line (``` or ```json)
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    @PostMapping("/posts/full")
    public FullPostOutput full(@Valid @RequestBody FullPostInput input) throws Exception {
        // sensible defaults
        String tone = (input.tone() == null || input.tone().isBlank())
                ? "practical"
                : input.tone();
        int maxTags = (input.maxHashtags() == null || input.maxHashtags() <= 0)
                ? 5
                : input.maxHashtags();

        // 1) Get a few ideas and pick the first
        String ideasJson = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user("""
                    I need LinkedIn post ideas.

                    Topic: %s
                    Audience: %s
                    Goal: %s

                    Return a JSON array of short title strings.
                    """.formatted(input.topic(), input.audience(), input.goal()))
        ));
        // Example expected: ["Title 1", "Title 2", ...]
        var ideasNode = mapper.readTree(ideasJson);
        String ideaTitle = ideasNode.isArray() && ideasNode.size() > 0
                ? ideasNode.get(0).asText()
                : input.goal(); // fallback

        // 2) Outline
        String outlineJson = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user("""
                    Create a concise outline for a LinkedIn post with this title:

                    "%s"

                    Audience: %s
                    Tone: %s

                    Return JSON: { "outline": "..." }
                    """.formatted(ideaTitle, input.audience(), tone))
        ));
        String outline = mapper.readTree(outlineJson).path("outline").asText();

        // 3) Draft
        String draft = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user("""
                    Write a LinkedIn post using this outline:

                    %s

                    Audience: %s
                    Tone: %s
                    Constraints: %s

                    Return ONLY the post text, no JSON.
                    """.formatted(outline, input.audience(), tone,
                        input.constraints() == null ? "" : input.constraints()))
        ));

        // 4) Hashtags
        String hashtagsJson = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user("""
                    Generate up to %d high-quality hashtags for this LinkedIn post:

                    %s

                    Return JSON: { "hashtags": ["#tag1", "#tag2", ...] }
                    """.formatted(maxTags, draft))
        ));
        var tagsNode = mapper.readTree(hashtagsJson).path("hashtags");
        java.util.List<String> hashtags = new java.util.ArrayList<>();
        if (tagsNode.isArray()) {
            tagsNode.forEach(n -> hashtags.add(n.asText()));
        }

        // 5) Image prompt
        String imageJson = llm.chat(List.of(
                LlmMessage.system(Prompts.SYSTEM),
                LlmMessage.user("""
                    Create a concise prompt for an illustration or header image
                    that would go well with this LinkedIn post:

                    %s

                    Return JSON: { "imagePrompt": "..." }
                    """.formatted(draft))
        ));
        String imagePrompt = mapper.readTree(imageJson).path("imagePrompt").asText();

        // 6) Final packaged text (post + hashtags)
        String tagsLine = hashtags.isEmpty()
                ? ""
                : "\n\n" + String.join(" ", hashtags);

        String finalText = draft.trim() + tagsLine;
        int charCount = dev.arno.linkedin.postagent.service.PackagingService.countChars(finalText);

        return new FullPostOutput(
                ideaTitle,
                outline,
                draft,
                hashtags,
                imagePrompt,
                finalText,
                charCount
        );
    }

}
