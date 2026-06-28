package com.scholario.notification.controller;

import com.scholario.notification.client.IdentityServiceClient;
import com.scholario.notification.client.UserDto;
import com.scholario.notification.dto.NotificationResponse;
import com.scholario.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class NotificationRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;
    @Mock
    private IdentityServiceClient identityServiceClient;

    @InjectMocks
    private NotificationRestController notificationRestController;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationRestController).build();
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
    void testGetMyNotifications() throws Exception {
        mockAuthentication("testuser");
        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);

        NotificationResponse resp = new NotificationResponse(100L, "BOOK_ISSUED", "Msg", 10L, null, false, null);
        when(notificationService.getNotificationsByUser(10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetUnreadNotifications() throws Exception {
        mockAuthentication("testuser");
        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);

        NotificationResponse resp = new NotificationResponse(100L, "BOOK_ISSUED", "Msg", 10L, null, false, null);
        when(notificationService.getUnreadNotifications(10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetUnreadNotificationCount() throws Exception {
        mockAuthentication("testuser");
        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);

        when(notificationService.getUnreadCount(10L)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void testMarkAsRead() throws Exception {
        NotificationResponse resp = new NotificationResponse(100L, "BOOK_ISSUED", "Msg", 10L, null, true, null);
        when(notificationService.markAsRead(100L)).thenReturn(resp);

        mockMvc.perform(post("/api/notifications/100/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void testMarkAllAsRead() throws Exception {
        mockAuthentication("testuser");
        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Collections.emptySet());
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);

        mockMvc.perform(post("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetMyNotifications_Unauthenticated() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        assertThrows(Exception.class, () -> mockMvc.perform(get("/api/notifications")));
    }

    @Test
    void testGetMyNotifications_JwtPrincipal() throws Exception {
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

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyNotifications_UserNotFound() {
        mockAuthentication("testuser");
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(null);

        assertThrows(Exception.class, () -> mockMvc.perform(get("/api/notifications")));
    }

    @Test
    void testGetMyNotifications_FeignError() {
        mockAuthentication("testuser");
        when(identityServiceClient.getUserByUsername("testuser")).thenThrow(new RuntimeException("Feign error"));

        assertThrows(Exception.class, () -> mockMvc.perform(get("/api/notifications")));
    }

    @Test
    void testGetMyNotifications_OtherPrincipalName() throws Exception {
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

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());
    }
}
