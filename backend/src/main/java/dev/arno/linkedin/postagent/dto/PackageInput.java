package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PackageInput(
        @NotBlank String text,
        List<String> hashtags,
        String imagePrompt,
        String constraints
) {}
