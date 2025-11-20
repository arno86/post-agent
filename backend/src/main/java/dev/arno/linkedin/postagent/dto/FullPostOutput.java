package dev.arno.linkedin.postagent.dto;

import java.util.List;

public record FullPostOutput(
        String ideaTitle,
        String outline,
        String draft,
        List<String> hashtags,
        String imagePrompt,
        String finalText,
        int charCount
) { }
