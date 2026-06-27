package com.scholario.royalty.service;

import com.scholario.royalty.client.BookServiceClient;
import com.scholario.royalty.client.IdentityServiceClient;
import com.scholario.royalty.client.UserDto;
import com.scholario.royalty.dto.RoyaltyPolicyInput;
import com.scholario.royalty.model.RoyaltyPolicy;
import com.scholario.royalty.model.RoyaltyRecord;
import com.scholario.royalty.repository.RoyaltyPolicyRepository;
import com.scholario.royalty.repository.RoyaltyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoyaltyService {
    private final RoyaltyPolicyRepository policyRepository;
    private final RoyaltyRecordRepository recordRepository;
    private final BookServiceClient bookServiceClient;
    private final IdentityServiceClient identityServiceClient;

    public RoyaltyService(RoyaltyPolicyRepository policyRepository,
                          RoyaltyRecordRepository recordRepository,
                          BookServiceClient bookServiceClient,
                          IdentityServiceClient identityServiceClient) {
        this.policyRepository = policyRepository;
        this.recordRepository = recordRepository;
        this.bookServiceClient = bookServiceClient;
        this.identityServiceClient = identityServiceClient;
    }

    @Transactional
    public RoyaltyPolicy defineRoyaltyPolicy(RoyaltyPolicyInput input) {
        validateBookExists(input.bookId());
        validateFaculty(input.facultyId());
        validateRoyaltyPercentage(input.royaltyPercentage());

        RoyaltyPolicy policy = policyRepository.findByBookId(input.bookId())
                .orElse(new RoyaltyPolicy());

        policy.setBookId(input.bookId());
        policy.setFacultyId(input.facultyId());
        policy.setRoyaltyPercentage(input.royaltyPercentage());
        policy.setSharingModel(input.sharingModel());

        return policyRepository.save(policy);
    }

    @Transactional
    public RoyaltyRecord calculateRoyalty(Long bookId, BigDecimal totalRevenue) {
        validateBookExists(bookId);
        if (totalRevenue == null || totalRevenue.signum() < 0) {
            throw new IllegalArgumentException("Total revenue cannot be negative");
        }

        RoyaltyPolicy policy = policyRepository.findByBookId(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Royalty policy not found for book: " + bookId));

        // Formula: Royalty = Total Revenue x Royalty Percentage / 100
        BigDecimal royaltyAmount = totalRevenue.multiply(policy.getRoyaltyPercentage())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        RoyaltyRecord record = new RoyaltyRecord();
        record.setBookId(bookId);
        record.setFacultyId(policy.getFacultyId());
        record.setTotalRevenue(totalRevenue);
        record.setCalculatedRoyalty(royaltyAmount);
        record.setPayoutStatus("PENDING");

        return recordRepository.save(record);
    }

    @Transactional
    public RoyaltyRecord distributeRoyalty(Long recordId) {
        RoyaltyRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Royalty record not found: " + recordId));

        record.setPayoutStatus("COMPLETED");
        record.setDistributedAt(LocalDateTime.now());

        return recordRepository.save(record);
    }

    public List<RoyaltyRecord> getRoyaltyDetails(Long bookId) {
        return recordRepository.findByBookId(bookId);
    }

    public BigDecimal getRevenueByBook(Long bookId) {
        return recordRepository.findByBookId(bookId).stream()
                .map(RoyaltyRecord::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateBookExists(Long bookId) {
        Boolean exists = bookServiceClient.existsById(bookId);
        if (exists == null || !exists) {
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }
    }

    private void validateFaculty(Long facultyId) {
        UserDto faculty = identityServiceClient.getUserById(facultyId);
        if (faculty == null) {
            throw new IllegalArgumentException("User not found with id: " + facultyId);
        }
        if (!faculty.roles().contains("FACULTY")) {
            throw new IllegalArgumentException("User with id " + facultyId + " is not a Faculty member");
        }
    }

    private void validateRoyaltyPercentage(BigDecimal royaltyPercentage) {
        if (royaltyPercentage == null
                || royaltyPercentage.signum() < 0
                || royaltyPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Royalty percentage must be between 0 and 100");
        }
    }
}
