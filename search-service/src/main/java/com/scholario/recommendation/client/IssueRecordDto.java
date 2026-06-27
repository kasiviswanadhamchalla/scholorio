package com.scholario.recommendation.client;

public record IssueRecordDto(
        Long id,
        Long userId,
        Long bookId,
        String issueDate,
        String dueDate,
        String returnDate,
        String stateType
) {}
