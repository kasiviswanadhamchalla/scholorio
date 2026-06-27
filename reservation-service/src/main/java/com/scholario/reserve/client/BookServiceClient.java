package com.scholario.reserve.client;

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

    public BookDto getBookById(Long id) {
        String query = """
            query GetBookById($id: ID!) {
                getBookById(id: $id) {
                    id
                    title
                    isbn
                    facultyId
                }
            }
            """;
        return graphQlClient.document(query)
                .variable("id", id)
                .retrieve("getBookById")
                .toEntity(BookDto.class)
                .block();
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
