package com.scholario.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseInput(
        @NotBlank(message = "Course code is required")
        String courseCode,

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Faculty ID is required")
        Long facultyId
) {}
