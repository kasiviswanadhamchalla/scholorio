package com.scholario.analytics.resolver;

import com.scholario.analytics.dto.*;
import com.scholario.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AnalyticsResolver {

    private final AnalyticsService analyticsService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'FACULTY')")
    public BookUsageAnalytics getBookUsageAnalytics(@Argument Long bookId) {
        return analyticsService.getBookUsageAnalytics(bookId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'FACULTY')")
    public CourseMaterialStats getCourseMaterialStats(@Argument Long courseId) {
        return analyticsService.getCourseMaterialStats(courseId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public FacultyPerformance getFacultyPerformance(@Argument Long facultyId) {
        return analyticsService.getFacultyPerformance(facultyId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public StudentEngagement getStudentEngagement(@Argument Long studentId) {
        return analyticsService.getStudentEngagement(studentId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public LibrarianStats getLibrarianStats() {
        return analyticsService.getLibrarianStats();
    }
}
