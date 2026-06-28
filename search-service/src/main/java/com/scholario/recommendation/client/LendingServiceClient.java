package com.scholario.recommendation.client;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Component
public class LendingServiceClient {

    private final WebClient webClient;

    public LendingServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.baseUrl("http://lending-service").build();
    }

    public List<IssueRecordDto> getIssuesByUser(Long userId) {
        return java.util.Collections.emptyList();
    }

    public List<BookIssueCountDto> getIssuesByBook() {
        return java.util.Collections.emptyList();
    }
}
