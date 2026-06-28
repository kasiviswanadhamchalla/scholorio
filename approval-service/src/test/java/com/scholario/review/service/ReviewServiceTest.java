package com.scholario.review.service;

import com.scholario.review.client.BookServiceClient;
import com.scholario.review.client.IdentityServiceClient;
import com.scholario.review.client.UserDto;
import com.scholario.review.event.ReviewEventProducer;
import com.scholario.review.model.*;
import com.scholario.review.repository.ReviewHistoryRepository;
import com.scholario.review.repository.ReviewRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRecordRepository reviewRecordRepository;
    @Mock
    private ReviewHistoryRepository reviewHistoryRepository;
    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;
    @Mock
    private ReviewEventProducer reviewEventProducer;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void testSubmitBookForReview_Success_NewRecord() {
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(reviewRecordRepository.findByBookId(1L)).thenReturn(Optional.empty());
        
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        record.setBookId(1L);
        record.setReviewerId(10L);
        when(reviewRecordRepository.save(any(ReviewRecord.class))).thenReturn(record);

        ReviewRecord submitted = reviewService.submitBookForReview(1L, 10L);

        assertNotNull(submitted);
        verify(bookServiceClient).submitForReview(1L);
        verify(reviewHistoryRepository).save(any(ReviewHistory.class));
        verify(reviewEventProducer).publishReviewEvent("BOOK_SUBMITTED_FOR_REVIEW", 100L, 1L, 10L, "PENDING", null);
    }

    @Test
    void testSubmitBookForReview_ReviewerNotFound() {
        when(identityServiceClient.getUserById(10L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> reviewService.submitBookForReview(1L, 10L));
    }

    @Test
    void testApproveBook_Success() {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        record.setBookId(1L);
        record.setReviewerId(10L);
        record.setStatus(new Pending());

        when(reviewRecordRepository.findById(100L)).thenReturn(Optional.of(record));
        when(reviewRecordRepository.save(any(ReviewRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRecord approved = reviewService.approveBook(100L, "Nice book");

        assertTrue(approved.getStatus() instanceof Approved);
        assertEquals("Nice book", approved.getFeedback());
        verify(bookServiceClient).publishBook(1L);
        verify(reviewHistoryRepository).save(any(ReviewHistory.class));
        verify(reviewEventProducer).publishReviewEvent("BOOK_APPROVED", 100L, 1L, 10L, "APPROVED", "Nice book");
    }

    @Test
    void testApproveBook_NonPending() {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        record.setStatus(new Approved());

        when(reviewRecordRepository.findById(100L)).thenReturn(Optional.of(record));

        assertThrows(IllegalStateException.class, () -> reviewService.approveBook(100L, "feedback"));
    }

    @Test
    void testRejectBook_Success() {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        record.setBookId(1L);
        record.setReviewerId(10L);
        record.setStatus(new Pending());

        when(reviewRecordRepository.findById(100L)).thenReturn(Optional.of(record));
        when(reviewRecordRepository.save(any(ReviewRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRecord rejected = reviewService.rejectBook(100L, "Bad quality");

        assertTrue(rejected.getStatus() instanceof Rejected);
        assertEquals("Bad quality", rejected.getFeedback());
        verify(bookServiceClient).archiveBook(1L);
        verify(reviewEventProducer).publishReviewEvent("BOOK_REJECTED", 100L, 1L, 10L, "REJECTED", "Bad quality");
    }

    @Test
    void testRejectBook_NoFeedback() {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        record.setStatus(new Pending());
        when(reviewRecordRepository.findById(100L)).thenReturn(Optional.of(record));

        assertThrows(IllegalArgumentException.class, () -> reviewService.rejectBook(100L, ""));
    }

    @Test
    void testRequestChanges_Success() {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        record.setBookId(1L);
        record.setReviewerId(10L);
        record.setStatus(new Pending());

        when(reviewRecordRepository.findById(100L)).thenReturn(Optional.of(record));
        when(reviewRecordRepository.save(any(ReviewRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRecord result = reviewService.requestChanges(100L, "Fix spelling");

        assertTrue(result.getStatus() instanceof ChangesRequested);
        assertEquals("Fix spelling", result.getFeedback());
        verify(bookServiceClient).resetToDraft(1L);
        verify(reviewEventProducer).publishReviewEvent("BOOK_CHANGES_REQUESTED", 100L, 1L, 10L, "CHANGES_REQUESTED", "Fix spelling");
    }

    @Test
    void testGetReviewStatusAndHistory() {
        ReviewRecord record = new ReviewRecord();
        record.setId(100L);
        when(reviewRecordRepository.findByBookId(1L)).thenReturn(Optional.of(record));

        assertTrue(reviewService.getReviewStatus(1L).isPresent());

        ReviewHistory history = new ReviewHistory();
        when(reviewHistoryRepository.findByReviewRecordIdOrderByTimestampDesc(100L)).thenReturn(List.of(history));

        assertEquals(1, reviewService.getReviewHistory(1L).size());

        when(reviewRecordRepository.findByBookId(2L)).thenReturn(Optional.empty());
        assertTrue(reviewService.getReviewHistory(2L).isEmpty());
    }
}
