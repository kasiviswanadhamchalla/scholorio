package com.scholario.course.client;

import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        String fullName,
        Set<String> roles
) {}
