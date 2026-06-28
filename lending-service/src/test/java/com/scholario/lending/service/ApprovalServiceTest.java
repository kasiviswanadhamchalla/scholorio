package com.scholario.lending.service;

import com.scholario.lending.model.ApprovalRequest;
import com.scholario.lending.model.IssueRecord;
import com.scholario.lending.model.Issued;
import com.scholario.lending.model.Returned;
import com.scholario.lending.repository.ApprovalRequestRepository;
import com.scholario.lending.repository.IssueRecordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ApprovalServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;
    @Mock
    private IssueRecordRepository issueRecordRepository;

    @InjectMocks
    private ApprovalService approvalService;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    private void mockAuthentication(String username, List<String> roles) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(username);
        
        List<GrantedAuthority> authorities = roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .map(g -> (GrantedAuthority) g)
                .toList();
        doReturn(authorities).when(auth).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testGetQueue() {
        ApprovalRequest r1 = new ApprovalRequest();
        r1.setStatus("PENDING");
        ApprovalRequest r2 = new ApprovalRequest();
        r2.setStatus("ESCALATED");
        ApprovalRequest r3 = new ApprovalRequest();
        r3.setStatus("APPROVED");

        when(approvalRequestRepository.findAll()).thenReturn(List.of(r1, r2, r3));
        List<ApprovalRequest> queue = approvalService.getQueue();
        assertEquals(2, queue.size());
    }

    @Test
    void testCreateLendingRequest() {
        IssueRecord issue = new IssueRecord();
        issue.setId(100L);
        when(issueRecordRepository.save(any(IssueRecord.class))).thenReturn(issue);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalRequest request = approvalService.createLendingRequest(1L, 10L, "requester");

        assertNotNull(request);
        assertEquals(100L, request.getTargetId());
        assertEquals("LENDING", request.getType());
        assertEquals("PENDING", request.getStatus());
        assertEquals("requester", request.getRequestedBy());
        assertEquals("ASSISTANT_LIBRARIAN", request.getCurrentApprover());
        assertEquals(1, request.getApprovalLevel());
    }

    @Test
    void testApproveRequest_SelfApprovalForbidden() {
        ApprovalRequest request = new ApprovalRequest();
        request.setRequestedBy("requesterUser");
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        mockAuthentication("requesterUser", List.of("LIBRARIAN"));

        assertThrows(IllegalStateException.class, () -> approvalService.approveRequest(1L, "ok"));
    }

    @Test
    void testApproveRequest_Level1ToLevel2() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requesterUser");
        request.setApprovalLevel(1);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        mockAuthentication("approverUser", List.of("ASSISTANT_LIBRARIAN"));

        ApprovalRequest result = approvalService.approveRequest(1L, "Looks good");

        assertEquals(2, result.getApprovalLevel());
        assertEquals("LIBRARIAN", result.getCurrentApprover());
    }

    @Test
    void testApproveRequest_Level1BySuperAdmin() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requesterUser");
        request.setApprovalLevel(1);
        request.setTargetId(100L);

        IssueRecord issue = new IssueRecord();
        issue.setId(100L);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(issueRecordRepository.findById(100L)).thenReturn(Optional.of(issue));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        mockAuthentication("approverUser", List.of("SUPER_ADMIN"));

        ApprovalRequest result = approvalService.approveRequest(1L, "By Super Admin");

        assertEquals("APPROVED", result.getStatus());
        assertEquals(3, result.getApprovalLevel());
        assertNull(result.getCurrentApprover());
        assertTrue(issue.getState() instanceof Issued);
    }

    @Test
    void testApproveRequest_Level2Success() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requesterUser");
        request.setApprovalLevel(2);
        request.setTargetId(100L);

        IssueRecord issue = new IssueRecord();
        issue.setId(100L);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(issueRecordRepository.findById(100L)).thenReturn(Optional.of(issue));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        mockAuthentication("approverUser", List.of("LIBRARIAN"));

        ApprovalRequest result = approvalService.approveRequest(1L, "Approved fully");

        assertEquals("APPROVED", result.getStatus());
        assertTrue(issue.getState() instanceof Issued);
    }

    @Test
    void testApproveRequest_Level2InsufficientPermissions() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requesterUser");
        request.setApprovalLevel(2);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        mockAuthentication("approverUser", List.of("ASSISTANT_LIBRARIAN"));

        assertThrows(IllegalStateException.class, () -> approvalService.approveRequest(1L, "Try"));
    }

    @Test
    void testRejectRequest_Success() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requesterUser");
        request.setTargetId(100L);

        IssueRecord issue = new IssueRecord();
        issue.setId(100L);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(issueRecordRepository.findById(100L)).thenReturn(Optional.of(issue));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        mockAuthentication("rejecterUser", List.of("LIBRARIAN"));

        ApprovalRequest result = approvalService.rejectRequest(1L, "No stock");

        assertEquals("REJECTED", result.getStatus());
        assertEquals("No stock", result.getRejectReason());
        assertNull(result.getCurrentApprover());
        assertTrue(issue.getState() instanceof Returned);
    }

    @Test
    void testRejectRequest_SelfActionForbidden() {
        ApprovalRequest request = new ApprovalRequest();
        request.setRequestedBy("requesterUser");
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        mockAuthentication("requesterUser", List.of("LIBRARIAN"));

        assertThrows(IllegalStateException.class, () -> approvalService.rejectRequest(1L, "reason"));
    }

    @Test
    void testRejectRequest_InsufficientPermissions() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requesterUser");
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        mockAuthentication("rejecterUser", List.of("MEMBER"));

        assertThrows(IllegalStateException.class, () -> approvalService.rejectRequest(1L, "reason"));
    }

    @Test
    void testEscalateRequest_Success() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setStatus("PENDING");
        request.setApprovalLevel(1);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalRequest result = approvalService.escalateRequest(1L);

        assertEquals("ESCALATED", result.getStatus());
        assertEquals(2, result.getApprovalLevel());
        assertEquals("LIBRARIAN", result.getCurrentApprover());

        request.setStatus("PENDING");
        request.setApprovalLevel(2);
        ApprovalRequest result2 = approvalService.escalateRequest(1L);
        assertEquals(3, result2.getApprovalLevel());
        assertEquals("SUPER_ADMIN", result2.getCurrentApprover());
    }

    @Test
    void testEscalateRequest_NonPending() {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setStatus("APPROVED");

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () -> approvalService.escalateRequest(1L));
    }

    @Test
    void testApproveRequest_NotFound() {
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> approvalService.approveRequest(1L, "ok"));
    }

    @Test
    void testApproveRequest_InsufficientPermissionsLevel1() {
        // Mock authentication to have a different role, e.g. MEMBER
        mockAuthentication("approver", List.of("ROLE_MEMBER"));

        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requester");
        request.setApprovalLevel(1);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class, () -> approvalService.approveRequest(1L, "ok"));
    }

    @Test
    void testApproveRequest_InsufficientPermissionsLevel2() {
        // Mock authentication to have ASSISTANT_LIBRARIAN role, but level 2 requires LIBRARIAN
        mockAuthentication("approver", List.of("ROLE_ASSISTANT_LIBRARIAN"));

        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        request.setRequestedBy("requester");
        request.setApprovalLevel(2);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class, () -> approvalService.approveRequest(1L, "ok"));
    }

    @Test
    void testGetCurrentUsername_Anonymous() {
        SecurityContextHolder.getContext().setAuthentication(null);
        // Test createLendingRequest which will trigger getCurrentUsername() if we approved, 
        // but let's test it via approveRequest
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> approvalService.approveRequest(1L, "feedback"));
    }

    @Test
    void testRejectRequest_NotFound() {
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> approvalService.rejectRequest(1L, "reject"));
    }

    @Test
    void testEscalateRequest_NotFound() {
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> approvalService.escalateRequest(1L));
    }
}
