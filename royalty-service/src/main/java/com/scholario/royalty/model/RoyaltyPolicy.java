package com.scholario.royalty.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "royalty_policies", indexes = {
        @Index(name = "idx_royalty_book", columnList = "book_id"),
        @Index(name = "idx_royalty_faculty", columnList = "faculty_id")
})
public class RoyaltyPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false, unique = true)
    private Long bookId;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "royalty_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal royaltyPercentage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sharing_model")
    private Map<String, Object> sharingModel;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public BigDecimal getRoyaltyPercentage() {
        return royaltyPercentage;
    }

    public void setRoyaltyPercentage(BigDecimal royaltyPercentage) {
        this.royaltyPercentage = royaltyPercentage;
    }

    public Map<String, Object> getSharingModel() {
        return sharingModel;
    }

    public void setSharingModel(Map<String, Object> sharingModel) {
        this.sharingModel = sharingModel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
