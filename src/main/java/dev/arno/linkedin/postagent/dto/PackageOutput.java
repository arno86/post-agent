package dev.arno.linkedin.postagent.dto;

import java.util.List;

public record PackageOutput(
        String finalText,
        int finalCharCount,
        List<String> hashtags,
        String imagePrompt,
        List<String> warnings
) {}