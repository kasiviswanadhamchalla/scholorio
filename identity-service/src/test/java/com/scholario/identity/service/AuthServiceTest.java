package com.scholario.identity.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.scholario.identity.dto.AuthResponse;
import com.scholario.identity.dto.LoginInput;
import com.scholario.identity.dto.TokenValidationResponse;
import com.scholario.identity.model.User;
import com.scholario.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLogin_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedpass")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");

        LoginInput input = new LoginInput("testuser", "password");
        AuthResponse response = authService.login(input);

        assertNotNull(response);
        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        LoginInput input = new LoginInput("testuser", "password");
        assertThrows(BadCredentialsException.class, () -> authService.login(input));
    }

    @Test
    void testLogin_PasswordMismatch() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedpass")).thenReturn(false);

        LoginInput input = new LoginInput("testuser", "password");
        assertThrows(BadCredentialsException.class, () -> authService.login(input));
    }

    @Test
    void testRefreshToken_Success() {
        DecodedJWT decoded = mock(DecodedJWT.class);
        when(decoded.getSubject()).thenReturn("testuser");
        when(jwtService.validateToken("refresh-token")).thenReturn(decoded);

        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");

        AuthResponse resp = authService.refreshToken("refresh-token");
        assertNotNull(resp);
        assertEquals("new-access", resp.accessToken());
    }

    @Test
    void testValidateToken_Success() {
        DecodedJWT decoded = mock(DecodedJWT.class);
        when(decoded.getSubject()).thenReturn("testuser");
        when(decoded.getExpiresAt()).thenReturn(new Date());
        com.auth0.jwt.interfaces.Claim claim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(claim.asArray(String.class)).thenReturn(new String[]{"ROLE_MEMBER"});
        when(decoded.getClaim("roles")).thenReturn(claim);

        when(jwtService.validateToken("token")).thenReturn(decoded);

        TokenValidationResponse response = authService.validateToken("token");
        assertTrue(response.valid());
        assertEquals("testuser", response.username());
    }

    @Test
    void testValidateToken_Failure() {
        when(jwtService.validateToken("invalid")).thenThrow(new RuntimeException("invalid"));
        TokenValidationResponse response = authService.validateToken("invalid");
        assertFalse(response.valid());
    }

    @Test
    void testLogout() {
        assertTrue(authService.logout());
    }
}
