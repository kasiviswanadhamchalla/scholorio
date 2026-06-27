package com.scholario.recommendation.service;

import com.scholario.recommendation.client.*;
import com.scholario.recommendation.dto.BookRecommendation;
import com.scholario.recommendation.dto.CourseMaterialSuggestion;
import com.scholario.recommendation.dto.DemandPrediction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookServiceClient bookServiceClient;
    private final IdentityServiceClient identityServiceClient;
    private final LendingServiceClient lendingServiceClient;
    private final CourseServiceClient courseServiceClient;

    public List<BookRecommendation> recommendBooks(Long userId) {
        UserDto user = identityServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        List<IssueRecordDto> history = lendingServiceClient.getIssuesByUser(userId);
        List<Long> borrowedBookIds = history.stream()
                .map(IssueRecordDto::bookId)
                .toList();

        return bookServiceClient.getAllBooks().stream()
                .filter(b -> !borrowedBookIds.contains(b.id()))
                .limit(5)
                .map(b -> new BookRecommendation(
                        b.id(),
                        b.title(),
                        "Popular in your department",
                        0.85))
                .toList();
    }

    public List<CourseMaterialSuggestion> suggestCourseMaterials(Long courseId) {
        CourseDto course = courseServiceClient.getCourseById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        List<CourseMaterialDto> materials = courseServiceClient.getMaterialsByCourse(courseId);
        List<Long> assignedIds = materials.stream()
                .map(CourseMaterialDto::bookId)
                .toList();
        
        return bookServiceClient.getBooksByFaculty(course.facultyId()).stream()
                .filter(b -> !assignedIds.contains(b.id()))
                .limit(3)
                .map(b -> new CourseMaterialSuggestion(
                        b.id(),
                        b.title(),
                        course.title(),
                        "Authored by course faculty"))
                .toList();
    }

    public List<DemandPrediction> predictDemand() {
        List<BookIssueCountDto> issueCounts = lendingServiceClient.getIssuesByBook();
        Map<Long, Long> issueCountsByBook = issueCounts.stream()
                .collect(Collectors.toMap(
                        BookIssueCountDto::bookId,
                        BookIssueCountDto::issueCount,
                        (existing, replacement) -> existing
                ));

        return bookServiceClient.getAllBooks().stream()
                .limit(10)
                .map(b -> {
                    long issues = issueCountsByBook.getOrDefault(b.id(), 0L);
                    int predicted = (int) (issues * 1.2 + 5);
                    String risk = predicted > 20 ? "HIGH" : "LOW";
                    return new DemandPrediction(b.id(), b.title(), predicted, risk);
                })
                .sorted((a, b) -> Integer.compare(b.predictedDemandCount(), a.predictedDemandCount()))
                .toList();
    }
}
