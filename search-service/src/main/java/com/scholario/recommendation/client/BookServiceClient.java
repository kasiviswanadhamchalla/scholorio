package com.scholario.recommendation.client;

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

    public long countByFacultyId(Long facultyId) {
        try {
            List<BookDto> all = webClient.get()
                    .uri("/api/")
                    .retrieve()
                    .bodyToFlux(BookDto.class)
                    .collectList()
                    .block();
            if (all == null) return 0L;
            return all.stream().filter(b -> facultyId.equals(b.facultyId())).count();
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<BookDto> getBooksByFaculty(Long facultyId) {
        try {
            List<BookDto> all = webClient.get()
                    .uri("/api/")
                    .retrieve()
                    .bodyToFlux(BookDto.class)
                    .collectList()
                    .block();
            if (all == null) return java.util.Collections.emptyList();
            return all.stream().filter(b -> facultyId.equals(b.facultyId())).toList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public List<BookDto> getAllBooks() {
        try {
            return webClient.get()
                    .uri("/api/")
                    .retrieve()
                    .bodyToFlux(BookDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
