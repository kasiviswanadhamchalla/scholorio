package com.scholario.review.resolver;

import com.scholario.review.dto.ReviewResponse;
import com.scholario.review.model.ReviewHistory;
import com.scholario.review.model.ReviewRecord;
import com.scholario.review.service.ReviewService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
public class ReviewQueryResolver {

    private final ReviewService reviewService;

    public ReviewQueryResolver(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @QueryMapping
    public ReviewResponse getReviewStatus(@Argument Long bookId) {
        return reviewService.getReviewStatus(bookId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @QueryMapping
    public List<ReviewHistoryResponse> getReviewHistory(@Argument Long bookId) {
        return reviewService.getReviewHistory(bookId).stream()
                .map(h -> new ReviewHistoryResponse(
                        h.getId(),
                        h.getReviewRecordId(),
                        h.getStatus().name(),
                        h.getFeedback(),
                        h.getPerformedBy(),
                        h.getTimestamp().toString()
                ))
                .toList();
    }

    private ReviewResponse mapToResponse(ReviewRecord record) {
        return new ReviewResponse(
                record.getId(),
                record.getBookId(),
                record.getReviewerId(),
                record.getStatus().name(),
                record.getFeedback(),
                record.getCreatedAt().toString(),
                record.getUpdatedAt().toString()
        );
    }

    public record ReviewHistoryResponse(
            Long id,
            Long reviewRecordId,
            String status,
            String feedback,
            Long performedBy,
            String timestamp
    ) {}
}
