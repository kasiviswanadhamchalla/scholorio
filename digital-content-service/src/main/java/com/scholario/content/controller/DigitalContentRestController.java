package com.scholario.content.controller;

import com.scholario.content.dto.DigitalContentInput;
import com.scholario.content.model.ContentAccessLog;
import com.scholario.content.model.ContentAccessType;
import com.scholario.content.model.DigitalContent;
import com.scholario.content.service.DigitalContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DigitalContentRestController {

    private final DigitalContentService digitalContentService;

    @GetMapping("/{id}")
    public ResponseEntity<DigitalContent> getDigitalContent(@PathVariable Long id) {
        return ResponseEntity.ok(digitalContentService.getDigitalContent(id));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ContentAccessLog>> getAccessLogs(@RequestParam(required = false) Long contentId) {
        return ResponseEntity.ok(digitalContentService.getAccessLogs(contentId));
    }

    @PostMapping("/upload")
    public ResponseEntity<DigitalContent> uploadDigitalContent(@Valid @RequestBody DigitalContentInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(digitalContentService.uploadDigitalContent(input));
    }

    @PostMapping("/{contentId}/grant/{userId}")
    public ResponseEntity<ContentAccessLog> grantAccess(@PathVariable Long contentId, @PathVariable Long userId) {
        return ResponseEntity.ok(digitalContentService.grantAccess(contentId, userId));
    }

    @PostMapping("/{contentId}/revoke/{userId}")
    public ResponseEntity<Boolean> revokeAccess(@PathVariable Long contentId, @PathVariable Long userId) {
        return ResponseEntity.ok(digitalContentService.revokeAccess(contentId, userId));
    }

    @PostMapping("/{contentId}/log/{userId}")
    public ResponseEntity<ContentAccessLog> logAccess(@PathVariable Long contentId, @PathVariable Long userId, @RequestParam ContentAccessType type) {
        return ResponseEntity.ok(digitalContentService.logAccess(contentId, userId, type));
    }

    @GetMapping("/{contentId}/check/{userId}")
    public ResponseEntity<Boolean> hasAccess(@PathVariable Long userId, @PathVariable Long contentId) {
        return ResponseEntity.ok(digitalContentService.hasAccess(userId, contentId));
    }

    @GetMapping("/book/{bookId}/ids")
    public ResponseEntity<List<Long>> getDigitalContentIdsByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(digitalContentService.getDigitalContentIdsByBook(bookId));
    }

    @PostMapping("/logs/count/by-contents")
    public ResponseEntity<Long> countLogsByContents(@RequestBody List<Long> contentIds) {
        return ResponseEntity.ok(digitalContentService.countLogsByContents(contentIds));
    }

    @GetMapping("/logs/count/by-user/{userId}")
    public ResponseEntity<Long> countLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(digitalContentService.countLogsByUser(userId));
    }
}
