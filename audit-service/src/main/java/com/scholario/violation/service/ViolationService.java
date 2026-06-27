package com.scholario.violation.service;

import com.scholario.violation.model.*;
import com.scholario.violation.repository.AccessLogRepository;
import com.scholario.violation.repository.ViolationReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ViolationService {

    private final AccessLogRepository accessLogRepository;
    private final ViolationReportRepository violationReportRepository;

    public ViolationService(AccessLogRepository accessLogRepository, ViolationReportRepository violationReportRepository) {
        this.accessLogRepository = accessLogRepository;
        this.violationReportRepository = violationReportRepository;
    }

    public void logAccess(String username, String resource, String action, boolean allowed, String clientIp) {
        AccessLog log = new AccessLog();
        log.setUsername(username);
        log.setResource(resource);
        log.setAction(action);
        log.setAllowed(allowed);
        log.setClientIp(clientIp);
        accessLogRepository.save(log);
    }

    public List<ViolationReport> detectUnauthorizedAccess() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long threshold = 5;
        List<Object[]> suspiciousUsers = accessLogRepository.findUsersWithExcessiveDeniedAccess(since, threshold);

        return suspiciousUsers.stream().map(data -> {
            String username = (String) data[0];
            Long count = (Long) data[1];

            ViolationReport report = new ViolationReport();
            report.setUsername(username);
            report.setType(ViolationType.ACCESS_ABUSE);
            report.setSeverity(ViolationSeverity.HIGH);
            report.setDescription("User has " + count + " denied access attempts in the last 24 hours.");
            return violationReportRepository.save(report);
        }).toList();
    }

    public List<ViolationReport> analyzeUsagePatterns() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        long threshold = 10;
        List<Object[]> suspiciousUsers = accessLogRepository.findUsersWithExcessiveActivity(since, threshold);

        return suspiciousUsers.stream().map(data -> {
            String username = (String) data[0];
            Long count = (Long) data[1];

            ViolationReport report = new ViolationReport();
            report.setUsername(username);
            report.setType(ViolationType.EXCESSIVE_USAGE);
            report.setSeverity(ViolationSeverity.MEDIUM);
            report.setDescription("User has " + count + " total access attempts in the last hour.");
            return violationReportRepository.save(report);
        }).toList();
    }

    public List<ViolationReport> getViolationReports(String username) {
        if (username != null) {
            return violationReportRepository.findByUsername(username);
        }
        return violationReportRepository.findAll();
    }
}
