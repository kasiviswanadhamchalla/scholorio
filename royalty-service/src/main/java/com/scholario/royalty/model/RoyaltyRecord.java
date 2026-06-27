package com.scholario.royalty.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "royalty_records", indexes = {
        @Index(name = "idx_royalty_record_book", columnList = "book_id"),
        @Index(name = "idx_royalty_record_faculty", columnList = "faculty_id"),
        @Index(name = "idx_royalty_record_status", columnList = "payout_status")
})
public class RoyaltyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "total_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "calculated_royalty", nullable = false, precision = 19, scale = 2)
    private BigDecimal calculatedRoyalty;

    @Column(name = "payout_status", nullable = false)
    private String payoutStatus;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "distributed_at")
    private LocalDateTime distributedAt;

    @PrePersist
    protected void onCalculate() {
        calculatedAt = LocalDateTime.now();
        if (payoutStatus == null) {
            payoutStatus = "PENDING";
        }
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

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getCalculatedRoyalty() {
        return calculatedRoyalty;
    }

    public void setCalculatedRoyalty(BigDecimal calculatedRoyalty) {
        this.calculatedRoyalty = calculatedRoyalty;
    }

    public String getPayoutStatus() {
        return payoutStatus;
    }

    public void setPayoutStatus(String payoutStatus) {
        this.payoutStatus = payoutStatus;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public LocalDateTime getDistributedAt() {
        return distributedAt;
    }

    public void setDistributedAt(LocalDateTime distributedAt) {
        this.distributedAt = distributedAt;
    }
}
