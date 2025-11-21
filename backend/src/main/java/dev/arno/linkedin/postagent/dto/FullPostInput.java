package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.NotBlank;

public record FullPostInput(
        @NotBlank String topic,          // e.g. "flaky tests in CI"
        @NotBlank String audience,       // e.g. "senior SDETs"
        @NotBlank String goal,           // e.g. "Write a practical LinkedIn tip post"
        String tone,                     // e.g. "practical", "storytelling"
        String constraints,              // e.g. "under 1000 chars, no emojis"
        Integer maxHashtags,             // optional, default in controller
        String style                     // optional, e.g. "clean, engineering-focused"
) { }
