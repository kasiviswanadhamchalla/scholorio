package com.scholario.analytics.service;

import com.scholario.analytics.client.*;
import com.scholario.analytics.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final BookServiceClient bookServiceClient;
    private final IdentityServiceClient identityServiceClient;
    private final LendingServiceClient lendingServiceClient;
    private final ReservationServiceClient reservationServiceClient;
    private final DigitalContentServiceClient digitalContentServiceClient;
    private final CourseServiceClient courseServiceClient;

    public BookUsageAnalytics getBookUsageAnalytics(Long bookId) {
        BookDto book = bookServiceClient.getBookById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book not found");
        }

        long issues = lendingServiceClient.countByBookId(bookId);
        long reservations = reservationServiceClient.countByBookId(bookId);
        
        List<Long> contentIds = digitalContentServiceClient.getDigitalContentIdsByBook(bookId);
        long digitalAccess = contentIds.isEmpty() ? 0 : digitalContentServiceClient.countLogsByContents(contentIds);

        return new BookUsageAnalytics(bookId, book.title(), issues, reservations, digitalAccess);
    }

    public CourseMaterialStats getCourseMaterialStats(Long courseId) {
        CourseDto course = courseServiceClient.getCourseById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        List<CourseMaterialDto> materials = courseServiceClient.getMaterialsByCourse(courseId);
        long total = materials.size();
        long mandatory = materials.stream().filter(CourseMaterialDto::mandatory).count();
        long optional = total - mandatory;

        if (total == 0) {
            return new CourseMaterialStats(courseId, course.courseCode(), 0, 0, 0, 0.0);
        }

        long usedMaterials = 0;
        for (CourseMaterialDto material : materials) {
            Long bookId = material.bookId();
            boolean hasUsage = lendingServiceClient.countByBookId(bookId) > 0 ||
                               reservationServiceClient.countByBookId(bookId) > 0;

            if (!hasUsage) {
                List<Long> contentIds = digitalContentServiceClient.getDigitalContentIdsByBook(bookId);
                if (!contentIds.isEmpty() && digitalContentServiceClient.countLogsByContents(contentIds) > 0) {
                    hasUsage = true;
                }
            }

            if (hasUsage) {
                usedMaterials++;
            }
        }

        double usageRate = (double) usedMaterials / total;

        return new CourseMaterialStats(courseId, course.courseCode(), total, mandatory, optional, usageRate);
    }

    public FacultyPerformance getFacultyPerformance(Long facultyId) {
        UserDto faculty = identityServiceClient.getUserById(facultyId);
        if (faculty == null) {
            throw new IllegalArgumentException("Faculty not found");
        }

        long books = bookServiceClient.countByFacultyId(facultyId);
        long courses = courseServiceClient.countByFacultyId(facultyId);
        
        List<BookDto> facultyBooks = bookServiceClient.getBooksByFaculty(facultyId);
        long engagement = facultyBooks.stream()
                .mapToLong(b -> lendingServiceClient.countByBookId(b.id()))
                .sum();

        return new FacultyPerformance(facultyId, faculty.fullName(), books, courses, engagement);
    }

    public StudentEngagement getStudentEngagement(Long studentId) {
        UserDto student = identityServiceClient.getUserById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found");
        }

        long borrowed = lendingServiceClient.countByUserId(studentId);
        long digital = digitalContentServiceClient.countLogsByUser(studentId);
        long reservations = reservationServiceClient.countByUserId(studentId);

        return new StudentEngagement(studentId, student.fullName(), borrowed, digital, reservations);
    }

    public LibrarianStats getLibrarianStats() {
        long activeIssues = lendingServiceClient.countActive();
        long overdue = lendingServiceClient.countOverdue();
        long returnedToday = lendingServiceClient.countReturnedToday();
        long reservations = reservationServiceClient.countPending();

        return new LibrarianStats(activeIssues, overdue, returnedToday, reservations);
    }
}
