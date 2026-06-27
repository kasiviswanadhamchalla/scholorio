package com.scholario.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "course-service", path = "/internal/courses")
public interface CourseServiceClient {

    @GetMapping("/{id}")
    CourseDto getCourseById(@PathVariable("id") Long id);

    @GetMapping("/count/by-faculty/{facultyId}")
    long countByFacultyId(@PathVariable("facultyId") Long facultyId);

    @GetMapping("/materials/by-course/{courseId}")
    List<CourseMaterialDto> getMaterialsByCourse(@PathVariable("courseId") Long courseId);
}
