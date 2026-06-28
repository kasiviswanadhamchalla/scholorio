package com.scholario.recommendation.client;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Component
public class CourseServiceClient {

    private final WebClient webClient;

    public CourseServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.baseUrl("http://course-service").build();
    }

    public CourseDto getCourseById(Long id) {
        return null;
    }

    public long countByFacultyId(Long facultyId) {
        return 0L;
    }

    public List<CourseMaterialDto> getMaterialsByCourse(Long courseId) {
        return java.util.Collections.emptyList();
    }
}
