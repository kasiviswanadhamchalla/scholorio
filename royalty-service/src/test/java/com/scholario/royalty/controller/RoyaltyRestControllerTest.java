package com.scholario.royalty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholario.royalty.dto.RoyaltyPolicyInput;
import com.scholario.royalty.model.RoyaltyPolicy;
import com.scholario.royalty.model.RoyaltyRecord;
import com.scholario.royalty.service.RoyaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoyaltyRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoyaltyService royaltyService;

    @InjectMocks
    private RoyaltyRestController royaltyRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(royaltyRestController).build();
    }

    @Test
    void testDefineRoyaltyPolicy() throws Exception {
        RoyaltyPolicy policy = new RoyaltyPolicy();
        policy.setBookId(1L);
        when(royaltyService.defineRoyaltyPolicy(any(RoyaltyPolicyInput.class))).thenReturn(policy);

        RoyaltyPolicyInput input = new RoyaltyPolicyInput(1L, 10L, new BigDecimal("15.0"), java.util.Map.of("model", "SHARED"));

        mockMvc.perform(post("/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(1L));
    }

    @Test
    void testCalculateRoyalty() throws Exception {
        RoyaltyRecord record = new RoyaltyRecord();
        record.setId(100L);
        when(royaltyService.calculateRoyalty(1L, new BigDecimal("1000.0"))).thenReturn(record);

        mockMvc.perform(post("/calculate")
                        .param("bookId", "1")
                        .param("totalRevenue", "1000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testDistributeRoyalty() throws Exception {
        RoyaltyRecord record = new RoyaltyRecord();
        record.setId(100L);
        when(royaltyService.distributeRoyalty(100L)).thenReturn(record);

        mockMvc.perform(post("/distribute/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testGetRoyaltyDetails() throws Exception {
        RoyaltyRecord record = new RoyaltyRecord();
        record.setId(100L);
        when(royaltyService.getRoyaltyDetails(1L)).thenReturn(List.of(record));

        mockMvc.perform(get("/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void testGetRevenueByBook() throws Exception {
        when(royaltyService.getRevenueByBook(1L)).thenReturn(new BigDecimal("150.00"));

        mockMvc.perform(get("/revenue/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(150.00));
    }
}
