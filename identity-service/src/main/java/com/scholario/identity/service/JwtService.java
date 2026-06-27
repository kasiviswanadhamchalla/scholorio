package com.scholario.identity.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.scholario.identity.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${auth.jwt.secret:scholario-secret-key-2026-very-secure}")
    private String secretKey;

    @Value("${auth.jwt.access-token-expiration-ms:3600000}")
    private long accessTokenExpirationMs;

    @Value("${auth.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpirationMs);
    }

    private String generateToken(User user, long expirationMs) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withSubject(user.getUsername())
                .withArrayClaim("roles", user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .withClaim("email", user.getEmail())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }
}
