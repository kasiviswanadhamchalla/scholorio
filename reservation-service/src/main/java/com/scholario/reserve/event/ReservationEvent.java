package com.scholario.reserve.event;

public record ReservationEvent(
        String eventType,
        Long reservationId,
        Long bookId,
        Long userId,
        String status
) {}
