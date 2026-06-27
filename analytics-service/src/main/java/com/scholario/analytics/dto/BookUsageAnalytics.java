package com.scholario.analytics.dto;

public record BookUsageAnalytics(
    Long bookId,
    String title,
    long totalIssues,
    long totalReservations,
    long digitalAccessCount
) {}
