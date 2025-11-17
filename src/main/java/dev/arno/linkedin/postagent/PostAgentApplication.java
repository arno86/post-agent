package dev.arno.linkedin.postagent;

import dev.arno.linkedin.postagent.config.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiProperties.class)
public class PostAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostAgentApplication.class, args);
    }
}