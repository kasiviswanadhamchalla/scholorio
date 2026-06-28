package com.scholario.reserve.service;

import com.scholario.reserve.client.BookServiceClient;
import com.scholario.reserve.client.IdentityServiceClient;
import com.scholario.reserve.client.UserDto;
import com.scholario.reserve.event.ReservationEventProducer;
import com.scholario.reserve.model.*;
import com.scholario.reserve.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;
    @Mock
    private ReservationEventProducer reservationEventProducer;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void testReserveBook_Success() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation res = reservationService.reserveBook(1L, 10L);

        assertNotNull(res);
        assertEquals(1L, res.getBookId());
        assertEquals(10L, res.getUserId());
        assertTrue(res.getStatus() instanceof Pending);
        verify(reservationEventProducer).publishReservationEvent(eq("RESERVATION_CREATED"), any(), eq(1L), eq(10L), eq("PENDING"));
    }

    @Test
    void testCancelReservation_Success() {
        Reservation res = new Reservation();
        res.setId(100L);
        res.setBookId(1L);
        res.setUserId(10L);
        res.setStatus(new Pending());

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation cancelled = reservationService.cancelReservation(100L);

        assertTrue(cancelled.getStatus() instanceof Cancelled);
        verify(reservationEventProducer).publishReservationEvent("RESERVATION_CANCELLED", 100L, 1L, 10L, "CANCELLED");
    }

    @Test
    void testCancelReservation_NonPending() {
        Reservation res = new Reservation();
        res.setId(100L);
        res.setStatus(new Allocated());

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(res));

        assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(100L));
    }

    @Test
    void testAllocateReservedBook_NoReservations() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(reservationRepository.findByBookIdOrderByReservedAtAsc(1L)).thenReturn(new ArrayList<>());

        Reservation res = reservationService.allocateReservedBook(1L);
        assertNull(res);
    }

    @Test
    void testAllocateReservedBook_FIFO_Allocated() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);

        Reservation res1 = new Reservation();
        res1.setId(101L);
        res1.setBookId(1L);
        res1.setUserId(10L);
        res1.setStatus(new Pending());
        res1.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(reservationRepository.findByBookIdOrderByReservedAtAsc(1L)).thenReturn(List.of(res1));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation allocated = reservationService.allocateReservedBook(1L);

        assertNotNull(allocated);
        assertEquals(101L, allocated.getId());
        assertTrue(allocated.getStatus() instanceof Allocated);
        verify(reservationEventProducer).publishReservationEvent("RESERVATION_ALLOCATED", 101L, 1L, 10L, "ALLOCATED");
    }

    @Test
    void testAllocateReservedBook_FIFO_ExpiredAndNext() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);

        Reservation res1 = new Reservation();
        res1.setId(101L);
        res1.setBookId(1L);
        res1.setUserId(10L);
        res1.setStatus(new Pending());
        res1.setExpiresAt(LocalDateTime.now().minusDays(1)); // expired

        Reservation res2 = new Reservation();
        res2.setId(102L);
        res2.setBookId(1L);
        res2.setUserId(20L);
        res2.setStatus(new Pending());
        res2.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(reservationRepository.findByBookIdOrderByReservedAtAsc(1L)).thenReturn(List.of(res1, res2));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation allocated = reservationService.allocateReservedBook(1L);

        assertNotNull(allocated);
        assertEquals(102L, allocated.getId());
        assertTrue(res1.getStatus() instanceof Expired);
        assertTrue(allocated.getStatus() instanceof Allocated);
        verify(reservationEventProducer).publishReservationEvent("RESERVATION_EXPIRED", 101L, 1L, 10L, "EXPIRED");
        verify(reservationEventProducer).publishReservationEvent("RESERVATION_ALLOCATED", 102L, 1L, 20L, "ALLOCATED");
    }

    @Test
    void testQueriesAndCounts() {
        Reservation res = new Reservation();
        res.setStatus(new Pending());
        when(reservationRepository.findByBookIdOrderByReservedAtAsc(1L)).thenReturn(List.of(res));
        assertEquals(1, reservationService.getReservationQueue(1L).size());

        when(reservationRepository.findByUserId(10L)).thenReturn(List.of(res));
        assertEquals(1, reservationService.getUserReservations(10L).size());

        when(reservationRepository.countByBookId(1L)).thenReturn(2L);
        assertEquals(2L, reservationService.countByBookId(1L));

        when(reservationRepository.countByUserId(10L)).thenReturn(3L);
        assertEquals(3L, reservationService.countByUserId(10L));

        when(reservationRepository.countByStatusType("Pending")).thenReturn(5L);
        assertEquals(5L, reservationService.countPending());
    }
}
