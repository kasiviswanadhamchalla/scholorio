package com.scholario.review.service;

import com.scholario.review.client.BookServiceClient;
import com.scholario.review.client.IdentityServiceClient;
import com.scholario.review.event.ReviewEventProducer;
import com.scholario.review.model.*;
import com.scholario.review.repository.ReviewHistoryRepository;
import com.scholario.review.repository.ReviewRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private static final String REVIEW_RECORD_NOT_FOUND = "Review record not found";
    private final ReviewRecordRepository reviewRecordRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final BookServiceClient bookServiceClient;
    private final IdentityServiceClient identityServiceClient;
    private final ReviewEventProducer reviewEventProducer;

    public ReviewService(ReviewRecordRepository reviewRecordRepository,
                         ReviewHistoryRepository reviewHistoryRepository,
                         BookServiceClient bookServiceClient,
                         IdentityServiceClient identityServiceClient,
                         ReviewEventProducer reviewEventProducer) {
        this.reviewRecordRepository = reviewRecordRepository;
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.bookServiceClient = bookServiceClient;
        this.identityServiceClient = identityServiceClient;
        this.reviewEventProducer = reviewEventProducer;
    }

    @Transactional
    public ReviewRecord submitBookForReview(Long bookId, Long reviewerId) {
        if (reviewerId != null && identityServiceClient.getUserById(reviewerId) == null) {
            throw new IllegalArgumentException("Reviewer not found with id: " + reviewerId);
        }

        // 1. Transition Book State to REVIEW via Feign client
        bookServiceClient.submitForReview(bookId);

        // 2. Create or Update Review Record
        ReviewRecord record = reviewRecordRepository.findByBookId(bookId)
                .orElse(new ReviewRecord());
        
        record.setBookId(bookId);
        record.setReviewerId(reviewerId);
        record.setStatus(new Pending());
        record.setFeedback(null);
        
        ReviewRecord savedRecord = reviewRecordRepository.save(record);

        // 3. Add to History
        addHistory(savedRecord, new Pending(), "Book submitted for review", reviewerId);

        // 4. Publish Event
        reviewEventProducer.publishReviewEvent("BOOK_SUBMITTED_FOR_REVIEW", savedRecord.getId(), savedRecord.getBookId(), savedRecord.getReviewerId(), "PENDING", null);

        return savedRecord;
    }

    @Transactional
    public ReviewRecord approveBook(Long requestId, String feedback) {
        ReviewRecord record = reviewRecordRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(REVIEW_RECORD_NOT_FOUND));
        validatePending(record);

        // 1. Update Review Record
        record.setStatus(new Approved());
        record.setFeedback(feedback);
        ReviewRecord savedRecord = reviewRecordRepository.save(record);

        // 2. Transition Book State to PUBLISHED via Feign client
        bookServiceClient.publishBook(record.getBookId());

        // 3. Add to History
        addHistory(savedRecord, new Approved(), feedback, record.getReviewerId());

        // 4. Publish Event
        reviewEventProducer.publishReviewEvent("BOOK_APPROVED", savedRecord.getId(), savedRecord.getBookId(), savedRecord.getReviewerId(), "APPROVED", feedback);

        return savedRecord;
    }

    @Transactional
    public ReviewRecord rejectBook(Long requestId, String feedback) {
        ReviewRecord reviewRecord = reviewRecordRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(REVIEW_RECORD_NOT_FOUND));
        validatePending(reviewRecord);
        validateFeedback(feedback);

        reviewRecord.setStatus(new Rejected());
        reviewRecord.setFeedback(feedback);
        ReviewRecord savedRecord = reviewRecordRepository.save(reviewRecord);

        // Final rejection leads to archiving via Feign client
        bookServiceClient.archiveBook(reviewRecord.getBookId());

        addHistory(savedRecord, new Rejected(), feedback, reviewRecord.getReviewerId());

        // Publish Event
        reviewEventProducer.publishReviewEvent("BOOK_REJECTED", savedRecord.getId(), savedRecord.getBookId(), savedRecord.getReviewerId(), "REJECTED", feedback);

        return savedRecord;
    }

    @Transactional
    public ReviewRecord requestChanges(Long requestId, String feedback) {
        ReviewRecord reviewRecord = reviewRecordRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(REVIEW_RECORD_NOT_FOUND));
        validatePending(reviewRecord);
        validateFeedback(feedback);

        reviewRecord.setStatus(new ChangesRequested());
        reviewRecord.setFeedback(feedback);
        ReviewRecord savedRecord = reviewRecordRepository.save(reviewRecord);

        // Transition Book back to DRAFT via Feign client
        bookServiceClient.resetToDraft(reviewRecord.getBookId());

        addHistory(savedRecord, new ChangesRequested(), feedback, reviewRecord.getReviewerId());

        // Publish Event
        reviewEventProducer.publishReviewEvent("BOOK_CHANGES_REQUESTED", savedRecord.getId(), savedRecord.getBookId(), savedRecord.getReviewerId(), "CHANGES_REQUESTED", feedback);

        return savedRecord;
    }

    public Optional<ReviewRecord> getReviewStatus(Long bookId) {
        return reviewRecordRepository.findByBookId(bookId);
    }

    public List<ReviewHistory> getReviewHistory(Long bookId) {
        return reviewRecordRepository.findByBookId(bookId)
                .map(record -> reviewHistoryRepository.findByReviewRecordIdOrderByTimestampDesc(record.getId()))
                .orElse(List.of());
    }

    private void addHistory(ReviewRecord reviewRecord, ReviewStatus status, String feedback, Long performedBy) {
        ReviewHistory history = new ReviewHistory();
        history.setReviewRecordId(reviewRecord.getId());
        history.setStatus(status);
        history.setFeedback(feedback);
        history.setPerformedBy(performedBy);
        reviewHistoryRepository.save(history);
    }

    private void validatePending(ReviewRecord reviewRecord) {
        if (!(reviewRecord.getStatus() instanceof Pending)) {
            throw new IllegalStateException("Review record must be pending");
        }
    }

    private void validateFeedback(String feedback) {
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalArgumentException("Feedback is required");
        }
    }
}
