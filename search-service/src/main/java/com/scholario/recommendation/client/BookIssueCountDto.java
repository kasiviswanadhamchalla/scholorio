package com.scholario.recommendation.client;

public record BookIssueCountDto(
        Long bookId,
        long issueCount
) {}
