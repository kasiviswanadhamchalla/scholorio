package com.scholario.identity.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.scholario.identity.dto.AuthResponse;
import com.scholario.identity.dto.LoginInput;
import com.scholario.identity.dto.TokenValidationResponse;
import com.scholario.identity.model.User;
import com.scholario.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginInput input) {
        User user = userRepository.findByUsername(input.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(input.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                3600,
                user
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        DecodedJWT decodedJWT = jwtService.validateToken(refreshToken);
        String username = decodedJWT.getSubject();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                3600,
                user
        );
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            DecodedJWT decodedJWT = jwtService.validateToken(token);
            String roles = String.join(",", decodedJWT.getClaim("roles").asArray(String.class));
            return new TokenValidationResponse(
                    true,
                    decodedJWT.getSubject(),
                    roles,
                    decodedJWT.getExpiresAt().toString()
            );
        } catch (Exception e) {
            log.info("Token validation failed: {}", e.getMessage());
            return new TokenValidationResponse(false, null, null, null);
        }
    }

    public boolean logout() {
        return true;
    }
}
