package com.scholario.lending.dto;

import jakarta.validation.constraints.NotNull;

public record RenewInput(
        @NotNull(message = "Issue ID is required")
        Long issueId,

        @NotNull(message = "User ID is required")
        Long userId
) {}
