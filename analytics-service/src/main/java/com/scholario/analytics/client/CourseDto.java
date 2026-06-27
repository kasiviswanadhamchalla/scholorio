package com.scholario.analytics.client;

public record CourseDto(
        Long id,
        String courseCode,
        String title,
        String description,
        Long facultyId
) {}
