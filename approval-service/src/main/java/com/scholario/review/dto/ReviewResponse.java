package com.scholario.review.dto;

public record ReviewResponse(
    Long id,
    Long bookId,
    Long reviewerId,
    String status,
    String feedback,
    String createdAt,
    String updatedAt
) {}
