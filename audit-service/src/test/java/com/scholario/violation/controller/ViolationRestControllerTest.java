package com.scholario.violation.controller;

import com.scholario.violation.model.ViolationReport;
import com.scholario.violation.service.ViolationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ViolationRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ViolationService violationService;

    @InjectMocks
    private ViolationRestController violationRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(violationRestController).build();
    }

    @Test
    void testLogAccess() throws Exception {
        mockMvc.perform(post("/access-log")
                        .param("username", "user")
                        .param("resource", "books")
                        .param("action", "READ")
                        .param("allowed", "true")
                        .param("clientIp", "127.0.0.1"))
                .andExpect(status().isOk());

        verify(violationService).logAccess("user", "books", "READ", true, "127.0.0.1");
    }

    @Test
    void testDetectUnauthorizedAccess() throws Exception {
        ViolationReport report = new ViolationReport();
        report.setId(100L);
        when(violationService.detectUnauthorizedAccess()).thenReturn(List.of(report));

        mockMvc.perform(get("/detect-unauthorized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testAnalyzeUsagePatterns() throws Exception {
        ViolationReport report = new ViolationReport();
        report.setId(100L);
        when(violationService.analyzeUsagePatterns()).thenReturn(List.of(report));

        mockMvc.perform(get("/analyze-patterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetViolationReports() throws Exception {
        ViolationReport report = new ViolationReport();
        report.setId(100L);
        when(violationService.getViolationReports("user")).thenReturn(List.of(report));

        mockMvc.perform(get("/reports").param("username", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }
}
