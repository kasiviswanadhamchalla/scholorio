package com.scholario.recommendation.client;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Component
public class LendingServiceClient {

    private final HttpGraphQlClient graphQlClient;

    public LendingServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.graphQlClient = HttpGraphQlClient.builder(loadBalancedWebClientBuilder.build())
                .url("http://lending-service/graphql")
                .build();
    }

    public List<IssueRecordDto> getIssuesByUser(Long userId) {
        String query = """
            query GetIssuesByUser($userId: ID!) {
                getIssuesByUser(userId: $userId) {
                    id
                    bookId
                    userId
                    issueDate
                    dueDate
                    returnDate
                }
            }
            """;
        return graphQlClient.document(query)
                .variable("userId", userId)
                .retrieve("getIssuesByUser")
                .toEntityList(IssueRecordDto.class)
                .block();
    }

    public List<BookIssueCountDto> getIssuesByBook() {
        String query = """
            query GetIssuesByBook {
                getIssuesByBook {
                    bookId
                    issueCount
                }
            }
            """;
        return graphQlClient.document(query)
                .retrieve("getIssuesByBook")
                .toEntityList(BookIssueCountDto.class)
                .block();
    }
}
