package com.scholario.royalty.controller;

import com.scholario.royalty.dto.RoyaltyPolicyInput;
import com.scholario.royalty.model.RoyaltyPolicy;
import com.scholario.royalty.model.RoyaltyRecord;
import com.scholario.royalty.service.RoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RoyaltyRestController {

    private final RoyaltyService royaltyService;

    @PostMapping("/policies")
    public ResponseEntity<RoyaltyPolicy> defineRoyaltyPolicy(@Valid @RequestBody RoyaltyPolicyInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(royaltyService.defineRoyaltyPolicy(input));
    }

    @PostMapping("/calculate")
    public ResponseEntity<RoyaltyRecord> calculateRoyalty(@RequestParam Long bookId, @RequestParam BigDecimal totalRevenue) {
        return ResponseEntity.ok(royaltyService.calculateRoyalty(bookId, totalRevenue));
    }

    @PostMapping("/distribute/{recordId}")
    public ResponseEntity<RoyaltyRecord> distributeRoyalty(@PathVariable Long recordId) {
        return ResponseEntity.ok(royaltyService.distributeRoyalty(recordId));
    }

    @GetMapping("/details/{bookId}")
    public ResponseEntity<List<RoyaltyRecord>> getRoyaltyDetails(@PathVariable Long bookId) {
        return ResponseEntity.ok(royaltyService.getRoyaltyDetails(bookId));
    }

    @GetMapping("/revenue/{bookId}")
    public ResponseEntity<BigDecimal> getRevenueByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(royaltyService.getRevenueByBook(bookId));
    }
}
