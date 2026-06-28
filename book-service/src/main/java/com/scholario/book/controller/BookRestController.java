package com.scholario.book.controller;

import com.scholario.book.dto.BookInput;
import com.scholario.book.dto.BookVersionInput;
import com.scholario.book.model.Book;
import com.scholario.book.model.Draft;
import com.scholario.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public List<Book> getAllOrSearchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String isbn) {
        return bookService.searchBooks(title, isbn);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(input));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN')")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody BookInput input) {
        try {
            return ResponseEntity.ok(bookService.updateBook(id, input));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Book> deleteBook(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookService.deleteBook(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Book> publishBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.publishBook(id));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Book> archiveBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.archiveBook(id));
    }

    @PostMapping("/version")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Book> versionBook(@Valid @RequestBody BookVersionInput input) {
        return ResponseEntity.ok(bookService.versionBook(input));
    }

    @PostMapping("/{id}/submit-review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public ResponseEntity<Book> submitBookForReview(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.submitForReview(id));
    }

    @PostMapping("/{id}/reset-draft")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Book> resetBookToDraft(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.updateBookState(id, new Draft()));
    }

    @GetMapping("/{id}/exists")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LIBRARIAN', 'ASSISTANT_LIBRARIAN', 'MEMBER')")
    public ResponseEntity<Boolean> existsBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id).isPresent());
    }
}
