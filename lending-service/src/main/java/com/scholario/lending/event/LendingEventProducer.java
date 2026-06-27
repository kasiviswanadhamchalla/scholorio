package com.scholario.lending.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LendingEventProducer {

    private static final String TOPIC = "lending-events";
    private final KafkaTemplate<String, LendingEvent> kafkaTemplate;

    public void publishLendingEvent(String eventType, Long issueId, Long bookId, Long userId, String status) {
        LendingEvent event = new LendingEvent(eventType, issueId, bookId, userId, status);
        log.info("Publishing lending event to Kafka: {} for issue: {}", eventType, issueId);
        try {
            kafkaTemplate.send(TOPIC, issueId.toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish lending event to Kafka for issue: {}", issueId, e);
        }
    }
}
