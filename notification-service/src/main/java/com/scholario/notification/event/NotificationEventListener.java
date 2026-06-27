package com.scholario.notification.event;

import com.scholario.notification.model.Notification;
import com.scholario.notification.model.NotificationType;
import com.scholario.notification.service.NotificationPublisher;
import com.scholario.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;

    @KafkaListener(topics = "book-events", groupId = "notification-group")
    public void consumeBookEvent(Map<String, Object> event) {
        log.info("Received book event: {}", event);
        try {
            String eventType = (String) event.get("eventType");
            if ("BOOK_PUBLISHED".equals(eventType)) {
                Long bookId = ((Number) event.get("bookId")).longValue();
                String title = (String) event.get("title");
                Long facultyId = ((Number) event.get("facultyId")).longValue();
                
                Notification notification = notificationService.createNotification(
                        NotificationType.BOOK_PUBLISHED,
                        "New book published: " + title,
                        facultyId,
                        bookId
                );
                notificationPublisher.publish(notification);
            }
        } catch (Exception e) {
            log.error("Error processing book event", e);
        }
    }

    @KafkaListener(topics = "lending-events", groupId = "notification-group")
    public void consumeLendingEvent(Map<String, Object> event) {
        log.info("Received lending event: {}", event);
        try {
            String eventType = (String) event.get("eventType");
            Long recordId = ((Number) event.get("recordId")).longValue();
            Long bookId = ((Number) event.get("bookId")).longValue();
            Long userId = ((Number) event.get("userId")).longValue();

            if ("BOOK_ISSUED".equals(eventType)) {
                Notification notification = notificationService.createNotification(
                        NotificationType.BOOK_ISSUED,
                        "Book with ID " + bookId + " has been successfully issued to you.",
                        userId,
                        recordId
                );
                notificationPublisher.publish(notification);
            } else if ("BOOK_OVERDUE".equals(eventType)) {
                Notification notification = notificationService.createNotification(
                        NotificationType.DUE_DATE_REMINDER,
                        "Reminder: Book with ID " + bookId + " is overdue.",
                        userId,
                        recordId
                );
                notificationPublisher.publish(notification);
            }
        } catch (Exception e) {
            log.error("Error processing lending event", e);
        }
    }

    @KafkaListener(topics = "reservation-events", groupId = "notification-group")
    public void consumeReservationEvent(Map<String, Object> event) {
        log.info("Received reservation event: {}", event);
        try {
            String eventType = (String) event.get("eventType");
            Long reservationId = ((Number) event.get("reservationId")).longValue();
            Long bookId = ((Number) event.get("bookId")).longValue();
            Long userId = ((Number) event.get("userId")).longValue();

            if ("RESERVATION_ALLOCATED".equals(eventType)) {
                Notification notification = notificationService.createNotification(
                        NotificationType.RESERVATION_AVAILABLE,
                        "Reserved book with ID " + bookId + " is now available for pickup.",
                        userId,
                        reservationId
                );
                notificationPublisher.publish(notification);
            }
        } catch (Exception e) {
            log.error("Error processing reservation event", e);
        }
    }
}
