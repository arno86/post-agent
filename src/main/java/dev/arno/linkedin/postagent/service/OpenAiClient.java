package dev.arno.linkedin.postagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.arno.linkedin.postagent.config.OpenAiProperties;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiClient {

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties props;

    public OpenAiClient(ObjectMapper objectMapper, OpenAiProperties props) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
        this.props = props;
    }

    public String generateLinkedInDraft(String topic, String angle, String tone) {
        try {
            String systemPrompt = """
                    You are a professional LinkedIn ghostwriter for senior engineers and engineering leaders.
                    You write concise, high-signal posts about project management, automation, and DevOps.
                    Constraints:
                    - Max ~1200 characters
                    - First line must be a hook (no emoji at the start)
                    - Use short paragraphs and line breaks
                    - 0–2 emojis max, only if they really help
                    - No links in the body, you can mention "link in first comment" if needed
                    - Avoid generic fluff, focus on specific, practical insights
                    """;

            String userPrompt = """
                    Topic: %s
                    Angle: %s
                    Tone: %s

                    Write a LinkedIn post targeting senior SDETs / SDEs / tech leads.
                    Make it opinionated but grounded, with at least one concrete example or tactic.
                    Do NOT include hashtags – we'll add them later.
                    """.formatted(topic, angle, tone);

            Map<String, Object> body = Map.of(
                    "model", props.getModel(),
                    "temperature", 0.7,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_CHAT_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + props.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new RuntimeException("OpenAI API error: " + response.statusCode() + " - " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            String content = contentNode.asText();
            if (content == null || content.isBlank()) {
                throw new RuntimeException("Empty response from OpenAI");
            }
            return content.trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API", e);
        }
    }
}
