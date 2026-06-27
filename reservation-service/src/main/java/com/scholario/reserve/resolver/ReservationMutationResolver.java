package com.scholario.reserve.resolver;

import com.scholario.reserve.client.IdentityServiceClient;
import com.scholario.reserve.client.UserDto;
import com.scholario.reserve.dto.ReservationResponse;
import com.scholario.reserve.model.Reservation;
import com.scholario.reserve.service.ReservationService;
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
public class ReservationMutationResolver {

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

    @MutationMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'LIBRARIAN', 'ADMIN')")
    public ReservationResponse reserveBook(@Argument Long bookId) {
        Reservation reservation = reservationService.reserveBook(bookId, getCurrentUserId());
        return ReservationResponse.fromEntity(reservation);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'LIBRARIAN', 'ADMIN')")
    public ReservationResponse cancelReservation(@Argument Long reservationId) {
        Reservation reservation = reservationService.cancelReservation(reservationId);
        return ReservationResponse.fromEntity(reservation);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ReservationResponse allocateReservedBook(@Argument Long bookId) {
        Reservation reservation = reservationService.allocateReservedBook(bookId);
        if (reservation == null) {
            return null;
        }
        return ReservationResponse.fromEntity(reservation);
    }
}
