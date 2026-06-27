package com.scholario.book.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventProducer {

    private static final String TOPIC = "book-events";
    private final KafkaTemplate<String, BookEvent> kafkaTemplate;

    public void publishBookEvent(String eventType, Long bookId, String title, Long facultyId, String description) {
        BookEvent event = new BookEvent(eventType, bookId, title, facultyId, description);
        log.info("Publishing book event to Kafka: {} for book: {}", eventType, bookId);
        try {
            kafkaTemplate.send(TOPIC, bookId.toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish book event to Kafka for book: {}", bookId, e);
        }
    }
}
