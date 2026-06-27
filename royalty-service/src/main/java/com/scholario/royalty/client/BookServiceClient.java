package com.scholario.royalty.client;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BookServiceClient {

    private final HttpGraphQlClient graphQlClient;

    public BookServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.graphQlClient = HttpGraphQlClient.builder(loadBalancedWebClientBuilder.build())
                .url("http://book-service/graphql")
                .build();
    }

    public Boolean existsById(Long id) {
        String query = """
            query ExistsBook($id: ID!) {
                existsBook(id: $id)
            }
            """;
        return graphQlClient.document(query)
                .variable("id", id)
                .retrieve("existsBook")
                .toEntity(Boolean.class)
                .block();
    }
}
