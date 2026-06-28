package com.scholario.review.controller;

import com.scholario.review.model.ReviewHistory;
import com.scholario.review.model.ReviewRecord;
import com.scholario.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewRestController {

    private final ReviewService reviewService;

    @PostMapping("/submit")
    public ResponseEntity<ReviewRecord> submitBookForReview(@RequestParam Long bookId, @RequestParam(required = false) Long reviewerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.submitBookForReview(bookId, reviewerId));
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ReviewRecord> approveBook(@PathVariable Long requestId, @RequestParam(required = false) String feedback) {
        return ResponseEntity.ok(reviewService.approveBook(requestId, feedback));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ReviewRecord> rejectBook(@PathVariable Long requestId, @RequestParam String feedback) {
        return ResponseEntity.ok(reviewService.rejectBook(requestId, feedback));
    }

    @PostMapping("/{requestId}/changes")
    public ResponseEntity<ReviewRecord> requestChanges(@PathVariable Long requestId, @RequestParam String feedback) {
        return ResponseEntity.ok(reviewService.requestChanges(requestId, feedback));
    }

    @GetMapping("/status/{bookId}")
    public ResponseEntity<ReviewRecord> getReviewStatus(@PathVariable Long bookId) {
        return reviewService.getReviewStatus(bookId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history/{bookId}")
    public ResponseEntity<List<ReviewHistory>> getReviewHistory(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewHistory(bookId));
    }
}
