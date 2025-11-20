package dev.arno.linkedin.postagent.dto;

public record DraftInput(
        OutlineOutput outline,
        String brief,
        Topic topic,
        Tone tone,
        String constraints
) {}
