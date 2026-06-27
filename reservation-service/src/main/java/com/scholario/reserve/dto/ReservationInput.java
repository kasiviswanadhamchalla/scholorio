package com.scholario.reserve.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationInput(
    @NotNull(message = "Book ID is required")
    Long bookId,

    @NotNull(message = "User ID is required")
    Long userId
) {}
