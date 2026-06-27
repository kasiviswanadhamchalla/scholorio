package com.scholario.notification.dto;

import com.scholario.notification.model.Notification;

public record NotificationResponse(
        Long id,
        String type,
        String message,
        Long userId,
        Long relatedEntityId,
        boolean read,
        String createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getUserId(),
                notification.getRelatedEntityId(),
                notification.isRead(),
                notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null
        );
    }
}
