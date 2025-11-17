package dev.arno.linkedin.postagent.service;


import dev.arno.linkedin.postagent.config.OpenAiProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class Demo {
    private final OpenAiProperties props;

    public Demo(OpenAiProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        System.out.println(props.getApiKey());
    }
}
