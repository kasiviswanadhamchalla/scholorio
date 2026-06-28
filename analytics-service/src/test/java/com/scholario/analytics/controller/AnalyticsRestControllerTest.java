package com.scholario.analytics.controller;

import com.scholario.analytics.dto.*;
import com.scholario.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsRestController analyticsRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsRestController).build();
    }

    @Test
    void testGetBookUsageAnalytics() throws Exception {
        BookUsageAnalytics analytics = new BookUsageAnalytics(1L, "Book", 5, 2, 10);
        when(analyticsService.getBookUsageAnalytics(1L)).thenReturn(analytics);

        mockMvc.perform(get("/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1L));
    }

    @Test
    void testGetCourseMaterialStats() throws Exception {
        CourseMaterialStats stats = new CourseMaterialStats(100L, "CS101", 5, 3, 2, 0.8);
        when(analyticsService.getCourseMaterialStats(100L)).thenReturn(stats);

        mockMvc.perform(get("/course/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(100L));
    }

    @Test
    void testGetFacultyPerformance() throws Exception {
        FacultyPerformance performance = new FacultyPerformance(10L, "Faculty", 5, 2, 15);
        when(analyticsService.getFacultyPerformance(10L)).thenReturn(performance);

        mockMvc.perform(get("/faculty/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facultyId").value(10L));
    }

    @Test
    void testGetStudentEngagement() throws Exception {
        StudentEngagement engagement = new StudentEngagement(10L, "Student", 4, 10, 2);
        when(analyticsService.getStudentEngagement(10L)).thenReturn(engagement);

        mockMvc.perform(get("/student/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(10L));
    }

    @Test
    void testGetLibrarianStats() throws Exception {
        LibrarianStats stats = new LibrarianStats(15, 3, 8, 5);
        when(analyticsService.getLibrarianStats()).thenReturn(stats);

        mockMvc.perform(get("/librarian"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeIssues").value(15));
    }
}
