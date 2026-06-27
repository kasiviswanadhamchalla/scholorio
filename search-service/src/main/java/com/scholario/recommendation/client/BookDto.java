package com.scholario.recommendation.client;

public record BookDto(
        Long id,
        String title,
        String isbn,
        Long facultyId,
        String stateName
) {}
