package com.scholario.course.dto;

import jakarta.validation.constraints.NotNull;

public record CourseMaterialInput(
        @NotNull(message = "Course ID is required")
        Long courseId,

        @NotNull(message = "Book ID is required")
        Long bookId,

        @NotNull(message = "Mandatory flag is required")
        Boolean mandatory
) {}
