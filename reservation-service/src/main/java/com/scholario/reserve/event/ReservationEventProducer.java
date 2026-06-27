package com.scholario.reserve.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventProducer {

    private static final String TOPIC = "reservation-events";
    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;

    public void publishReservationEvent(String eventType, Long reservationId, Long bookId, Long userId, String status) {
        ReservationEvent event = new ReservationEvent(eventType, reservationId, bookId, userId, status);
        log.info("Publishing reservation event to Kafka: {} for reservation: {}", eventType, reservationId);
        try {
            kafkaTemplate.send(TOPIC, reservationId.toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish reservation event to Kafka for reservation: {}", reservationId, e);
        }
    }
}
