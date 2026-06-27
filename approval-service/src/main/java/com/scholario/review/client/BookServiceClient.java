package com.scholario.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "book-service", path = "/internal/books")
public interface BookServiceClient {

    @PutMapping("/{id}/submit-review")
    void submitForReview(@PathVariable("id") Long id);

    @PutMapping("/{id}/publish")
    void publishBook(@PathVariable("id") Long id);

    @PutMapping("/{id}/archive")
    void archiveBook(@PathVariable("id") Long id);

    @PutMapping("/{id}/draft")
    void resetToDraft(@PathVariable("id") Long id);
}
