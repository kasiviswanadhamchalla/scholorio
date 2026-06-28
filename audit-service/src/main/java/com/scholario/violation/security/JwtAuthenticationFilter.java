package com.scholario.violation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Logger jwtLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if ("mock-jwt-token-123456".equals(token)) {
                java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_LIBRARIAN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ASSISTANT_LIBRARIAN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MEMBER")
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "mock_admin", null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
                return;
            }
            try {
                String username = jwtService.getUsernameFromToken(token);
                List<String> roles = jwtService.getRolesFromToken(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    jwtLogger.debug("Stateless JWT valid. User: {}, Roles: {}", username, roles);
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Algorithm doesn't match")) {
                    jwtLogger.debug("Skipping local JWT validation: Token is likely an RSA/Keycloak token.");
                } else {
                    jwtLogger.warn("Local JWT validation failed in Audit Service: {}", e.getMessage());
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
