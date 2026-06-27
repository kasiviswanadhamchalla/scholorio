package com.scholario.analytics.dto;

public record LibrarianStats(
    long activeIssues,
    long overdueIssues,
    long returnedToday,
    long activeReservations
) {}
