package dev.arno.linkedin.postagent.llm; // or .llm – match your package

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiClient implements LlmClient{

    private final HttpClient httpClient;
    @Autowired
    private ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    public OpenAiClient(
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.apiKey = apiKey;
        this.model = (model == null || model.isBlank()) ? "gpt-4o-mini" : model;
    }

    @Override
    public String chat(List<LlmMessage> messages) throws IOException, InterruptedException {
        var url = URI.create("https://api.openai.com/v1/chat/completions");
        var body = Map.of(
                "model", model,
                "temperature", 0.7,
                "messages", messages.stream()
                        .map(m -> Map.of("role", m.role(), "content", m.content()))
                        .toList()
        );

        var req = HttpRequest.newBuilder(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        var res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        // 🔍 Debug: log status + raw body
        System.out.println("LLM status = " + res.statusCode());
        System.out.println("LLM raw body = " + res.body());

        if (res.statusCode() / 100 != 2) {
            throw new IOException("LLM error: " + res.statusCode() + " - " + res.body());
        }

        JsonNode root = mapper.readTree(res.body());
        JsonNode choices = root.path("choices");

        if (!choices.isArray() || choices.isEmpty()) {
            throw new IOException("LLM response missing choices: " + res.body());
        }

        JsonNode contentNode = choices.get(0).path("message").path("content");

        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            throw new IOException("LLM response missing content: " + res.body());
        }

        return contentNode.asText().trim();
    }

}
