package com.scholario.recommendation.service;

import com.scholario.recommendation.client.*;
import com.scholario.recommendation.dto.BookRecommendation;
import com.scholario.recommendation.dto.CourseMaterialSuggestion;
import com.scholario.recommendation.dto.DemandPrediction;
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
class RecommendationServiceTest {

    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;
    @Mock
    private LendingServiceClient lendingServiceClient;
    @Mock
    private CourseServiceClient courseServiceClient;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void testRecommendBooks_Success() {
        UserDto user = new UserDto(10L, "u", "e", "User Name", Collections.emptySet());
        when(identityServiceClient.getUserById(10L)).thenReturn(user);

        IssueRecordDto issue = new IssueRecordDto(100L, 10L, 1L, null, null, null, "ISSUED"); // borrowed book 1
        when(lendingServiceClient.getIssuesByUser(10L)).thenReturn(List.of(issue));

        BookDto b1 = new BookDto(1L, "Book 1", "123", 100L, "PUBLISHED");
        BookDto b2 = new BookDto(2L, "Book 2", "456", 100L, "PUBLISHED");
        when(bookServiceClient.getAllBooks()).thenReturn(List.of(b1, b2));

        List<BookRecommendation> recs = recommendationService.recommendBooks(10L);

        assertEquals(1, recs.size());
        assertEquals(2L, recs.get(0).bookId());
        assertEquals("Book 2", recs.get(0).title());
    }

    @Test
    void testRecommendBooks_UserNotFound() {
        when(identityServiceClient.getUserById(10L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> recommendationService.recommendBooks(10L));
    }

    @Test
    void testSuggestCourseMaterials_Success() {
        CourseDto course = new CourseDto(100L, "CS101", "Course 1", "Desc", 10L); // faculty 10
        when(courseServiceClient.getCourseById(100L)).thenReturn(course);

        CourseMaterialDto material = new CourseMaterialDto(1L, 100L, 1L, true, null, null); // course 100, book 1 assigned
        when(courseServiceClient.getMaterialsByCourse(100L)).thenReturn(List.of(material));

        BookDto b1 = new BookDto(1L, "Book 1", "123", 10L, "PUBLISHED");
        BookDto b2 = new BookDto(2L, "Book 2", "456", 10L, "PUBLISHED");
        when(bookServiceClient.getBooksByFaculty(10L)).thenReturn(List.of(b1, b2));

        List<CourseMaterialSuggestion> suggestions = recommendationService.suggestCourseMaterials(100L);

        assertEquals(1, suggestions.size());
        assertEquals(2L, suggestions.get(0).bookId());
    }

    @Test
    void testSuggestCourseMaterials_CourseNotFound() {
        when(courseServiceClient.getCourseById(100L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> recommendationService.suggestCourseMaterials(100L));
    }

    @Test
    void testPredictDemand() {
        BookIssueCountDto bic1 = new BookIssueCountDto(1L, 10L);
        when(lendingServiceClient.getIssuesByBook()).thenReturn(List.of(bic1));

        BookDto b1 = new BookDto(1L, "Book 1", "123", 10L, "PUBLISHED");
        when(bookServiceClient.getAllBooks()).thenReturn(List.of(b1));

        List<DemandPrediction> predictions = recommendationService.predictDemand();

        assertEquals(1, predictions.size());
        assertEquals(1L, predictions.get(0).bookId());
        assertEquals(17, predictions.get(0).predictedDemandCount()); // 10 * 1.2 + 5 = 17
        assertEquals("LOW", predictions.get(0).riskLevel());
    }
}
