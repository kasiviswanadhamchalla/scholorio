package com.scholario.review.controller;

import com.scholario.review.model.ReviewHistory;
import com.scholario.review.model.ReviewRecord;
import com.scholario.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewRestController reviewRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewRestController).build();
    }

    @Test
    void testSubmitBookForReview() throws Exception {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        when(reviewService.submitBookForReview(1L, 10L)).thenReturn(record);

        mockMvc.perform(post("/submit")
                        .param("bookId", "1")
                        .param("reviewerId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testApproveBook() throws Exception {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        when(reviewService.approveBook(100L, "Looks good")).thenReturn(record);

        mockMvc.perform(post("/100/approve")
                        .param("feedback", "Looks good"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testRejectBook() throws Exception {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        when(reviewService.rejectBook(100L, "Bad quality")).thenReturn(record);

        mockMvc.perform(post("/100/reject")
                        .param("feedback", "Bad quality"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testRequestChanges() throws Exception {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        when(reviewService.requestChanges(100L, "Fix spelling")).thenReturn(record);

        mockMvc.perform(post("/100/changes")
                        .param("feedback", "Fix spelling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testGetReviewStatus() throws Exception {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        when(reviewService.getReviewStatus(1L)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        when(reviewService.getReviewStatus(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/status/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetReviewHistory() throws Exception {
        ReviewHistory history = new ReviewHistory();
        history.setId(500L);
        when(reviewService.getReviewHistory(1L)).thenReturn(List.of(history));

        mockMvc.perform(get("/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(500L));
    }
}
