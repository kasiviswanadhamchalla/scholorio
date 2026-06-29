package com.scholario.lending.controller;

import com.scholario.lending.dto.IssueInput;
import com.scholario.lending.dto.RenewInput;
import com.scholario.lending.dto.ReturnInput;
import com.scholario.lending.model.ApprovalRequest;
import com.scholario.lending.model.IssueRecord;
import com.scholario.lending.service.ApprovalService;
import com.scholario.lending.service.IssueService;
import com.scholario.lending.client.IdentityServiceClient;
import com.scholario.lending.client.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/", "", "/api", "/api/lending", "/lending"})
@RequiredArgsConstructor
@Slf4j
public class LendingRestController {

    private final IssueService issueService;
    private final ApprovalService approvalService;
    private final IdentityServiceClient identityServiceClient;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        } else if (principal instanceof String s) {
            return s;
        }
        return authentication.getName();
    }

    private Long getCurrentUserId() {
        String username = getCurrentUsername();
        try {
            UserDto user = identityServiceClient.getUserByUsername(username);
            if (user == null) {
                throw new IllegalArgumentException("User not found in identity service: " + username);
            }
            return user.id();
        } catch (Exception e) {
            log.error("Failed to fetch user from identity-service via Feign", e);
            throw new IllegalStateException("Authentication service unavailable", e);
        }
    }

    // Service 2: Workflow Endpoints
    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public List<ApprovalRequest> getApprovalQueue() {
        return approvalService.getQueue();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<ApprovalRequest> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String feedback = body != null ? body.getOrDefault("feedback", "Approved") : "Approved";
        try {
            return ResponseEntity.ok(approvalService.approveRequest(id, feedback));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<ApprovalRequest> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Rejected with no reason") : "Rejected";
        try {
            return ResponseEntity.ok(approvalService.rejectRequest(id, reason));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<ApprovalRequest> escalateRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(approvalService.escalateRequest(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Core lending operations
    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<IssueRecord> issueBook(@Valid @RequestBody IssueInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.issueBook(input));
    }

    @PostMapping("/return")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<IssueRecord> returnBook(@Valid @RequestBody ReturnInput input) {
        return ResponseEntity.ok(issueService.returnBook(input));
    }

    @PostMapping("/renew")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<IssueRecord> renewBook(@Valid @RequestBody RenewInput input) {
        return ResponseEntity.ok(issueService.renewBook(input));
    }

    // Request lending/reservation from member
    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public ResponseEntity<ApprovalRequest> requestBookLending(@RequestBody Map<String, Long> body) {
        Long bookId = body.get("bookId");
        Long userId = body.get("userId");
        if (bookId == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(approvalService.createLendingRequest(bookId, userId, getCurrentUsername()));
    }

    @GetMapping("/due-dates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public List<IssueRecord> getDueDates() {
        return issueService.getDueDates();
    }

    @GetMapping("/my-issued")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public List<IssueRecord> getMyIssued() {
        return issueService.getIssuedBooksByUser(getCurrentUserId());
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public List<IssueRecord> getMyHistory() {
        return issueService.getIssueHistory(getCurrentUserId());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public Map<String, Integer> getStats() {
        return Map.of(
                "activeIssues", (int) issueService.countActive(),
                "overdueIssues", (int) issueService.countOverdue(),
                "returnedToday", (int) issueService.countReturnedToday(),
                "activeReservations", approvalService.getQueue().size()
        );
    }
}
