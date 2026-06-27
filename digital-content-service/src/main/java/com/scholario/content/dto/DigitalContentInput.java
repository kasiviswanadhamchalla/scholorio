package com.scholario.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DigitalContentInput {
    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotBlank(message = "Content type is required")
    private String contentType;

    @NotBlank(message = "Content URL is required")
    private String contentUrl;

    private boolean drmEnforced;
}
