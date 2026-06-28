package com.scholario.analytics.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "scholario-secret-key-2026-very-secure";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
    }

    @Test
    void testValidateToken() {
        String token = JWT.create()
                .withSubject("testuser")
                .withClaim("roles", List.of("MEMBER"))
                .sign(Algorithm.HMAC256(secret));

        DecodedJWT decoded = jwtService.validateToken(token);
        assertNotNull(decoded);
        assertEquals("testuser", decoded.getSubject());
    }

    @Test
    void testGetUsernameFromToken() {
        String token = JWT.create()
                .withSubject("testuser")
                .withClaim("roles", List.of("MEMBER"))
                .sign(Algorithm.HMAC256(secret));

        assertEquals("testuser", jwtService.getUsernameFromToken(token));
    }

    @Test
    void testGetRolesFromToken() {
        String token = JWT.create()
                .withSubject("testuser")
                .withClaim("roles", List.of("MEMBER"))
                .sign(Algorithm.HMAC256(secret));

        assertEquals(List.of("MEMBER"), jwtService.getRolesFromToken(token));
    }
}
