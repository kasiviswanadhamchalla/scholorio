package com.scholario.analytics.dto;

public record StudentEngagement(
    Long studentId,
    String studentName,
    long booksBorrowed,
    long digitalContentAccessed,
    long activeReservations
) {}
