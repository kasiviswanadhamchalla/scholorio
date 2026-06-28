package com.scholario.recommendation.controller;

import com.scholario.recommendation.dto.BookRecommendation;
import com.scholario.recommendation.dto.CourseMaterialSuggestion;
import com.scholario.recommendation.dto.DemandPrediction;
import com.scholario.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RecommendationRestController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommend/{userId}")
    public ResponseEntity<List<BookRecommendation>> recommendBooks(@PathVariable Long userId) {
        return ResponseEntity.ok(recommendationService.recommendBooks(userId));
    }

    @GetMapping("/suggest/{courseId}")
    public ResponseEntity<List<CourseMaterialSuggestion>> suggestCourseMaterials(@PathVariable Long courseId) {
        return ResponseEntity.ok(recommendationService.suggestCourseMaterials(courseId));
    }

    @GetMapping("/predict")
    public ResponseEntity<List<DemandPrediction>> predictDemand() {
        return ResponseEntity.ok(recommendationService.predictDemand());
    }
}
