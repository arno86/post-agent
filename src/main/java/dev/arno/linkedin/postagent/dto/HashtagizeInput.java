package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record HashtagizeInput(
        @NotBlank String text,
        @Min(1) @Max(5) Integer maxTags,
        String strategy // broad | balanced | niche
) {
    public HashtagizeInput {
        maxTags = (maxTags == null) ? 5 : maxTags;
        strategy = (strategy == null) ? "balanced" : strategy;
    }
}
