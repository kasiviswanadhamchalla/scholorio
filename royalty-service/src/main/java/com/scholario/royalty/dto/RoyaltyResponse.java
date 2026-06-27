package com.scholario.royalty.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RoyaltyResponse(
    Long id,
    Long bookId,
    Long facultyId,
    BigDecimal totalRevenue,
    BigDecimal calculatedRoyalty,
    String payoutStatus,
    LocalDateTime calculatedAt,
    LocalDateTime distributedAt
) {}
