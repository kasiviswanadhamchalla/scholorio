package com.scholario.review.resolver;

import com.scholario.review.dto.ReviewResponse;
import com.scholario.review.model.ReviewRecord;
import com.scholario.review.service.ReviewService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
public class ReviewMutationResolver {

    private final ReviewService reviewService;

    public ReviewMutationResolver(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @MutationMapping
    public ReviewResponse submitBookForReview(@Argument Long bookId, @Argument Long reviewerId) {
        return mapToResponse(reviewService.submitBookForReview(bookId, reviewerId));
    }

    @MutationMapping
    public ReviewResponse approveBook(@Argument Long requestId, @Argument String feedback) {
        return mapToResponse(reviewService.approveBook(requestId, feedback));
    }

    @MutationMapping
    public ReviewResponse rejectBook(@Argument Long requestId, @Argument String feedback) {
        return mapToResponse(reviewService.rejectBook(requestId, feedback));
    }

    @MutationMapping
    public ReviewResponse requestChanges(@Argument Long requestId, @Argument String feedback) {
        return mapToResponse(reviewService.requestChanges(requestId, feedback));
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
}
