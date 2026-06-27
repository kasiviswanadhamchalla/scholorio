package com.scholario.course.dto;

import com.scholario.course.model.Course;

public record CourseResponse(
        Long id,
        String courseCode,
        String title,
        String description,
        Long facultyId,
        String createdAt,
        String updatedAt
) {
    public static CourseResponse fromEntity(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                course.getDescription(),
                course.getFacultyId(),
                course.getCreatedAt() != null ? course.getCreatedAt().toString() : null,
                course.getUpdatedAt() != null ? course.getUpdatedAt().toString() : null
        );
    }
}
