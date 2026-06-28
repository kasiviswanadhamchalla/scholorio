package com.scholario.identity.security;

import com.scholario.identity.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class KeycloakUserSyncFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserSyncFilter.class);
    private final UserService userService;

    public KeycloakUserSyncFilter(@org.springframework.context.annotation.Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            try {
                Jwt jwt = jwtAuthenticationToken.getToken();
                
                String username = jwt.getClaimAsString("preferred_username");
                String email = jwt.getClaimAsString("email");
                String fullName = jwt.getClaimAsString("name");
                
                List<String> roles = extractRoles(jwt);
                
                if (username != null) {
                    userService.syncUserFromExternalProvider(username, email, fullName, roles);
                } else {
                    log.warn("Keycloak JWT missing 'preferred_username' claim. User synchronization skipped.");
                }
            } catch (Exception e) {
                log.error("Failed to sync user from Keycloak JWT: {}", e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Set<String> allRoles = new java.util.HashSet<>();
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof Collection) {
                allRoles.addAll((Collection<String>) realmAccess.get("roles"));
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(clientAccess -> {
                    if (clientAccess instanceof Map) {
                        Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
                        if (clientAccessMap.get("roles") instanceof Collection) {
                            allRoles.addAll((Collection<String>) clientAccessMap.get("roles"));
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to extract roles from Keycloak JWT: {}", e.getMessage());
        }
        return allRoles.stream().toList();
    }
}
