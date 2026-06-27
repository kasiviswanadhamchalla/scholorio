package com.scholario.recommendation.client;

public record CourseMaterialDto(
        Long id,
        Long courseId,
        Long bookId,
        boolean mandatory,
        String createdAt,
        String updatedAt
) {}
