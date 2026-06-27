package com.scholario.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentInput(
    @NotBlank(message = "Department name is required")
    String name,

    @NotBlank(message = "Department code is required")
    String code
) {}
