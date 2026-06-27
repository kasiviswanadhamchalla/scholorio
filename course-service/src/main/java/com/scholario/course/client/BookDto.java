package com.scholario.course.client;

public record BookDto(
        Long id,
        String title,
        String isbn,
        Long facultyId,
        String description,
        Integer versionNumber,
        String stateName,
        Long parentBookId,
        String createdAt,
        String updatedAt
) {}
