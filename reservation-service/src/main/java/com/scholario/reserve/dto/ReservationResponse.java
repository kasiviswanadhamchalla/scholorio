package com.scholario.reserve.dto;

import com.scholario.reserve.model.Reservation;

public record ReservationResponse(
    Long id,
    Long bookId,
    Long userId,
    String reservedAt,
    String expiresAt,
    String status
) {
    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getBookId(),
            reservation.getUserId(),
            reservation.getReservedAt() != null ? reservation.getReservedAt().toString() : null,
            reservation.getExpiresAt() != null ? reservation.getExpiresAt().toString() : null,
            reservation.getStatus() != null ? reservation.getStatus().name() : "PENDING"
        );
    }
}
