package com.scholario.analytics.client;

import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        String fullName,
        Set<String> roles
) {}
