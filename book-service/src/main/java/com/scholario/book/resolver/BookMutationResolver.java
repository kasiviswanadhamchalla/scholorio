package com.scholario.book.resolver;

import com.scholario.book.dto.BookInput;
import com.scholario.book.dto.BookVersionInput;
import com.scholario.book.model.Book;
import com.scholario.book.service.BookService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class BookMutationResolver {

    private final BookService bookService;

    public BookMutationResolver(BookService bookService) {
        this.bookService = bookService;
    }

    @MutationMapping
    @PreAuthorize("hasRole('FACULTY')")
    public Book createBook(@Valid @Argument BookInput input) {
        return bookService.createBook(input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public Book updateBook(@Argument Long id, @Valid @Argument BookInput input) {
        return bookService.updateBook(id, input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Book deleteBook(@Argument Long id) {
        return bookService.deleteBook(id);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'LIBRARIAN')")
    public Book publishBook(@Argument Long id) {
        return bookService.publishBook(id);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'LIBRARIAN')")
    public Book archiveBook(@Argument Long id) {
        return bookService.archiveBook(id);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public Book versionBook(@Valid @Argument BookVersionInput input) {
        return bookService.versionBook(input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'LIBRARIAN')")
    public Book submitBookForReview(@Argument Long id) {
        return bookService.submitForReview(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Book resetBookToDraft(@Argument Long id) {
        return bookService.updateBookState(id, new com.scholario.book.model.Draft());
    }
}
