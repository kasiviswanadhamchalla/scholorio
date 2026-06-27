package com.scholario.identity.dto;

public record TokenValidationResponse(
    boolean valid,
    String username,
    String role,
    String expiresAt
) {}
