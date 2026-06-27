package com.scholario.content.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
public class UnassignedRoleFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UnassignedRoleFilter.class);

    private final List<String> publicPaths = List.of(
            "/h2-console",
            "/graphiql",
            "/favicon.ico",
            "/assets/",
            "/favicon.svg",
            "/graphiql-local",
            "/monacoeditorwork",
            "/graphql"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !isPublicResource(request)) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            
            boolean hasUnassigned = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_UNASSIGNED"));
            boolean hasOtherRoles = authorities.stream()
                    .anyMatch(a -> !a.getAuthority().equals("ROLE_UNASSIGNED"));

            if (hasUnassigned && !hasOtherRoles) {
                log.warn("Blocking access to {} for user with only UNASSIGNED role.", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied: Your account role is currently UNASSIGNED. Please wait for an administrator to assign a role.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicResource(HttpServletRequest request) {
        String path = request.getRequestURI();
        return publicPaths.stream().anyMatch(path::startsWith);
    }
}
