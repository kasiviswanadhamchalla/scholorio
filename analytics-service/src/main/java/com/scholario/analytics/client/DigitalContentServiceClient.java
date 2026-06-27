package com.scholario.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "digital-content-service", path = "/internal/content")
public interface DigitalContentServiceClient {

    @GetMapping("/ids/by-book/{bookId}")
    List<Long> getDigitalContentIdsByBook(@PathVariable("bookId") Long bookId);

    @PostMapping("/logs/count/by-contents")
    long countLogsByContents(@RequestBody List<Long> contentIds);

    @GetMapping("/logs/count/by-user/{userId}")
    long countLogsByUser(@PathVariable("userId") Long userId);
}
