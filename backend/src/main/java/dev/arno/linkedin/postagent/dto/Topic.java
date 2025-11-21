package dev.arno.linkedin.postagent.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Topic {

    PROJECT_MANAGEMENT("project_management"),
    DEVOPS("devops"),
    AUTOMATION("automation");

    private final String json;

    Topic(String json) {
        this.json = json;
    }

    @JsonValue
    public String getJson() {
        return json;
    }

    @JsonCreator
    public static Topic fromJson(String value) {
        if (value == null) {
            return PROJECT_MANAGEMENT;
        }

        String v = value.toLowerCase(Locale.ROOT).trim();

        // Very simple heuristic mapping:
        if (v.contains("project")) {
            return PROJECT_MANAGEMENT;
        }
        if (v.contains("devops")) {
            return DEVOPS;
        }
        if (v.contains("automation") || v.contains("test") || v.contains("qa")) {
            return AUTOMATION;
        }

        // Fallback: default to project management (or throw, if you prefer)
        return PROJECT_MANAGEMENT;
        // or: throw new IllegalArgumentException("Unknown topic: " + value);
    }
}
