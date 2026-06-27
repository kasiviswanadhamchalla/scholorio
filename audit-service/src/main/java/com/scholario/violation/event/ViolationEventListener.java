package com.scholario.violation.event;

import com.scholario.violation.service.ViolationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ViolationEventListener {

    private final ViolationService violationService;

    public ViolationEventListener(ViolationService violationService) {
        this.violationService = violationService;
    }

    @EventListener
    public void handleAccessDenied(AccessDeniedEvent event) {
        violationService.logAccess(
            event.username(),
            event.resource(),
            event.action(),
            false,
            event.clientIp()
        );
    }
}
