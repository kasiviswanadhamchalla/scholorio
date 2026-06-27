package com.scholario.recommendation.dto;

public record BookRecommendation(
    Long bookId,
    String title,
    String recommendationReason,
    double confidenceScore
) {}
