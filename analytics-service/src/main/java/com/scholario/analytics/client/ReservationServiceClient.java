package com.scholario.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reservation-service", path = "/internal/reservations")
public interface ReservationServiceClient {

    @GetMapping("/count/by-book/{bookId}")
    long countByBookId(@PathVariable("bookId") Long bookId);

    @GetMapping("/count/by-user/{userId}")
    long countByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/count/pending")
    long countPending();
}
