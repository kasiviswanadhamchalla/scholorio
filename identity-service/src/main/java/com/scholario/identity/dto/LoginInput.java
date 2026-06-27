package com.scholario.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginInput(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {}
