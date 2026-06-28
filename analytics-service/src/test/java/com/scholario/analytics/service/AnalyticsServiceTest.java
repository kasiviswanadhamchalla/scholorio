package com.scholario.analytics.service;

import com.scholario.analytics.client.*;
import com.scholario.analytics.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;
    @Mock
    private LendingServiceClient lendingServiceClient;
    @Mock
    private ReservationServiceClient reservationServiceClient;
    @Mock
    private DigitalContentServiceClient digitalContentServiceClient;
    @Mock
    private CourseServiceClient courseServiceClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testGetBookUsageAnalytics_Success() {
        BookDto book = new BookDto(1L, "Book Title", "isbn", 10L, "PUBLISHED");
        when(bookServiceClient.getBookById(1L)).thenReturn(book);
        when(lendingServiceClient.countByBookId(1L)).thenReturn(5L);
        when(reservationServiceClient.countByBookId(1L)).thenReturn(3L);
        when(digitalContentServiceClient.getDigitalContentIdsByBook(1L)).thenReturn(List.of(100L));
        when(digitalContentServiceClient.countLogsByContents(List.of(100L))).thenReturn(10L);

        BookUsageAnalytics analytics = analyticsService.getBookUsageAnalytics(1L);

        assertNotNull(analytics);
        assertEquals(1L, analytics.bookId());
        assertEquals("Book Title", analytics.title());
        assertEquals(5L, analytics.totalIssues());
        assertEquals(3L, analytics.totalReservations());
        assertEquals(10L, analytics.digitalAccessCount());
    }

    @Test
    void testGetBookUsageAnalytics_NotFound() {
        when(bookServiceClient.getBookById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> analyticsService.getBookUsageAnalytics(1L));
    }

    @Test
    void testGetCourseMaterialStats_Success() {
        CourseDto course = new CourseDto(100L, "CS101", "Intro", "Desc", 10L);
        when(courseServiceClient.getCourseById(100L)).thenReturn(course);

        CourseMaterialDto mat1 = new CourseMaterialDto(1L, 100L, 1L, true, null, null); // mandatory
        CourseMaterialDto mat2 = new CourseMaterialDto(2L, 100L, 2L, false, null, null); // optional
        when(courseServiceClient.getMaterialsByCourse(100L)).thenReturn(List.of(mat1, mat2));

        // mat 1 has lending usage
        when(lendingServiceClient.countByBookId(1L)).thenReturn(1L);
        // mat 2 has digital content usage
        when(lendingServiceClient.countByBookId(2L)).thenReturn(0L);
        when(reservationServiceClient.countByBookId(2L)).thenReturn(0L);
        when(digitalContentServiceClient.getDigitalContentIdsByBook(2L)).thenReturn(List.of(200L));
        when(digitalContentServiceClient.countLogsByContents(List.of(200L))).thenReturn(5L);

        CourseMaterialStats stats = analyticsService.getCourseMaterialStats(100L);

        assertNotNull(stats);
        assertEquals(2, stats.totalMaterials());
        assertEquals(1, stats.mandatoryCount());
        assertEquals(1, stats.optionalCount());
        assertEquals(1.0, stats.averageUsageRate()); // both are used
    }

    @Test
    void testGetCourseMaterialStats_EmptyMaterials() {
        CourseDto course = new CourseDto(100L, "CS101", "Intro", "Desc", 10L);
        when(courseServiceClient.getCourseById(100L)).thenReturn(course);
        when(courseServiceClient.getMaterialsByCourse(100L)).thenReturn(Collections.emptyList());

        CourseMaterialStats stats = analyticsService.getCourseMaterialStats(100L);
        assertEquals(0, stats.totalMaterials());
        assertEquals(0.0, stats.averageUsageRate());
    }

    @Test
    void testGetFacultyPerformance() {
        UserDto faculty = new UserDto(10L, "u", "e", "Name", Collections.emptySet());
        when(identityServiceClient.getUserById(10L)).thenReturn(faculty);
        when(bookServiceClient.countByFacultyId(10L)).thenReturn(5L);
        when(courseServiceClient.countByFacultyId(10L)).thenReturn(2L);

        BookDto book = new BookDto(1L, "Book", "isbn", 10L, "PUBLISHED");
        when(bookServiceClient.getBooksByFaculty(10L)).thenReturn(List.of(book));
        when(lendingServiceClient.countByBookId(1L)).thenReturn(8L);

        FacultyPerformance performance = analyticsService.getFacultyPerformance(10L);

        assertNotNull(performance);
        assertEquals("Name", performance.facultyName());
        assertEquals(5L, performance.booksAuthored());
        assertEquals(2L, performance.coursesTaught());
        assertEquals(8L, performance.totalStudentEngagement());
    }

    @Test
    void testGetStudentEngagement() {
        UserDto student = new UserDto(10L, "u", "e", "Student Name", Collections.emptySet());
        when(identityServiceClient.getUserById(10L)).thenReturn(student);
        when(lendingServiceClient.countByUserId(10L)).thenReturn(4L);
        when(digitalContentServiceClient.countLogsByUser(10L)).thenReturn(12L);
        when(reservationServiceClient.countByUserId(10L)).thenReturn(2L);

        StudentEngagement engagement = analyticsService.getStudentEngagement(10L);

        assertNotNull(engagement);
        assertEquals("Student Name", engagement.studentName());
        assertEquals(4L, engagement.booksBorrowed());
        assertEquals(12L, engagement.digitalContentAccessed());
        assertEquals(2L, engagement.activeReservations());
    }

    @Test
    void testGetLibrarianStats() {
        when(lendingServiceClient.countActive()).thenReturn(15L);
        when(lendingServiceClient.countOverdue()).thenReturn(3L);
        when(lendingServiceClient.countReturnedToday()).thenReturn(8L);
        when(reservationServiceClient.countPending()).thenReturn(5L);

        LibrarianStats stats = analyticsService.getLibrarianStats();

        assertNotNull(stats);
        assertEquals(15L, stats.activeIssues());
        assertEquals(3L, stats.overdueIssues());
        assertEquals(8L, stats.returnedToday());
        assertEquals(5L, stats.activeReservations());
    }
}
