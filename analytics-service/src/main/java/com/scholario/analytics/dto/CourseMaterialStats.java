package com.scholario.analytics.dto;

public record CourseMaterialStats(
    Long courseId,
    String courseCode,
    long totalMaterials,
    long mandatoryCount,
    long optionalCount,
    double averageUsageRate
) {}
