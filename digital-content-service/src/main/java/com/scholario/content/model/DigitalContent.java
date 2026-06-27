package com.scholario.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "digital_contents", indexes = {
    @Index(name = "idx_digital_content_book", columnList = "book_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "content_url", nullable = false)
    private String contentUrl;

    @Column(name = "drm_enforced", nullable = false)
    private boolean drmEnforced;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
