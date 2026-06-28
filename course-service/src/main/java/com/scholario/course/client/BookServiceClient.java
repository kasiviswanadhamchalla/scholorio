package com.scholario.course.client;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Component
public class BookServiceClient {

    private final WebClient webClient;

    public BookServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.baseUrl("http://book-service").build();
    }

    public BookDto getBookById(Long id) {
        try {
            return webClient.get()
                    .uri("/api/{id}", id)
                    .retrieve()
                    .bodyToMono(BookDto.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
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

    public List<BookDto> getBooksByIds(List<Long> ids) {
        try {
            List<BookDto> all = webClient.get()
                    .uri("/api/")
                    .retrieve()
                    .bodyToFlux(BookDto.class)
                    .collectList()
                    .block();
            if (all == null) return java.util.Collections.emptyList();
            return all.stream().filter(b -> ids.contains(b.id())).toList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
