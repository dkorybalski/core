package pl.edu.amu.wmi.client;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Configuration
public class RestTemplateConfiguration {
    private final RestTemplateBuilder restTemplateBuilder;

    @Bean
    RestTemplate getRestTemplate() {
        return restTemplateBuilder.build();
    }
}
