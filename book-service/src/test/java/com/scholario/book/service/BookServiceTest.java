package com.scholario.book.service;

import com.scholario.book.client.IdentityServiceClient;
import com.scholario.book.client.UserDto;
import com.scholario.book.dto.BookInput;
import com.scholario.book.dto.BookVersionInput;
import com.scholario.book.event.BookEventProducer;
import com.scholario.book.model.*;
import com.scholario.book.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private IdentityServiceClient identityServiceClient;
    @Mock
    private BookEventProducer bookEventProducer;

    @InjectMocks
    private BookService bookService;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    private void mockAuthentication(String username, Object principal) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);
        if (principal instanceof String) {
            when(auth.getName()).thenReturn((String) principal);
        }
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testGetBookById() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.getBookById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());

        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(bookService.getBookById(2L).isPresent());
    }

    @Test
    void testSearchBooks() {
        Book b = new Book();
        b.setTitle("Test Book");
        b.setIsbn("12345");

        when(bookRepository.findByTitleContainingIgnoreCaseAndIsbnContaining("Test", "123")).thenReturn(List.of(b));
        List<Book> res1 = bookService.searchBooks("Test", "123");
        assertEquals(1, res1.size());

        when(bookRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(b));
        List<Book> res2 = bookService.searchBooks("Test", null);
        assertEquals(1, res2.size());

        when(bookRepository.findByIsbn("12345")).thenReturn(Optional.of(b));
        List<Book> res3 = bookService.searchBooks(null, "12345");
        assertEquals(1, res3.size());

        when(bookRepository.findByIsbn("empty")).thenReturn(Optional.empty());
        List<Book> res3Empty = bookService.searchBooks(null, "empty");
        assertTrue(res3Empty.isEmpty());

        when(bookRepository.findAll()).thenReturn(List.of(b));
        List<Book> res4 = bookService.searchBooks(null, null);
        assertEquals(1, res4.size());
    }

    @Test
    void testGetBooksByFaculty() {
        Book b = new Book();
        when(bookRepository.findByFacultyId(10L)).thenReturn(List.of(b));
        assertEquals(1, bookService.getBooksByFaculty(10L).size());
    }

    @Test
    void testGetBookVersions() {
        Book parent = new Book();
        parent.setId(1L);
        parent.setParentBookId(null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(bookRepository.findVersionsByParentBookId(1L)).thenReturn(List.of(parent));

        List<Book> versions = bookService.getBookVersions(1L);
        assertEquals(1, versions.size());

        Book child = new Book();
        child.setId(2L);
        child.setParentBookId(1L);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(child));
        when(bookRepository.findVersionsByParentBookId(1L)).thenReturn(List.of(parent, child));

        List<Book> versionsChild = bookService.getBookVersions(2L);
        assertEquals(2, versionsChild.size());

        when(bookRepository.findById(3L)).thenReturn(Optional.empty());
        assertTrue(bookService.getBookVersions(3L).isEmpty());
    }

    @Test
    void testGetAllBooks() {
        Book b = new Book();
        when(bookRepository.findAll()).thenReturn(List.of(b));
        assertEquals(1, bookService.getAllBooks().size());
    }

    @Test
    void testCreateBook_Success() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("librarianUser");
        mockAuthentication("librarianUser", jwt);

        UserDto userDto = new UserDto(100L, "librarianUser", "lib@test.com", "Librarian User", Set.of(Role.LIBRARIAN));
        when(identityServiceClient.getUserByUsername("librarianUser")).thenReturn(userDto);
        when(bookRepository.findByIsbn("12345")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookInput input = new BookInput("Title", "12345", "Desc");
        Book created = bookService.createBook(input);

        assertNotNull(created);
        assertEquals("Title", created.getTitle());
        assertEquals("12345", created.getIsbn());
        assertEquals(100L, created.getFacultyId());
        assertTrue(created.getState() instanceof Draft);
    }

    @Test
    void testCreateBook_InvalidRole() {
        mockAuthentication("memberUser", "memberUser");
        UserDto userDto = new UserDto(100L, "memberUser", "member@test.com", "Member User", Set.of(Role.MEMBER));
        when(identityServiceClient.getUserByUsername("memberUser")).thenReturn(userDto);

        BookInput input = new BookInput("Title", "12345", "Desc");
        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(input));
    }

    @Test
    void testCreateBook_DuplicateIsbn() {
        mockAuthentication("adminUser", "adminUser");
        UserDto userDto = new UserDto(100L, "adminUser", "admin@test.com", "Admin User", Set.of(Role.SUPER_ADMIN));
        when(identityServiceClient.getUserByUsername("adminUser")).thenReturn(userDto);
        when(bookRepository.findByIsbn("12345")).thenReturn(Optional.of(new Book()));

        BookInput input = new BookInput("Title", "12345", "Desc");
        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(input));
    }

    @Test
    void testUpdateBook_Success() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Draft());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookInput input = new BookInput("New Title", "12345", "New Desc");
        Book updated = bookService.updateBook(1L, input);

        assertEquals("New Title", updated.getTitle());
        assertEquals("New Desc", updated.getDescription());
    }

    @Test
    void testUpdateBook_NonDraftState() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Published());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookInput input = new BookInput("New Title", "12345", "New Desc");
        assertThrows(IllegalStateException.class, () -> bookService.updateBook(1L, input));
    }

    @Test
    void testDeleteBook_Success() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Draft());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book deleted = bookService.deleteBook(1L);
        assertNotNull(deleted);
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void testDeleteBook_Published() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Published());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class, () -> bookService.deleteBook(1L));
    }

    @Test
    void testSubmitForReview_Success() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Draft());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.submitForReview(1L);
        assertTrue(result.getState() instanceof Review);
    }

    @Test
    void testUpdateBookState_InvalidTransition() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Published());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class, () -> bookService.updateBookState(1L, new Draft()));
    }

    @Test
    void testPublishBook_Success() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Review());
        book.setTitle("Test Title");
        book.setFacultyId(10L);
        book.setDescription("Test Desc");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.publishBook(1L);
        assertTrue(result.getState() instanceof Published);
        verify(bookEventProducer).publishBookEvent("BOOK_PUBLISHED", 1L, "Test Title", 10L, "Test Desc");
    }

    @Test
    void testArchiveBook() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Published());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.archiveBook(1L);
        assertTrue(result.getState() instanceof Archived);
    }

    @Test
    void testVersionBook_Success() {
        Book parent = new Book();
        parent.setId(1L);
        parent.setState(new Published());
        parent.setVersionNumber(1);
        parent.setTitle("Parent Book");
        parent.setDescription("Parent Desc");
        parent.setFacultyId(10L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(bookRepository.findByIsbn("54321")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookVersionInput input = new BookVersionInput(1L, "New Version Title", "54321", "New Version Desc");
        Book version = bookService.versionBook(input);

        assertNotNull(version);
        assertEquals("New Version Title", version.getTitle());
        assertEquals("54321", version.getIsbn());
        assertEquals(2, version.getVersionNumber());
        assertEquals(1L, version.getParentBookId());
        assertTrue(version.getState() instanceof Draft);
    }

    @Test
    void testVersionBook_NonPublishedParent() {
        Book parent = new Book();
        parent.setId(1L);
        parent.setState(new Draft());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(parent));
        BookVersionInput input = new BookVersionInput(1L, "54321", "Title", "Desc");
        assertThrows(IllegalStateException.class, () -> bookService.versionBook(input));
    }

    @Test
    void testExistsById() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        assertTrue(bookService.existsById(1L));
    }

    @Test
    void testCountByFacultyId() {
        when(bookRepository.countByFacultyId(10L)).thenReturn(5L);
        assertEquals(5L, bookService.countByFacultyId(10L));
    }

    @Test
    void testGetBooksByIds() {
        List<Long> ids = List.of(1L, 2L);
        Book b1 = new Book();
        Book b2 = new Book();
        when(bookRepository.findAllById(ids)).thenReturn(List.of(b1, b2));
        assertEquals(2, bookService.getBooksByIds(ids).size());
    }

    @Test
    void testGetCurrentUser_AuthFailure() {
        SecurityContextHolder.setContext(mock(SecurityContext.class)); // Empty auth
        BookInput input = new BookInput("Title", "123", "Desc");
        assertThrows(IllegalStateException.class, () -> bookService.createBook(input));
    }

    @Test
    void testSubmitForReview_InvalidTransition() {
        Book book = new Book();
        book.setId(1L);
        book.setState(new Published()); // Published cannot transition to Review
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class, () -> bookService.submitForReview(1L));
    }

    @Test
    void testVersionBook_DuplicateIsbn() {
        Book parent = new Book();
        parent.setId(1L);
        parent.setState(new Published());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(bookRepository.findByIsbn("54321")).thenReturn(Optional.of(new Book()));

        BookVersionInput input = new BookVersionInput(1L, "New Title", "54321", "Desc");
        assertThrows(IllegalArgumentException.class, () -> bookService.versionBook(input));
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        mockAuthentication("testuser", "testuser");
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(null);

        BookInput input = new BookInput("Title", "123", "Desc");
        assertThrows(IllegalStateException.class, () -> bookService.createBook(input));
    }

    @Test
    void testGetCurrentUser_FeignError() {
        mockAuthentication("testuser", "testuser");
        when(identityServiceClient.getUserByUsername("testuser")).thenThrow(new RuntimeException("Feign error"));

        BookInput input = new BookInput("Title", "123", "Desc");
        assertThrows(IllegalStateException.class, () -> bookService.createBook(input));
    }

    @Test
    void testGetCurrentUser_JwtPrincipal() {
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        UserDto userDto = new UserDto(10L, "testuser", "test@test.com", "Test User", Set.of(Role.SUPER_ADMIN));
        when(identityServiceClient.getUserByUsername("testuser")).thenReturn(userDto);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookInput input = new BookInput("Title", "123", "Desc");
        Book created = bookService.createBook(input);
        assertNotNull(created);
    }

    @Test
    void testGetCurrentUser_OtherPrincipalName() {
        Object principalObj = new Object();
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principalObj);
        when(auth.getName()).thenReturn("othername");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        UserDto userDto = new UserDto(10L, "othername", "test@test.com", "Test User", Set.of(Role.SUPER_ADMIN));
        when(identityServiceClient.getUserByUsername("othername")).thenReturn(userDto);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookInput input = new BookInput("Title", "123", "Desc");
        Book created = bookService.createBook(input);
        assertNotNull(created);
    }
}
