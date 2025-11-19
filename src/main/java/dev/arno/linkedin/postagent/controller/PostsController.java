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
}
