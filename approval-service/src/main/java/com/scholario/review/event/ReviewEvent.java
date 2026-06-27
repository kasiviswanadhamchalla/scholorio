package com.scholario.review.event;

public record ReviewEvent(
        String eventType,
        Long reviewRecordId,
        Long bookId,
        Long reviewerId,
        String status,
        String feedback
) {}
