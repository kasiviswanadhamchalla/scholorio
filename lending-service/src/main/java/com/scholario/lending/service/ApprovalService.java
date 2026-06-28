package com.scholario.lending.service;

import com.scholario.lending.model.ApprovalRequest;
import com.scholario.lending.model.IssueRecord;
import com.scholario.lending.model.Issued;
import com.scholario.lending.model.Returned;
import com.scholario.lending.repository.ApprovalRequestRepository;
import com.scholario.lending.repository.IssueRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final IssueRecordRepository issueRecordRepository;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        } else if (principal instanceof String s) {
            return s;
        }
        return authentication.getName();
    }

    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    public List<ApprovalRequest> getQueue() {
        return approvalRequestRepository.findAll().stream()
                .filter(r -> "PENDING".equals(r.getStatus()) || "ESCALATED".equals(r.getStatus()))
                .toList();
    }

    public ApprovalRequest createLendingRequest(Long bookId, Long userId, String requestedByUsername) {
        // Create an IssueRecord in Requested state first
        IssueRecord issue = new IssueRecord();
        issue.setBookId(bookId);
        issue.setUserId(userId);
        issue.setIssueDate(LocalDateTime.now());
        issue.setDueDate(LocalDateTime.now().plusDays(14));
        issue.setStateType("REQUESTED");
        issue = issueRecordRepository.save(issue);

        ApprovalRequest request = new ApprovalRequest();
        request.setTargetId(issue.getId());
        request.setType("LENDING");
        request.setStatus("PENDING");
        request.setRequestedBy(requestedByUsername);
        request.setCurrentApprover("ASSISTANT_LIBRARIAN");
        request.setApprovalLevel(1);

        return approvalRequestRepository.save(request);
    }

    public ApprovalRequest approveRequest(Long approvalId, String feedback) {
        ApprovalRequest request = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + approvalId));

        String currentUsername = getCurrentUsername();
        
        // Rule: No self-approval
        if (request.getRequestedBy().equalsIgnoreCase(currentUsername)) {
            throw new IllegalStateException("Self-approval is not allowed. A requester cannot approve their own request.");
        }

        // Multi-level approval logic:
        // Level 1: Assistant Librarian (can approve to level 2)
        // Level 2: Librarian (can approve and finalize)
        // Super Admin can approve and finalize at any level
        if (request.getApprovalLevel() == 1) {
            if (hasRole("SUPER_ADMIN")) {
                // Super Admin can bypass directly to fully approved
                finalizeApproval(request);
            } else if (hasRole("ASSISTANT_LIBRARIAN") || hasRole("LIBRARIAN")) {
                // Advance to Level 2
                request.setApprovalLevel(2);
                request.setCurrentApprover("LIBRARIAN");
                log.info("Approval request {} approved at Level 1 by {}. Escalating to Level 2 (Librarian).", approvalId, currentUsername);
            } else {
                throw new IllegalStateException("Insufficient permissions to approve at level 1");
            }
        } else if (request.getApprovalLevel() == 2) {
            if (hasRole("LIBRARIAN") || hasRole("SUPER_ADMIN")) {
                finalizeApproval(request);
            } else {
                throw new IllegalStateException("Insufficient permissions to approve at level 2 (requires LIBRARIAN or SUPER_ADMIN)");
            }
        }

        return approvalRequestRepository.save(request);
    }

    private void finalizeApproval(ApprovalRequest request) {
        request.setStatus("APPROVED");
        request.setApprovalLevel(3);
        request.setCurrentApprover(null);

        // Update target issue record to ISSUED state
        IssueRecord issue = issueRecordRepository.findById(request.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("Issue record not found for approval request: " + request.getTargetId()));
        
        LocalDateTime now = LocalDateTime.now();
        issue.setIssueDate(now);
        issue.setDueDate(now.plusDays(14));
        issue.setState(new Issued(now, now.plusDays(14)));
        issueRecordRepository.save(issue);
        
        log.info("Approval request {} fully approved. Book issue activated.", request.getId());
    }

    public ApprovalRequest rejectRequest(Long approvalId, String reason) {
        ApprovalRequest request = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + approvalId));

        String currentUsername = getCurrentUsername();
        if (request.getRequestedBy().equalsIgnoreCase(currentUsername)) {
            throw new IllegalStateException("Self-rejection/action is not allowed.");
        }

        if (!hasRole("LIBRARIAN") && !hasRole("ASSISTANT_LIBRARIAN") && !hasRole("SUPER_ADMIN")) {
            throw new IllegalStateException("Insufficient permissions to reject request");
        }

        request.setStatus("REJECTED");
        request.setRejectReason(reason);
        request.setCurrentApprover(null);

        // Update target issue record to RETURNED / CANCELLED state
        IssueRecord issue = issueRecordRepository.findById(request.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("Issue record not found for approval request: " + request.getTargetId()));
        
        issue.setReturnDate(LocalDateTime.now());
        issue.setState(new Returned(LocalDateTime.now(), 0.0));
        issueRecordRepository.save(issue);

        log.info("Approval request {} rejected by {} with reason: {}", approvalId, currentUsername, reason);
        return approvalRequestRepository.save(request);
    }

    public ApprovalRequest escalateRequest(Long approvalId) {
        ApprovalRequest request = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + approvalId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be escalated");
        }

        request.setStatus("ESCALATED");
        // Automatically escalate level
        if (request.getApprovalLevel() == 1) {
            request.setApprovalLevel(2);
            request.setCurrentApprover("LIBRARIAN");
        } else if (request.getApprovalLevel() == 2) {
            request.setApprovalLevel(3);
            request.setCurrentApprover("SUPER_ADMIN");
        }

        log.info("Approval request {} escalated to level {} (approver: {})", approvalId, request.getApprovalLevel(), request.getCurrentApprover());
        return approvalRequestRepository.save(request);
    }
}
