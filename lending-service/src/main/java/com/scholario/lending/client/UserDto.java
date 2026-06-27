package com.scholario.lending.client;

import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        String fullName,
        Set<String> roles
) {}
