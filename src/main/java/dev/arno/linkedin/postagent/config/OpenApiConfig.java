package dev.arno.linkedin.postagent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("LinkedIn Post Generator (PM/Automation/DevOps)")
                .version("1.0.0")
                .description("Generate ideas → outline → draft → polish → hashtags → image prompts → package"));
    }
}
