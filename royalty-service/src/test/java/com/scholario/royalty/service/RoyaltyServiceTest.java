package com.scholario.royalty.service;

import com.scholario.royalty.client.BookServiceClient;
import com.scholario.royalty.client.IdentityServiceClient;
import com.scholario.royalty.client.UserDto;
import com.scholario.royalty.dto.RoyaltyPolicyInput;
import com.scholario.royalty.model.RoyaltyPolicy;
import com.scholario.royalty.model.RoyaltyRecord;
import com.scholario.royalty.repository.RoyaltyPolicyRepository;
import com.scholario.royalty.repository.RoyaltyRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoyaltyServiceTest {

    @Mock
    private RoyaltyPolicyRepository policyRepository;
    @Mock
    private RoyaltyRecordRepository recordRepository;
    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;

    @InjectMocks
    private RoyaltyService royaltyService;

    @Test
    void testDefineRoyaltyPolicy_Success() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        UserDto faculty = new UserDto(10L, "f", "e", "Faculty Name", Set.of("FACULTY"));
        when(identityServiceClient.getUserById(10L)).thenReturn(faculty);
        when(policyRepository.findByBookId(1L)).thenReturn(Optional.empty());
        when(policyRepository.save(any(RoyaltyPolicy.class))).thenAnswer(inv -> inv.getArgument(0));

        RoyaltyPolicyInput input = new RoyaltyPolicyInput(1L, 10L, new BigDecimal("15.5"), java.util.Map.of("model", "SHARED"));
        RoyaltyPolicy policy = royaltyService.defineRoyaltyPolicy(input);

        assertNotNull(policy);
        assertEquals(1L, policy.getBookId());
        assertEquals(10L, policy.getFacultyId());
        assertEquals(new BigDecimal("15.5"), policy.getRoyaltyPercentage());
    }

    @Test
    void testDefineRoyaltyPolicy_BookNotFound() {
        when(bookServiceClient.existsById(1L)).thenReturn(false);
        RoyaltyPolicyInput input = new RoyaltyPolicyInput(1L, 10L, new BigDecimal("15.5"), java.util.Map.of("model", "SHARED"));
        assertThrows(IllegalArgumentException.class, () -> royaltyService.defineRoyaltyPolicy(input));
    }

    @Test
    void testDefineRoyaltyPolicy_UserNotFound() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(null);

        RoyaltyPolicyInput input = new RoyaltyPolicyInput(1L, 10L, new BigDecimal("15.5"), java.util.Map.of("model", "SHARED"));
        assertThrows(IllegalArgumentException.class, () -> royaltyService.defineRoyaltyPolicy(input));
    }

    @Test
    void testDefineRoyaltyPolicy_NotFaculty() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        UserDto nonFaculty = new UserDto(10L, "f", "e", "Non Faculty", Set.of("MEMBER"));
        when(identityServiceClient.getUserById(10L)).thenReturn(nonFaculty);

        RoyaltyPolicyInput input = new RoyaltyPolicyInput(1L, 10L, new BigDecimal("15.5"), java.util.Map.of("model", "SHARED"));
        assertThrows(IllegalArgumentException.class, () -> royaltyService.defineRoyaltyPolicy(input));
    }

    @Test
    void testDefineRoyaltyPolicy_InvalidPercentage() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        UserDto faculty = new UserDto(10L, "f", "e", "Faculty Name", Set.of("FACULTY"));
        when(identityServiceClient.getUserById(10L)).thenReturn(faculty);

        RoyaltyPolicyInput inputHigh = new RoyaltyPolicyInput(1L, 10L, new BigDecimal("105.0"), java.util.Map.of("model", "SHARED"));
        assertThrows(IllegalArgumentException.class, () -> royaltyService.defineRoyaltyPolicy(inputHigh));

        RoyaltyPolicyInput inputNeg = new RoyaltyPolicyInput(1L, 10L, new BigDecimal("-1.0"), java.util.Map.of("model", "SHARED"));
        assertThrows(IllegalArgumentException.class, () -> royaltyService.defineRoyaltyPolicy(inputNeg));
    }

    @Test
    void testCalculateRoyalty_Success() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        RoyaltyPolicy policy = new RoyaltyPolicy();
        policy.setBookId(1L);
        policy.setFacultyId(10L);
        policy.setRoyaltyPercentage(new BigDecimal("10.0"));
        when(policyRepository.findByBookId(1L)).thenReturn(Optional.of(policy));
        when(recordRepository.save(any(RoyaltyRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        RoyaltyRecord record = royaltyService.calculateRoyalty(1L, new BigDecimal("1000.00"));

        assertNotNull(record);
        assertEquals(new BigDecimal("100.00"), record.getCalculatedRoyalty()); // 1000 * 10%
        assertEquals("PENDING", record.getPayoutStatus());
    }

    @Test
    void testCalculateRoyalty_NegativeRevenue() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> royaltyService.calculateRoyalty(1L, new BigDecimal("-100.00")));
    }

    @Test
    void testDistributeRoyalty_Success() {
        RoyaltyRecord record = new RoyaltyRecord();
        record.setId(100L);
        record.setPayoutStatus("PENDING");
        when(recordRepository.findById(100L)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(RoyaltyRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        RoyaltyRecord distributed = royaltyService.distributeRoyalty(100L);

        assertEquals("COMPLETED", distributed.getPayoutStatus());
        assertNotNull(distributed.getDistributedAt());
    }

    @Test
    void testGetRoyaltyDetailsAndRevenue() {
        RoyaltyRecord rec1 = new RoyaltyRecord();
        rec1.setTotalRevenue(new BigDecimal("500.00"));
        RoyaltyRecord rec2 = new RoyaltyRecord();
        rec2.setTotalRevenue(new BigDecimal("300.00"));

        when(recordRepository.findByBookId(1L)).thenReturn(List.of(rec1, rec2));

        assertEquals(2, royaltyService.getRoyaltyDetails(1L).size());
        assertEquals(new BigDecimal("800.00"), royaltyService.getRevenueByBook(1L));
    }
}
