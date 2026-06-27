package com.scholario.violation.event;

public record AccessDeniedEvent(
    String username,
    String resource,
    String action,
    String clientIp
) {}
