package com.scholario.notification.service;

import com.scholario.notification.model.Notification;
import com.scholario.notification.model.NotificationType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class NotificationPublisherTest {

    private final NotificationPublisher publisher = new NotificationPublisher();

    @Test
    void testPublishAndSubscribe() {
        Notification notification = new Notification();
        notification.setId(100L);
        notification.setType(NotificationType.BOOK_ISSUED);
        notification.setMessage("Test Message");

        Flux<Notification> subscription = publisher.getSubscription(NotificationType.BOOK_ISSUED);

        StepVerifier.create(subscription)
                .then(() -> publisher.publish(notification))
                .expectNext(notification)
                .thenCancel()
                .verify();
    }

    @Test
    void testGetSubscription_NullType() {
        Flux<Notification> subscription = publisher.getSubscription(null);
        StepVerifier.create(subscription)
                .expectComplete()
                .verify();
    }

    @Test
    void testPublish_NullType() {
        Notification notification = new Notification();
        notification.setType(null);
        assertThrows(NullPointerException.class, () -> publisher.publish(notification));
    }
}
