package com.scholario.lending.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkIssueInput(
        @NotEmpty(message = "Book IDs are required")
        List<Long> bookIds,

        @NotNull(message = "User ID is required")
        Long userId
) {}
