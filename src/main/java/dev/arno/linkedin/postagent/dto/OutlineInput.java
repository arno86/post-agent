package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OutlineInput(
        @NotBlank String ideaId,
        @NotNull PostFormat format,
        List<String> keyPoints,
        AudienceLevel audienceLevel
) {}
