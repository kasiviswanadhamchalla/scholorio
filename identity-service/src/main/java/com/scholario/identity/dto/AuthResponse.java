package com.scholario.identity.dto;

import com.scholario.identity.model.User;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    int expiresIn,
    User user
) {}
