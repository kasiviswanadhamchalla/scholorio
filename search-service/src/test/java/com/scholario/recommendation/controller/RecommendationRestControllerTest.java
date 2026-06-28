package com.scholario.recommendation.controller;

import com.scholario.recommendation.dto.BookRecommendation;
import com.scholario.recommendation.dto.CourseMaterialSuggestion;
import com.scholario.recommendation.dto.DemandPrediction;
import com.scholario.recommendation.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecommendationRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationRestController recommendationRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recommendationRestController).build();
    }

    @Test
    void testRecommendBooks() throws Exception {
        BookRecommendation rec = new BookRecommendation(1L, "Book 1", "Reason", 0.9);
        when(recommendationService.recommendBooks(10L)).thenReturn(List.of(rec));

        mockMvc.perform(get("/recommend/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(1L));
    }

    @Test
    void testSuggestCourseMaterials() throws Exception {
        CourseMaterialSuggestion sug = new CourseMaterialSuggestion(1L, "Book 1", "Course 1", "Reason");
        when(recommendationService.suggestCourseMaterials(100L)).thenReturn(List.of(sug));

        mockMvc.perform(get("/suggest/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(1L));
    }

    @Test
    void testPredictDemand() throws Exception {
        DemandPrediction pred = new DemandPrediction(1L, "Book 1", 15, "LOW");
        when(recommendationService.predictDemand()).thenReturn(List.of(pred));

        mockMvc.perform(get("/predict"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(1L));
    }
}
