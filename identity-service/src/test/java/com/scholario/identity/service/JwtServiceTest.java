package com.scholario.identity.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.scholario.identity.model.Role;
import com.scholario.identity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "scholario-secret-key-2026-very-secure";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@scholario.com");
        user.setRoles(Set.of(Role.MEMBER));

        String token = jwtService.generateAccessToken(user);
        assertNotNull(token);

        DecodedJWT decoded = jwtService.validateToken(token);
        assertNotNull(decoded);
        assertEquals("testuser", decoded.getSubject());
        assertEquals("test@scholario.com", decoded.getClaim("email").asString());
        assertEquals("testuser", jwtService.getUsernameFromToken(token));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@scholario.com");
        user.setRoles(Set.of(Role.MEMBER));

        String token = jwtService.generateRefreshToken(user);
        assertNotNull(token);

        DecodedJWT decoded = jwtService.validateToken(token);
        assertNotNull(decoded);
    }
}
