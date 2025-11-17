package dev.arno.linkedin.postagent.controller;

import dev.arno.linkedin.postagent.dto.DraftRequest;
import dev.arno.linkedin.postagent.dto.DraftResponse;
import dev.arno.linkedin.postagent.service.OpenAiClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostsController {

    private final OpenAiClient openAiClient;

    public PostsController(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @PostMapping(path = "/draft", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DraftResponse draft(@RequestBody DraftRequest request) {
        String topic = request.topic() != null ? request.topic() : "automation";
        String angle = request.angle() != null ? request.angle() : "reducing flaky tests in CI pipelines";
        String tone  = request.tone()  != null ? request.tone()  : "practical";

        String draftText = openAiClient.generateLinkedInDraft(topic, angle, tone);
        int length = draftText.length();

        return new DraftResponse(draftText, length);
    }
}

