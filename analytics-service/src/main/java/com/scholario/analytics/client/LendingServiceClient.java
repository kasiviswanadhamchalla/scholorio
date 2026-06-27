package com.scholario.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lending-service", path = "/internal/lending")
public interface LendingServiceClient {

    @GetMapping("/count/by-book/{bookId}")
    long countByBookId(@PathVariable("bookId") Long bookId);

    @GetMapping("/count/by-user/{userId}")
    long countByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/count/active")
    long countActive();

    @GetMapping("/count/overdue")
    long countOverdue();

    @GetMapping("/count/returned-today")
    long countReturnedToday();
}
