package com.scholario.reserve.resolver;

import com.scholario.reserve.client.IdentityServiceClient;
import com.scholario.reserve.client.UserDto;
import com.scholario.reserve.dto.ReservationResponse;
import com.scholario.reserve.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReservationQueryResolver {

    private final ReservationService reservationService;
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

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public List<ReservationResponse> getReservationQueue(@Argument Long bookId) {
        return reservationService.getReservationQueue(bookId).stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
    public List<ReservationResponse> getUserReservations() {
        return reservationService.getUserReservations(getCurrentUserId()).stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public int countReservationByBook(@Argument Long bookId) {
        return (int) reservationService.countByBookId(bookId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public int countReservationByUser(@Argument Long userId) {
        return (int) reservationService.countByUserId(userId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public int countPendingReservations() {
        return (int) reservationService.countPending();
    }
}
