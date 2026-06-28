package com.scholario.reserve.controller;

import com.scholario.reserve.model.Reservation;
import com.scholario.reserve.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReservationRestController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Reservation> reserveBook(@RequestParam Long bookId, @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.reserveBook(bookId, userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @PostMapping("/allocate")
    public ResponseEntity<Reservation> allocateReservedBook(@RequestParam Long bookId) {
        Reservation reservation = reservationService.allocateReservedBook(bookId);
        if (reservation == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/queue/{bookId}")
    public ResponseEntity<List<Reservation>> getReservationQueue(@PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.getReservationQueue(bookId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getUserReservations(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    @GetMapping("/count/by-book/{bookId}")
    public ResponseEntity<Long> countByBookId(@PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.countByBookId(bookId));
    }

    @GetMapping("/count/by-user/{userId}")
    public ResponseEntity<Long> countByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.countByUserId(userId));
    }

    @GetMapping("/count/pending")
    public ResponseEntity<Long> countPending() {
        return ResponseEntity.ok(reservationService.countPending());
    }
}
