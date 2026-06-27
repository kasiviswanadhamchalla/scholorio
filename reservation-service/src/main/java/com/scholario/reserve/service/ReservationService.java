package com.scholario.reserve.service;

import com.scholario.reserve.client.BookServiceClient;
import com.scholario.reserve.client.IdentityServiceClient;
import com.scholario.reserve.event.ReservationEventProducer;
import com.scholario.reserve.model.*;
import com.scholario.reserve.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookServiceClient bookServiceClient;
    private final IdentityServiceClient identityServiceClient;
    private final ReservationEventProducer reservationEventProducer;

    public ReservationService(ReservationRepository reservationRepository,
                              BookServiceClient bookServiceClient,
                              IdentityServiceClient identityServiceClient,
                              ReservationEventProducer reservationEventProducer) {
        this.reservationRepository = reservationRepository;
        this.bookServiceClient = bookServiceClient;
        this.identityServiceClient = identityServiceClient;
        this.reservationEventProducer = reservationEventProducer;
    }

    @Transactional
    public Reservation reserveBook(Long bookId, Long userId) {
        validateBookExists(bookId);
        validateUserExists(userId);

        Reservation reservation = new Reservation();
        reservation.setBookId(bookId);
        reservation.setUserId(userId);
        reservation.setReservedAt(LocalDateTime.now());
        // Default expiry of 2 days
        reservation.setExpiresAt(LocalDateTime.now().plusDays(2));
        reservation.setStatus(new Pending());
        
        Reservation saved = reservationRepository.save(reservation);
        
        reservationEventProducer.publishReservationEvent("RESERVATION_CREATED", saved.getId(), saved.getBookId(), saved.getUserId(), "PENDING");

        return saved;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));
        
        if (!(reservation.getStatus() instanceof Pending)) {
            throw new IllegalStateException("Only pending reservations can be cancelled");
        }

        reservation.setStatus(new Cancelled());
        Reservation saved = reservationRepository.save(reservation);

        reservationEventProducer.publishReservationEvent("RESERVATION_CANCELLED", saved.getId(), saved.getBookId(), saved.getUserId(), "CANCELLED");

        return saved;
    }

    @Transactional
    public Reservation allocateReservedBook(Long bookId) {
        validateBookExists(bookId);

        // FIFO: Get all reservations for the book ordered by date
        List<Reservation> allReservations = reservationRepository.findByBookIdOrderByReservedAtAsc(bookId);

        LocalDateTime now = LocalDateTime.now();
        for (Reservation reservation : allReservations) {
            if (!(reservation.getStatus() instanceof Pending)) {
                continue;
            }

            if (reservation.getExpiresAt() != null && reservation.getExpiresAt().isBefore(now)) {
                reservation.setStatus(new Expired());
                Reservation savedExpired = reservationRepository.save(reservation);
                reservationEventProducer.publishReservationEvent("RESERVATION_EXPIRED", savedExpired.getId(), savedExpired.getBookId(), savedExpired.getUserId(), "EXPIRED");
                continue;
            }

            reservation.setStatus(new Allocated());
            Reservation savedAllocated = reservationRepository.save(reservation);
            
            reservationEventProducer.publishReservationEvent("RESERVATION_ALLOCATED", savedAllocated.getId(), savedAllocated.getBookId(), savedAllocated.getUserId(), "ALLOCATED");
            
            return savedAllocated;
        }

        return null;
    }

    public List<Reservation> getReservationQueue(Long bookId) {
        return reservationRepository.findByBookIdOrderByReservedAtAsc(bookId).stream()
                .filter(r -> r.getStatus() instanceof Pending)
                .toList();
    }

    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    private void validateBookExists(Long bookId) {
        Boolean exists = bookServiceClient.existsById(bookId);
        if (exists == null || !exists) {
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }
    }

    private void validateUserExists(Long userId) {
        if (identityServiceClient.getUserById(userId) == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
    }

    public long countByBookId(Long bookId) {
        return reservationRepository.countByBookId(bookId);
    }

    public long countByUserId(Long userId) {
        return reservationRepository.countByUserId(userId);
    }

    public long countPending() {
        return reservationRepository.countByStatusType("Pending");
    }
}
