package com.scholario.recommendation.dto;

public record DemandPrediction(
    Long bookId,
    String title,
    int predictedDemandCount,
    String riskLevel
) {}
