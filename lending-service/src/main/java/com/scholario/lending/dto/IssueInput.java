package com.scholario.lending.dto;

import jakarta.validation.constraints.NotNull;

public record IssueInput(
        @NotNull(message = "Book ID is required")
        Long bookId,

        @NotNull(message = "User ID is required")
        Long userId
) {}
