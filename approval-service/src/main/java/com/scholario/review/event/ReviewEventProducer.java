package com.scholario.review.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventProducer {

    private static final String TOPIC = "review-events";
    private final KafkaTemplate<String, ReviewEvent> kafkaTemplate;

    public void publishReviewEvent(String eventType, Long reviewRecordId, Long bookId, Long reviewerId, String status, String feedback) {
        ReviewEvent event = new ReviewEvent(eventType, reviewRecordId, bookId, reviewerId, status, feedback);
        log.info("Publishing review event to Kafka: {} for book: {}", eventType, bookId);
        try {
            kafkaTemplate.send(TOPIC, reviewRecordId.toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish review event to Kafka for book: {}", bookId, e);
        }
    }
}
