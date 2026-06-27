package com.scholario.lending.client;

public record BookDto(
        Long id,
        String title,
        String isbn,
        Long facultyId,
        String stateName
) {}
