package com.scholario.royalty.config;

import com.scholario.royalty.security.JwtAuthenticationFilter;
import com.scholario.royalty.security.KeycloakJwtAuthenticationConverter;
import com.scholario.royalty.security.UnassignedRoleFilter;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
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
    private final UnassignedRoleFilter unassignedRoleFilter;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080/realms/scholario}")
    private String issuerUri;

    @Value("${scholario.security.keycloak.enabled:false}")
    private boolean keycloakEnabled;

    @Bean
    public JwtDecoder jwtDecoder() {
        if (!keycloakEnabled) {
            log.info("Keycloak authentication is explicitly disabled in Royalty Service.");
            return null;
        }
        log.info("Configuring Keycloak NimbusJwtDecoder with issuer-uri: {}", issuerUri);
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/graphql/**",
                    "/graphql",
                    "/graphiql/**",
                    "/internal/**"
                ).permitAll()
                .anyRequest().authenticated()
            );

        if (keycloakEnabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
                .bearerTokenResolver(customBearerTokenResolver())
            );
        }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(unassignedRoleFilter, AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private BearerTokenResolver customBearerTokenResolver() {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return request -> {
            String token = resolver.resolve(request);
            if (token != null) {
                try {
                    com.auth0.jwt.JWT.decode(token);
                    String alg = com.auth0.jwt.JWT.decode(token).getAlgorithm();
                    if ("HS256".equalsIgnoreCase(alg)) {
                        log.debug("Bearer token is a symmetric HS256 JWT, skipping Keycloak decoder");
                        return null;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            return token;
        };
    }
}
