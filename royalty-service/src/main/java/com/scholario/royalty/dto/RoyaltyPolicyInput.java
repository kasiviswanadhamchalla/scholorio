package com.scholario.royalty.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

public record RoyaltyPolicyInput(
    @NotNull(message = "Book ID is required")
    Long bookId,

    @NotNull(message = "Faculty ID is required")
    Long facultyId,

    @NotNull(message = "Royalty percentage is required")
    @DecimalMin(value = "0.0", message = "Royalty percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Royalty percentage cannot exceed 100")
    BigDecimal royaltyPercentage,

    @NotNull(message = "Sharing model is required")
    Map<String, Object> sharingModel
) {}
