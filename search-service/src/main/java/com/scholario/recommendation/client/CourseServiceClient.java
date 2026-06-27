package com.scholario.recommendation.client;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Component
public class CourseServiceClient {

    private final HttpGraphQlClient graphQlClient;

    public CourseServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.graphQlClient = HttpGraphQlClient.builder(loadBalancedWebClientBuilder.build())
                .url("http://course-service/graphql")
                .build();
    }

    public CourseDto getCourseById(Long id) {
        String query = """
            query GetCourseById($id: ID!) {
                getCourseById(id: $id) {
                    id
                    courseCode
                    title
                    description
                    facultyId
                }
            }
            """;
        return graphQlClient.document(query)
                .variable("id", id)
                .retrieve("getCourseById")
                .toEntity(CourseDto.class)
                .block();
    }

    public long countByFacultyId(Long facultyId) {
        String query = """
            query CountCoursesByFaculty($facultyId: ID!) {
                countCoursesByFaculty(facultyId: $facultyId)
            }
            """;
        return graphQlClient.document(query)
                .variable("facultyId", facultyId)
                .retrieve("countCoursesByFaculty")
                .toEntity(Long.class)
                .block();
    }

    public List<CourseMaterialDto> getMaterialsByCourse(Long courseId) {
        String query = """
            query GetCourseMaterials($courseId: ID!) {
                getCourseMaterials(courseId: $courseId) {
                    id
                    courseId
                    bookId
                    mandatory
                }
            }
            """;
        return graphQlClient.document(query)
                .variable("courseId", courseId)
                .retrieve("getCourseMaterials")
                .toEntityList(CourseMaterialDto.class)
                .block();
    }
}
