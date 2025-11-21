package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record IdeasInput(
        @NotNull Topic topic,
        AudienceLevel audienceLevel,
        @Min(1) @Max(12) Integer nIdeas,
        List<String> seedKeywords,
        List<String> avoid
) {
    public IdeasInput {
        nIdeas = (nIdeas == null) ? 8 : nIdeas;
    }
}