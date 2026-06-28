package com.scholario.analytics.controller;

import com.scholario.analytics.dto.*;
import com.scholario.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;

    @GetMapping("/book/{bookId}")
    public ResponseEntity<BookUsageAnalytics> getBookUsageAnalytics(@PathVariable Long bookId) {
        return ResponseEntity.ok(analyticsService.getBookUsageAnalytics(bookId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<CourseMaterialStats> getCourseMaterialStats(@PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getCourseMaterialStats(courseId));
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<FacultyPerformance> getFacultyPerformance(@PathVariable Long facultyId) {
        return ResponseEntity.ok(analyticsService.getFacultyPerformance(facultyId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentEngagement> getStudentEngagement(@PathVariable Long studentId) {
        return ResponseEntity.ok(analyticsService.getStudentEngagement(studentId));
    }

    @GetMapping("/librarian")
    public ResponseEntity<LibrarianStats> getLibrarianStats() {
        return ResponseEntity.ok(analyticsService.getLibrarianStats());
    }
}
