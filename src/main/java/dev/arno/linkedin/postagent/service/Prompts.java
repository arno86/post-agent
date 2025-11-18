package dev.arno.linkedin.postagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.arno.linkedin.postagent.dto.*;

import java.util.List;

public class Prompts {
    public static final String SYSTEM = """
  You write high-signal LinkedIn posts for software audiences about project management, test automation, and DevOps.
  Constraints: ≤2200 chars (prefer ≤1200), 3–5 concise hashtags, line breaks, minimal emojis unless tone=friendly,
  first-line hook, strong verbs, no body links (use "link in first comment"), avoid clichés.
  Allowed formats: tip_list, checklist, how_to, lesson_learned, myth_vs_fact, mini_case_study, opinion.
  Audience: beginner, intermediate, advanced, executive. Always return clean, publish-ready text.
  """;

    public static String ideasPrompt(IdeasInput in){
        String seeds = (in.seedKeywords()!=null && !in.seedKeywords().isEmpty()) ? "Seed keywords: " + String.join(", ", in.seedKeywords()) : "";
        String avoid = (in.avoid()!=null && !in.avoid().isEmpty()) ? "Avoid: " + String.join(", ", in.avoid()) : "";
        String level = (in.audienceLevel()==null) ? "intermediate" : in.audienceLevel().name();
        return """
      Generate %d concise post ideas for topic "%s" for %s audience.
      Each idea returns a title and a 1-line hook.
      %s %s
      Return as a JSON array of objects: [{ "id": "slug", "title": "...", "hook": "..." }].
      Only return JSON.
      """.formatted(in.nIdeas(), in.topic().name(), level, seeds, avoid);
    }

    public static String outlinePrompt(String ideaTitle, PostFormat format, List<String> keyPoints, AudienceLevel level){
        String keys = (keyPoints!=null && !keyPoints.isEmpty()) ? "Key points: " + String.join(" | ", keyPoints) : "";
        return """
      Outline a LinkedIn post based on idea "%s" in format %s.
      %s
      Return JSON: { "outline": { "hook": "...", "bullets": ["..."], "cta": "ask_opinion|invite_dm|link_in_first_comment|follow_me|read_more_thread" } }
      """.formatted(ideaTitle, format.name(), keys);
    }

    public static String draftPrompt(OutlineOutput outline, String brief, Topic topic, Tone tone, Constraints c){
        String base = (outline!=null) ? ("Use this outline:\n" + toJson(outline)) : ("Brief:\n" + brief);
        return """
      Write a LinkedIn post. Topic: %s. Tone: %s.
      Constraints: %s
      %s
      Return only the post text (no JSON).
      """.formatted(
                topic==null ? "automation" : topic.name(),
                tone==null ? "practical" : tone.name(),
                toJson(c),
                base
        );
    }

    public static String polishPrompt(String draft, int tighten, List<String> rules){
        String r = (rules==null || rules.isEmpty())
                ? "front-load value, remove filler, shorten sentences, active voice"
                : String.join(", ", rules);
        return """
      Polish this LinkedIn post. Tighten by ~%d%% while preserving meaning.
      Rules: %s

      Return JSON:
      { "polished": "...", "charCount": <int>, "diffs": [{ "from": "...", "to": "...", "rationale": "..." }] }

      Draft:
      \"%s\"
      """.formatted(tighten, r, draft);
    }

    public static String hashtagsPrompt(String text, int maxTags, String strategy){
        return """
      Suggest up to %d concise hashtags for the post below.
      Strategy: %s (mix broad + niche; max 5).
      Return JSON: { "hashtags": ["#..."], "rationale": "..." }

      Post:
      \"%s\"
      """.formatted(maxTags, strategy, text);
    }

    public static String imagePrompt(String text, String style){
        return """
      Propose 3-6 %s image prompt ideas to pair with the LinkedIn post.
      Return JSON: { "prompts": ["..."] }

      Post:
      \"%s\"
      """.formatted(style, text);
    }

    public static String packagePrompt(String text, List<String> hashtags, String imagePrompt, Constraints c){
        return """
      Package the final LinkedIn post. Enforce constraints: %s
      Ensure final text ≤ constraints.maxChars (default 2200), uses line breaks, and includes hashtags at the end (3–5 total).
      Return JSON: {
        "finalText": "...",
        "finalCharCount": <int>,
        "hashtags": ["#..."],
        "imagePrompt": "...",
        "warnings": []
      }

      Text:
      \"%s\"

      Hashtags: %s
      Image prompt: %s
      """.formatted(toJson(c), text, (hashtags==null? "" : String.join(" ", hashtags)), imagePrompt==null? "" : imagePrompt);
    }

    private static String toJson(Object o){
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
