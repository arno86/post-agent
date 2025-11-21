package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PolishInput(
        @NotBlank String draft,
        @Min(0) @Max(60) Integer tightenByPercent,
        List<String> editRules,
        String platform // "linkedin"
) {
    public PolishInput {
        tightenByPercent = (tightenByPercent == null) ? 15 : tightenByPercent;
        platform = (platform == null) ? "linkedin" : platform;
    }
}