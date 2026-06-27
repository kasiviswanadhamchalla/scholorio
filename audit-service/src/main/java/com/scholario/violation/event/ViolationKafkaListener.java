package com.scholario.violation.event;

import com.scholario.violation.service.ViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViolationKafkaListener {

    private final ViolationService violationService;

    @KafkaListener(topics = "security-violations", groupId = "audit-group")
    public void consumeAccessDenied(Map<String, Object> event) {
        log.info("Received security violation event: {}", event);
        try {
            String username = (String) event.get("username");
            String resource = (String) event.get("resource");
            String action = (String) event.get("action");
            String clientIp = (String) event.get("clientIp");
            boolean allowed = event.containsKey("allowed") ? (Boolean) event.get("allowed") : false;

            violationService.logAccess(username, resource, action, allowed, clientIp);
        } catch (Exception e) {
            log.error("Error processing security violation event", e);
        }
    }
}
