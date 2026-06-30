package com.scholario.book.controller;

import com.scholario.book.model.Book;
import com.scholario.book.model.Draft;
import com.scholario.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/books")
@RequiredArgsConstructor
public class BookInternalRestController {

    private final BookService bookService;

    @PutMapping("/{id}/submit-review")
    public ResponseEntity<Void> submitForReview(@PathVariable Long id) {
        bookService.submitForReview(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<Void> publishBook(@PathVariable Long id) {
        bookService.publishBook(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archiveBook(@PathVariable Long id) {
        bookService.archiveBook(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/draft")
    public ResponseEntity<Void> resetToDraft(@PathVariable Long id) {
        bookService.updateBookState(id, new Draft());
        return ResponseEntity.ok().build();
    }
}
