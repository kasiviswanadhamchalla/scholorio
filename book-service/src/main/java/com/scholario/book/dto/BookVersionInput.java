package com.scholario.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookVersionInput(
        @NotNull(message = "Parent book ID is required")
        Long parentBookId,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "ISBN is required")
        String isbn,

        String description
) {
}
