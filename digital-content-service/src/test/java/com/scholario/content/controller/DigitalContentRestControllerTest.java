package com.scholario.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholario.content.dto.DigitalContentInput;
import com.scholario.content.model.ContentAccessLog;
import com.scholario.content.model.ContentAccessType;
import com.scholario.content.model.DigitalContent;
import com.scholario.content.service.DigitalContentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DigitalContentRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DigitalContentService digitalContentService;

    @InjectMocks
    private DigitalContentRestController digitalContentRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(digitalContentRestController).build();
    }

    @Test
    void testGetDigitalContent() throws Exception {
        DigitalContent content = new DigitalContent();
        content.setId(100L);
        when(digitalContentService.getDigitalContent(100L)).thenReturn(content);

        mockMvc.perform(get("/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testGetAccessLogs() throws Exception {
        ContentAccessLog log = new ContentAccessLog();
        log.setId(500L);
        when(digitalContentService.getAccessLogs(100L)).thenReturn(List.of(log));

        mockMvc.perform(get("/logs").param("contentId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(500L));
    }

    @Test
    void testUploadDigitalContent() throws Exception {
        DigitalContent content = new DigitalContent();
        content.setId(100L);
        when(digitalContentService.uploadDigitalContent(any(DigitalContentInput.class))).thenReturn(content);

        DigitalContentInput input = new DigitalContentInput();
        input.setBookId(1L);
        input.setContentType("pdf");
        input.setContentUrl("url");
        input.setDrmEnforced(true);

        mockMvc.perform(post("/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testGrantAccess() throws Exception {
        ContentAccessLog log = new ContentAccessLog();
        log.setId(500L);
        when(digitalContentService.grantAccess(100L, 10L)).thenReturn(log);

        mockMvc.perform(post("/100/grant/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L));
    }

    @Test
    void testRevokeAccess() throws Exception {
        when(digitalContentService.revokeAccess(100L, 10L)).thenReturn(true);

        mockMvc.perform(post("/100/revoke/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testLogAccess() throws Exception {
        ContentAccessLog log = new ContentAccessLog();
        log.setId(500L);
        when(digitalContentService.logAccess(100L, 10L, ContentAccessType.VIEW)).thenReturn(log);

        mockMvc.perform(post("/100/log/10").param("type", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L));
    }

    @Test
    void testHasAccess() throws Exception {
        when(digitalContentService.hasAccess(10L, 100L)).thenReturn(true);

        mockMvc.perform(get("/100/check/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testGetDigitalContentIdsByBook() throws Exception {
        when(digitalContentService.getDigitalContentIdsByBook(1L)).thenReturn(List.of(100L, 101L));

        mockMvc.perform(get("/book/1/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(100L));
    }

    @Test
    void testCountLogsByContents() throws Exception {
        List<Long> ids = List.of(100L, 101L);
        when(digitalContentService.countLogsByContents(ids)).thenReturn(10L);

        mockMvc.perform(post("/logs/count/by-contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void testCountLogsByUser() throws Exception {
        when(digitalContentService.countLogsByUser(10L)).thenReturn(5L);

        mockMvc.perform(get("/logs/count/by-user/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
