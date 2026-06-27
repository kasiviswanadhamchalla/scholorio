package com.scholario.notification.resolver;

import com.scholario.notification.client.IdentityServiceClient;
import com.scholario.notification.client.UserDto;
import com.scholario.notification.dto.NotificationResponse;
import com.scholario.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class NotificationMutationResolver {

    private final NotificationService notificationService;
    private final IdentityServiceClient identityServiceClient;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        } else if (principal instanceof String s) {
            return s;
        }
        return authentication.getName();
    }

    private Long getCurrentUserId() {
        String username = getCurrentUsername();
        try {
            UserDto user = identityServiceClient.getUserByUsername(username);
            if (user == null) {
                throw new IllegalArgumentException("User not found in identity service: " + username);
            }
            return user.id();
        } catch (Exception e) {
            log.error("Failed to fetch user from identity-service via Feign", e);
            throw new IllegalStateException("Authentication service unavailable", e);
        }
    }

    @MutationMapping
    public NotificationResponse markNotificationAsRead(@Argument Long id) {
        return notificationService.markAsRead(id);
    }

    @MutationMapping
    public Boolean markAllNotificationsAsRead() {
        notificationService.markAllAsRead(getCurrentUserId());
        return true;
    }
}
