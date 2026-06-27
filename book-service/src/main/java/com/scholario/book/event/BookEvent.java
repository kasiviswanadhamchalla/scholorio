package com.scholario.book.event;

public record BookEvent(
        String eventType,
        Long bookId,
        String title,
        Long facultyId,
        String description
) {}
