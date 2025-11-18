package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.NotBlank;

public record ImagePromptsInput(
        @NotBlank String text,
        String style // diagram | minimal_illustration | flat_icon_set | photo | screenshot_mock
) {
    public ImagePromptsInput {
        style = (style == null) ? "minimal_illustration" : style;
    }
}