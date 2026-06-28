package com.scholario.identity.config;

import com.scholario.identity.security.JwtAuthenticationFilter;
import com.scholario.identity.security.KeycloakJwtAuthenticationConverter;
import com.scholario.identity.security.KeycloakUserSyncFilter;
import com.scholario.identity.security.UnassignedRoleFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final KeycloakUserSyncFilter keycloakUserSyncFilter;
    private final UnassignedRoleFilter unassignedRoleFilter;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080/realms/scholario}")
    private String issuerUri;

    @Value("${scholario.security.keycloak.enabled:false}")
    private boolean keycloakEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (!keycloakEnabled) {
            log.info("Keycloak authentication is explicitly disabled in Identity Service.");
            return null;
        }

        log.info("Initializing Keycloak JwtDecoder with issuer: {}", issuerUri);
        try {
            return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        } catch (Exception e) {
            log.warn("Could not initialize JwtDecoder via discovery. Fallback to JWK Set URI. Error: {}", e.getMessage());
            String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
            log.info("Attempting fallback to JWK Set URI: {}", jwkSetUri);
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-requested-with"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return request -> {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                return null;
            }
            return resolver.resolve(request);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, Optional<JwtDecoder> jwtDecoder) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/auth/**",
                        "/h2-console/**",
                        "/graphiql/**",
                        "/favicon.ico",
                        "/graphql/**",
                        "/graphql",
                        "/internal/**"
                ).permitAll()
                .anyRequest().authenticated()
            );

        if (jwtDecoder.isPresent()) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(bearerTokenResolver())
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
            )
            .addFilterAfter(keycloakUserSyncFilter, BearerTokenAuthenticationFilter.class);
        } else {
            log.info("Configuring security without Keycloak support.");
        }

        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(unassignedRoleFilter, AuthorizationFilter.class);

        return http.build();
    }
}
