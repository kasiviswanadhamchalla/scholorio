package com.scholario.content.client;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class IdentityServiceClient {

    private final WebClient webClient;

    public IdentityServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.baseUrl("http://identity-service").build();
    }

    public UserDto getUserById(Long id) {
        try {
            return webClient.get()
                    .uri("/api/users/{id}", id)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }
}
