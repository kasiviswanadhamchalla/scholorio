package com.scholario.book.client;

import com.scholario.book.model.Role;
import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        String fullName,
        Set<Role> roles
) {}
