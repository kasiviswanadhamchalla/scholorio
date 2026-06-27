package com.scholario.lending.event;

public record LendingEvent(
        String eventType,
        Long issueId,
        Long bookId,
        Long userId,
        String status
) {}
