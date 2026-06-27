package com.scholario.lending.client;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class IdentityServiceClient {

    private final HttpGraphQlClient graphQlClient;

    public IdentityServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.graphQlClient = HttpGraphQlClient.builder(loadBalancedWebClientBuilder.build())
                .url("http://identity-service/graphql")
                .build();
    }

    public UserDto getUserByUsername(String username) {
        String query = """
            query GetUserByUsername($username: String!) {
                getUserByUsername(username: $username) {
                    id
                    username
                    email
                    fullName
                    roles
                }
            }
            """;
        return graphQlClient.document(query)
                .variable("username", username)
                .retrieve("getUserByUsername")
                .toEntity(UserDto.class)
                .block();
    }

    public UserDto getUserById(Long id) {
        String query = """
            query GetUserById($id: ID!) {
                getUserById(id: $id) {
                    id
                    username
                    email
                    fullName
                    roles
                }
            }
            """;
        return graphQlClient.document(query)
                .variable("id", id)
                .retrieve("getUserById")
                .toEntity(UserDto.class)
                .block();
    }
}
