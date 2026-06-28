package com.scholario.violation.controller;

import com.scholario.violation.model.ViolationReport;
import com.scholario.violation.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ViolationRestController {

    private final ViolationService violationService;

    @PostMapping("/access-log")
    public ResponseEntity<Void> logAccess(
            @RequestParam String username,
            @RequestParam String resource,
            @RequestParam String action,
            @RequestParam boolean allowed,
            @RequestParam String clientIp) {
        violationService.logAccess(username, resource, action, allowed, clientIp);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detect-unauthorized")
    public ResponseEntity<List<ViolationReport>> detectUnauthorizedAccess() {
        return ResponseEntity.ok(violationService.detectUnauthorizedAccess());
    }

    @GetMapping("/analyze-patterns")
    public ResponseEntity<List<ViolationReport>> analyzeUsagePatterns() {
        return ResponseEntity.ok(violationService.analyzeUsagePatterns());
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ViolationReport>> getViolationReports(@RequestParam(required = false) String username) {
        return ResponseEntity.ok(violationService.getViolationReports(username));
    }
}
