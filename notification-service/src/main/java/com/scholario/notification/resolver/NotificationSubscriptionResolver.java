package com.scholario.notification.resolver;

import com.scholario.notification.model.Notification;
import com.scholario.notification.model.NotificationType;
import com.scholario.notification.service.NotificationPublisher;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationSubscriptionResolver {

    private final NotificationPublisher notificationPublisher;

    public NotificationSubscriptionResolver(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }

    @SubscriptionMapping
    public Publisher<Notification> bookPublished() {
        return notificationPublisher.getSubscription(NotificationType.BOOK_PUBLISHED);
    }

    @SubscriptionMapping
    public Publisher<Notification> bookIssued() {
        return notificationPublisher.getSubscription(NotificationType.BOOK_ISSUED);
    }

    @SubscriptionMapping
    public Publisher<Notification> dueDateReminder() {
        return notificationPublisher.getSubscription(NotificationType.DUE_DATE_REMINDER);
    }

    @SubscriptionMapping
    public Publisher<Notification> reservationAvailable() {
        return notificationPublisher.getSubscription(NotificationType.RESERVATION_AVAILABLE);
    }

    @SubscriptionMapping
    public Publisher<Notification> notificationsByType(@Argument String type) {
        try {
            NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
            return notificationPublisher.getSubscription(notificationType);
        } catch (IllegalArgumentException e) {
            return notificationPublisher.getSubscription(NotificationType.BOOK_PUBLISHED);
        }
    }
}
