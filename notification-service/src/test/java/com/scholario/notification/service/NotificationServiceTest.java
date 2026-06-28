package com.scholario.notification.service;

import com.scholario.notification.dto.NotificationResponse;
import com.scholario.notification.model.Notification;
import com.scholario.notification.model.NotificationType;
import com.scholario.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification notification = notificationService.createNotification(NotificationType.BOOK_ISSUED, "Msg", 10L, 100L);

        assertNotNull(notification);
        assertEquals(NotificationType.BOOK_ISSUED, notification.getType());
        assertEquals("Msg", notification.getMessage());
        assertEquals(10L, notification.getUserId());
        assertEquals(100L, notification.getRelatedEntityId());
        assertFalse(notification.isRead());
    }

    @Test
    void testGetNotificationsByUser() {
        Notification n = new Notification();
        n.setType(NotificationType.BOOK_ISSUED);
        n.setMessage("Msg");
        n.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(n));

        List<NotificationResponse> list = notificationService.getNotificationsByUser(10L);
        assertEquals(1, list.size());
        assertEquals("Msg", list.get(0).message());
    }

    @Test
    void testGetUnreadNotifications() {
        Notification n = new Notification();
        n.setType(NotificationType.BOOK_ISSUED);
        n.setMessage("Msg");
        n.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(10L)).thenReturn(List.of(n));

        List<NotificationResponse> list = notificationService.getUnreadNotifications(10L);
        assertEquals(1, list.size());
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countUnreadByUserId(10L)).thenReturn(5L);
        assertEquals(5L, notificationService.getUnreadCount(10L));
    }

    @Test
    void testMarkAsRead_Success() {
        Notification n = new Notification();
        n.setId(100L);
        n.setRead(false);
        n.setType(NotificationType.BOOK_ISSUED);
        n.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findById(100L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse resp = notificationService.markAsRead(100L);
        assertTrue(n.isRead());
        assertTrue(resp.read());
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(100L));
    }

    @Test
    void testMarkAllAsRead() {
        Notification n1 = new Notification();
        n1.setRead(false);
        Notification n2 = new Notification();
        n2.setRead(false);

        List<Notification> unread = List.of(n1, n2);
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(10L)).thenReturn(unread);

        notificationService.markAllAsRead(10L);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository).saveAll(unread);
    }
}
