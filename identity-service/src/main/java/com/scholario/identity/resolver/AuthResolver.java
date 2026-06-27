package com.scholario.identity.resolver;

import com.scholario.identity.dto.AuthResponse;
import com.scholario.identity.dto.LoginInput;
import com.scholario.identity.dto.TokenValidationResponse;
import com.scholario.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;

    @MutationMapping
    public AuthResponse login(@Valid @Argument LoginInput input) {
        return authService.login(input);
    }

    @MutationMapping
    public AuthResponse refreshToken(@Argument String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean logout() {
        return authService.logout();
    }

    @QueryMapping
    public TokenValidationResponse validateToken(@Argument String token) {
        return authService.validateToken(token);
    }
}
