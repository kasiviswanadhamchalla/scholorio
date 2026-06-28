package com.scholario.royalty.client;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BookServiceClient {

    private final WebClient webClient;

    public BookServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.baseUrl("http://book-service").build();
    }

    public Boolean existsById(Long id) {
        try {
            return webClient.get()
                    .uri("/api/{id}/exists", id)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception e) {
            return false;
        }
    }
}
