package dev.arno.linkedin.postagent.dto;

import java.util.List;

public record Outline(
        String hook,
        List<String> bullets,
        CTA cta
) {}
