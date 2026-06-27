package com.scholario.recommendation.client;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

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

    public long countByFacultyId(Long facultyId) {
        String query = """
            query CountBooksByFaculty($facultyId: ID!) {
                countBooksByFaculty(facultyId: $facultyId)
            }
            """;
        return graphQlClient.document(query)
                .variable("facultyId", facultyId)
                .retrieve("countBooksByFaculty")
                .toEntity(Long.class)
                .block();
    }

    public List<BookDto> getBooksByFaculty(Long facultyId) {
        String query = """
            query GetBooksByFaculty($facultyId: ID!) {
                getBooksByFaculty(facultyId: $facultyId) {
                    id
                    title
                    isbn
                    facultyId
                }
            }
            """;
        return graphQlClient.document(query)
                .variable("facultyId", facultyId)
                .retrieve("getBooksByFaculty")
                .toEntityList(BookDto.class)
                .block();
    }

    public List<BookDto> getAllBooks() {
        String query = """
            query GetAllBooks {
                getAllBooks {
                    id
                    title
                    isbn
                    facultyId
                }
            }
            """;
        return graphQlClient.document(query)
                .retrieve("getAllBooks")
                .toEntityList(BookDto.class)
                .block();
    }
}
