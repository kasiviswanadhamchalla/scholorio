package com.scholario.book.dto;

import jakarta.validation.constraints.NotBlank;

public record BookInput(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "ISBN is required")
        String isbn,

        String description
) {
}
