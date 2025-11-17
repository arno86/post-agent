package dev.arno.linkedin.postagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    /**
     * OpenAI API key, injected from application.yml / env.
     */
    private String apiKey;

    /**
     * Default model to use (e.g., gpt-4o-mini).
     */
    private String model = "gpt-4o-mini";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
