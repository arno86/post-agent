package dev.arno.linkedin.postagent.llm;

import java.io.IOException;
import java.util.List;

public interface LlmClient {
    String chat(List<LlmMessage> messages) throws IOException, InterruptedException;
}
