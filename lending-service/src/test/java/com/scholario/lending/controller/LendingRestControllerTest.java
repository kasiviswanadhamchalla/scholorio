package com.scholario.lending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholario.lending.client.IdentityServiceClient;
import com.scholario.lending.client.UserDto;
import com.scholario.lending.dto.IssueInput;
import com.scholario.lending.dto.RenewInput;
import com.scholario.lending.dto.ReturnInput;
import com.scholario.lending.model.ApprovalRequest;
import com.scholario.lending.model.IssueRecord;
import com.scholario.lending.service.ApprovalService;
import com.scholario.lending.service.IssueService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class LendingRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IssueService issueService;
    @Mock
    private ApprovalService approvalService;
    @Mock
    private IdentityServiceClient identityServiceClient;

    @InjectMocks
    private LendingRestController lendingRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(lendingRestController).build();
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    private void mockAuthentication(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(username);
        when(auth.getName()).thenReturn(username);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testGetApprovalQueue() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        when(approvalService.getQueue()).thenReturn(List.of(request));

        mockMvc.perform(get("/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testApproveRequest() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        when(approvalService.approveRequest(1L, "Feedback")).thenReturn(request);

        Map<String, String> body = Map.of("feedback", "Feedback");

        mockMvc.perform(post("/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testApproveRequest_Forbidden() throws Exception {
        when(approvalService.approveRequest(1L, "Feedback")).thenThrow(new IllegalStateException("Self-approval forbidden"));

        Map<String, String> body = Map.of("feedback", "Feedback");

        mockMvc.perform(post("/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testApproveRequest_NotFound() throws Exception {
        when(approvalService.approveRequest(1L, "Feedback")).thenThrow(new IllegalArgumentException("Not found"));

        Map<String, String> body = Map.of("feedback", "Feedback");

        mockMvc.perform(post("/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRejectRequest() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        when(approvalService.rejectRequest(1L, "Reason")).thenReturn(request);

        Map<String, String> body = Map.of("reason", "Reason");

        mockMvc.perform(post("/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testRejectRequest_Forbidden() throws Exception {
        when(approvalService.rejectRequest(1L, "Reason")).thenThrow(new IllegalStateException("Self-rejection forbidden"));

        Map<String, String> body = Map.of("reason", "Reason");

        mockMvc.perform(post("/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRejectRequest_NotFound() throws Exception {
        when(approvalService.rejectRequest(1L, "Reason")).thenThrow(new IllegalArgumentException("Not found"));

        Map<String, String> body = Map.of("reason", "Reason");

        mockMvc.perform(post("/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEscalateRequest() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        when(approvalService.escalateRequest(1L)).thenReturn(request);

        mockMvc.perform(post("/1/escalate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        when(approvalService.escalateRequest(2L)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(post("/2/escalate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testIssueBook() throws Exception {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        when(issueService.issueBook(any(IssueInput.class))).thenReturn(record);

        IssueInput input = new IssueInput(1L, 10L);

        mockMvc.perform(post("/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testReturnBook() throws Exception {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        when(issueService.returnBook(any(ReturnInput.class))).thenReturn(record);

        ReturnInput input = new ReturnInput(100L, 10L);

        mockMvc.perform(post("/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testRenewBook() throws Exception {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        when(issueService.renewBook(any(RenewInput.class))).thenReturn(record);

        RenewInput input = new RenewInput(100L, 10L);

        mockMvc.perform(post("/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testRequestBookLending() throws Exception {
        mockAuthentication("testuser");
        ApprovalRequest request = new ApprovalRequest();
        request.setId(1L);
        when(approvalService.createLendingRequest(1L, 10L, "testuser")).thenReturn(request);

        Map<String, Long> body = Map.of("bookId", 1L, "userId", 10L);

        mockMvc.perform(post("/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        mockMvc.perform(post("/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bookId", 1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetDueDates() throws Exception {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        when(issueService.getDueDates()).thenReturn(List.of(record));

        mockMvc.perform(get("/due-dates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetMyIssued() throws Exception {
        mockAuthentication("testuser");
        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);

        IssueRecord record = new IssueRecord();
        record.setId(100L);
        when(issueService.getIssuedBooksByUser(10L)).thenReturn(List.of(record));

        mockMvc.perform(get("/my-issued"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetMyHistory() throws Exception {
        mockAuthentication("testuser");
        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);

        IssueRecord record = new IssueRecord();
        record.setId(100L);
        when(issueService.getIssueHistory(10L)).thenReturn(List.of(record));

        mockMvc.perform(get("/my-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetStats() throws Exception {
        when(issueService.countActive()).thenReturn(10L);
        when(issueService.countOverdue()).thenReturn(2L);
        when(issueService.countReturnedToday()).thenReturn(5L);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeIssues").value(10))
                .andExpect(jsonPath("$.overdueIssues").value(2))
                .andExpect(jsonPath("$.returnedToday").value(5));
    }

    @Test
    void testGetMyHistory_Unauthenticated() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        assertThrows(Exception.class, () -> mockMvc.perform(get("/my-history")));
    }

    @Test
    void testGetMyHistory_JwtPrincipal() throws Exception {
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);

        mockMvc.perform(get("/my-history"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyHistory_UserNotFound() {
        mockAuthentication("testuser");
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(null);

        assertThrows(Exception.class, () -> mockMvc.perform(get("/my-history")));
    }

    @Test
    void testGetMyHistory_FeignError() {
        mockAuthentication("testuser");
        when(identityServiceClient.getUserByUsername("testuser")).thenThrow(new RuntimeException("Feign error"));

        assertThrows(Exception.class, () -> mockMvc.perform(get("/my-history")));
    }

    @Test
    void testGetMyHistory_OtherPrincipalName() throws Exception {
        Object principalObj = new Object();
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principalObj);
        when(auth.getName()).thenReturn("othername");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        UserDto userDto = new UserDto(10L, "othername", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("othername")).thenReturn(userDto);

        mockMvc.perform(get("/my-history"))
                .andExpect(status().isOk());
    }
}
