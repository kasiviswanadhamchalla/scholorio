package com.scholario.recommendation.dto;

public record CourseMaterialSuggestion(
    Long bookId,
    String title,
    String courseContext,
    String suggestionReason
) {}
