package com.scholario.analytics.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtService {

    @Value("${auth.jwt.secret:scholario-secret-key-2026-very-secure}")
    private String secretKey;

    public DecodedJWT validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        return validateToken(token).getClaim("roles").asList(String.class);
    }
}
