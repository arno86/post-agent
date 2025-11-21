package dev.arno.linkedin.postagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties()
public class PostAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostAgentApplication.class, args);
    }
}