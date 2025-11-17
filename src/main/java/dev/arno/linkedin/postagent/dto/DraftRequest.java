package dev.arno.linkedin.postagent.dto;

public record DraftRequest(
        String topic,     // e.g. "automation", "project management", "devops"
        String angle,     // e.g. "flaky tests", "stakeholder communication"
        String tone       // e.g. "practical", "inspirational"
) {}