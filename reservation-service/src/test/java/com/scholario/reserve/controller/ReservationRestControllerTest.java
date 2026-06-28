package com.scholario.reserve.controller;

import com.scholario.reserve.model.Reservation;
import com.scholario.reserve.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservationRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationRestController reservationRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationRestController).build();
    }

    @Test
    void testReserveBook() throws Exception {
        Reservation res = new Reservation();
        res.setId(100L);
        when(reservationService.reserveBook(1L, 10L)).thenReturn(res);

        mockMvc.perform(post("/")
                        .param("bookId", "1")
                        .param("userId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testCancelReservation() throws Exception {
        Reservation res = new Reservation();
        res.setId(100L);
        when(reservationService.cancelReservation(100L)).thenReturn(res);

        mockMvc.perform(post("/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testAllocateReservedBook() throws Exception {
        Reservation res = new Reservation();
        res.setId(100L);
        when(reservationService.allocateReservedBook(1L)).thenReturn(res);

        mockMvc.perform(post("/allocate")
                        .param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        when(reservationService.allocateReservedBook(2L)).thenReturn(null);
        mockMvc.perform(post("/allocate")
                        .param("bookId", "2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetReservationQueue() throws Exception {
        Reservation res = new Reservation();
        res.setId(100L);
        when(reservationService.getReservationQueue(1L)).thenReturn(List.of(res));

        mockMvc.perform(get("/queue/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetUserReservations() throws Exception {
        Reservation res = new Reservation();
        res.setId(100L);
        when(reservationService.getUserReservations(10L)).thenReturn(List.of(res));

        mockMvc.perform(get("/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testCounts() throws Exception {
        when(reservationService.countByBookId(1L)).thenReturn(5L);
        mockMvc.perform(get("/count/by-book/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        when(reservationService.countByUserId(10L)).thenReturn(3L);
        mockMvc.perform(get("/count/by-user/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        when(reservationService.countPending()).thenReturn(10L);
        mockMvc.perform(get("/count/pending"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
}
