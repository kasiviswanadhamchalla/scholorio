package com.scholario.violation.service;

import com.scholario.violation.model.*;
import com.scholario.violation.repository.AccessLogRepository;
import com.scholario.violation.repository.ViolationReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViolationServiceTest {

    @Mock
    private AccessLogRepository accessLogRepository;
    @Mock
    private ViolationReportRepository violationReportRepository;

    @InjectMocks
    private ViolationService violationService;

    @Test
    void testLogAccess() {
        violationService.logAccess("testuser", "books", "READ", true, "127.0.0.1");
        verify(accessLogRepository).save(any(AccessLog.class));
    }

    @Test
    void testDetectUnauthorizedAccess() {
        Object[] suspiciousData = new Object[]{"hackerUser", 7L};
        List<Object[]> suspiciousList = new java.util.ArrayList<>();
        suspiciousList.add(suspiciousData);
        when(accessLogRepository.findUsersWithExcessiveDeniedAccess(any(LocalDateTime.class), eq(5L)))
                .thenReturn(suspiciousList);
        when(violationReportRepository.save(any(ViolationReport.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ViolationReport> reports = violationService.detectUnauthorizedAccess();

        assertEquals(1, reports.size());
        assertEquals("hackerUser", reports.get(0).getUsername());
        assertEquals(ViolationType.ACCESS_ABUSE, reports.get(0).getType());
        assertEquals(ViolationSeverity.HIGH, reports.get(0).getSeverity());
    }

    @Test
    void testAnalyzeUsagePatterns() {
        Object[] suspiciousData = new Object[]{"spamUser", 15L};
        List<Object[]> suspiciousList = new java.util.ArrayList<>();
        suspiciousList.add(suspiciousData);
        when(accessLogRepository.findUsersWithExcessiveActivity(any(LocalDateTime.class), eq(10L)))
                .thenReturn(suspiciousList);
        when(violationReportRepository.save(any(ViolationReport.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ViolationReport> reports = violationService.analyzeUsagePatterns();

        assertEquals(1, reports.size());
        assertEquals("spamUser", reports.get(0).getUsername());
        assertEquals(ViolationType.EXCESSIVE_USAGE, reports.get(0).getType());
        assertEquals(ViolationSeverity.MEDIUM, reports.get(0).getSeverity());
    }

    @Test
    void testGetViolationReports() {
        ViolationReport report = new ViolationReport();
        when(violationReportRepository.findByUsername("testuser")).thenReturn(List.of(report));
        assertEquals(1, violationService.getViolationReports("testuser").size());

        when(violationReportRepository.findAll()).thenReturn(List.of(report));
        assertEquals(1, violationService.getViolationReports(null).size());
    }
}
