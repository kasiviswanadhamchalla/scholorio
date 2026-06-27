package com.scholario.analytics.dto;

public record FacultyPerformance(
    Long facultyId,
    String facultyName,
    long booksAuthored,
    long coursesTaught,
    long totalStudentEngagement
) {}
