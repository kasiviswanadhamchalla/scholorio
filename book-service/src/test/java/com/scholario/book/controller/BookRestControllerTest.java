package com.scholario.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholario.book.dto.BookInput;
import com.scholario.book.dto.BookVersionInput;
import com.scholario.book.model.Book;
import com.scholario.book.model.Draft;
import com.scholario.book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookRestController bookRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookRestController).build();
    }

    @Test
    void testGetAllOrSearchBooks() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Title");
        when(bookService.searchBooks(any(), any())).thenReturn(List.of(book));

        mockMvc.perform(get("/")
                        .param("title", "Title")
                        .param("isbn", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Title"));
    }

    @Test
    void testGetBookById() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        when(bookService.getBookById(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Title");
        when(bookService.createBook(any(BookInput.class))).thenReturn(book);

        BookInput input = new BookInput("Title", "12345", "Desc");

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testUpdateBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("New Title");
        when(bookService.updateBook(eq(1L), any(BookInput.class))).thenReturn(book);

        BookInput input = new BookInput("New Title", "12345", "Desc");

        mockMvc.perform(put("/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));

        when(bookService.updateBook(eq(2L), any(BookInput.class))).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(put("/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.deleteBook(1L)).thenReturn(book);

        mockMvc.perform(delete("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        when(bookService.deleteBook(2L)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(delete("/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPublishBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.publishBook(1L)).thenReturn(book);

        mockMvc.perform(post("/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testArchiveBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.archiveBook(1L)).thenReturn(book);

        mockMvc.perform(post("/1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testVersionBook() throws Exception {
        Book book = new Book();
        book.setId(2L);
        when(bookService.versionBook(any(BookVersionInput.class))).thenReturn(book);

        BookVersionInput input = new BookVersionInput(1L, "54321", "Title", "Desc");

        mockMvc.perform(post("/version")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void testSubmitBookForReview() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.submitForReview(1L)).thenReturn(book);

        mockMvc.perform(post("/1/submit-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testResetBookToDraft() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.updateBookState(eq(1L), any(Draft.class))).thenReturn(book);

        mockMvc.perform(post("/1/reset-draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testExistsBook() throws Exception {
        Book book = new Book();
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book));
        mockMvc.perform(get("/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        when(bookService.getBookById(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/2/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
