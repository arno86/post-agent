package dev.arno.linkedin.postagent.dto;

import jakarta.validation.constraints.Positive;

public record Constraints(
        @Positive Integer maxChars,
        Boolean useEmojis,
        Boolean useLineBreaks,
        @Positive Integer paragraphsMax,
        @Positive Integer hashtagsMax
) {
    public Constraints {
        // defaults
        maxChars = (maxChars == null) ? 2200 : maxChars;
        useEmojis = (useEmojis == null) ? Boolean.TRUE : useEmojis;
        useLineBreaks = (useLineBreaks == null) ? Boolean.TRUE : useLineBreaks;
        paragraphsMax = (paragraphsMax == null) ? 8 : paragraphsMax;
        hashtagsMax = (hashtagsMax == null) ? 5 : hashtagsMax;
    }
}