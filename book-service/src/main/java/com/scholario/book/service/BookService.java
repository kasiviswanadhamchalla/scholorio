package com.scholario.book.service;

import com.scholario.book.client.IdentityServiceClient;
import com.scholario.book.client.UserDto;
import com.scholario.book.dto.BookInput;
import com.scholario.book.dto.BookVersionInput;
import com.scholario.book.event.BookEventProducer;
import com.scholario.book.model.*;
import com.scholario.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private static final String BOOK_NOT_FOUND = "Book not found with id: ";
    private final BookRepository bookRepository;
    private final IdentityServiceClient identityServiceClient;
    private final BookEventProducer bookEventProducer;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        } else if (principal instanceof String s) {
            return s;
        }
        return authentication.getName();
    }

    private UserDto getCurrentUser() {
        String username = getCurrentUsername();
        try {
            UserDto user = identityServiceClient.getUserByUsername(username);
            if (user == null) {
                throw new IllegalArgumentException("User not found in identity service: " + username);
            }
            return user;
        } catch (Exception e) {
            log.error("Failed to fetch user from identity-service via Feign", e);
            throw new IllegalStateException("Authentication service unavailable", e);
        }
    }

    // Queries

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> searchBooks(String title, String isbn) {
        if (title != null && isbn != null) {
            return bookRepository.findByTitleContainingIgnoreCaseAndIsbnContaining(title, isbn);
        } else if (title != null) {
            return bookRepository.findByTitleContainingIgnoreCase(title);
        } else if (isbn != null) {
            return bookRepository.findByIsbn(isbn)
                    .map(List::of)
                    .orElse(List.of());
        }
        return bookRepository.findAll();
    }

    public List<Book> getBooksByFaculty(Long facultyId) {
        return bookRepository.findByFacultyId(facultyId);
    }

    public List<Book> getBookVersions(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> bookRepository.findVersionsByParentBookId(
                        book.getParentBookId() != null ? book.getParentBookId() : bookId))
                .orElseGet(List::of);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Mutations

    public Book createBook(BookInput input) {
        UserDto faculty = getCurrentUser();

        if (!faculty.roles().contains(Role.LIBRARIAN) && 
            !faculty.roles().contains(Role.ASSISTANT_LIBRARIAN) && 
            !faculty.roles().contains(Role.SUPER_ADMIN)) {
            throw new IllegalArgumentException("Only users with Librarian, Assistant Librarian, or Super Admin roles can create books");
        }

        if (bookRepository.findByIsbn(input.isbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + input.isbn() + " already exists");
        }

        Book book = new Book();
        book.setTitle(input.title());
        book.setIsbn(input.isbn());
        book.setFacultyId(faculty.id());
        book.setDescription(input.description());
        book.setState(new Draft());
        book.setVersionNumber(1);

        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookInput input) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BOOK_NOT_FOUND + id));

        if (!(book.getState() instanceof Draft)) {
            throw new IllegalStateException("Cannot update book in " + book.getState().name() + " state");
        }

        if (input.title() != null) {
            book.setTitle(input.title());
        }
        if (input.description() != null) {
            book.setDescription(input.description());
        }

        return bookRepository.save(book);
    }

    public Book deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BOOK_NOT_FOUND + id));

        if (book.getState() instanceof Published) {
            throw new IllegalStateException("Cannot delete published book");
        }

        bookRepository.delete(book);
        return book;
    }

    public Book submitForReview(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BOOK_NOT_FOUND + id));

        BookState currentState = book.getState();
        BookState newState = new Review();

        if (currentState.canTransitionTo(newState)) {
            book.setState(newState);
        } else {
            throw new IllegalStateException("Cannot transition from " + currentState.name() + " to REVIEW");
        }

        return bookRepository.save(book);
    }

    public Book updateBookState(Long id, BookState newState) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BOOK_NOT_FOUND + id));

        BookState currentState = book.getState();
        if (currentState.canTransitionTo(newState)) {
            book.setState(newState);
        } else {
            throw new IllegalStateException("Cannot transition from " + currentState.name() + " to " + newState.name());
        }

        return bookRepository.save(book);
    }

    public Book publishBook(Long id) {
        Book book = updateBookState(id, new Published());
        
        // Publish event to Kafka
        bookEventProducer.publishBookEvent(
                "BOOK_PUBLISHED",
                book.getId(),
                book.getTitle(),
                book.getFacultyId(),
                book.getDescription()
        );
        
        return book;
    }

    public Book archiveBook(Long id) {
        return updateBookState(id, new Archived());
    }

    public Book versionBook(BookVersionInput input) {
        Book parentBook = bookRepository.findById(input.parentBookId())
                .orElseThrow(() -> new IllegalArgumentException("Parent book not found with id: " + input.parentBookId()));

        if (!(parentBook.getState() instanceof Published)) {
            throw new IllegalStateException("Can only version published books");
        }

        if (bookRepository.findByIsbn(input.isbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + input.isbn() + " already exists");
        }

        Book newVersion = new Book();
        newVersion.setTitle(input.title() != null ? input.title() : parentBook.getTitle());
        newVersion.setIsbn(input.isbn());
        newVersion.setFacultyId(parentBook.getFacultyId());
        newVersion.setDescription(input.description() != null ? input.description() : parentBook.getDescription());
        newVersion.setVersionNumber(parentBook.getVersionNumber() + 1);
        newVersion.setParentBookId(parentBook.getParentBookId() != null ? parentBook.getParentBookId() : parentBook.getId());
        newVersion.setState(new Draft());

        return bookRepository.save(newVersion);
    }

    public boolean existsById(Long id) {
        return bookRepository.existsById(id);
    }

    public long countByFacultyId(Long facultyId) {
        return bookRepository.countByFacultyId(facultyId);
    }

    public List<Book> getBooksByIds(List<Long> ids) {
        return bookRepository.findAllById(ids);
    }
}
