package com.scholario.content.service;

import com.scholario.content.client.BookServiceClient;
import com.scholario.content.client.IdentityServiceClient;
import com.scholario.content.client.UserDto;
import com.scholario.content.dto.DigitalContentInput;
import com.scholario.content.model.ContentAccessLog;
import com.scholario.content.model.ContentAccessType;
import com.scholario.content.model.DigitalContent;
import com.scholario.content.model.UserContentAccess;
import com.scholario.content.repository.ContentAccessLogRepository;
import com.scholario.content.repository.DigitalContentRepository;
import com.scholario.content.repository.UserContentAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalContentServiceTest {

    @Mock
    private DigitalContentRepository digitalContentRepository;
    @Mock
    private ContentAccessLogRepository contentAccessLogRepository;
    @Mock
    private UserContentAccessRepository userContentAccessRepository;
    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;

    @InjectMocks
    private DigitalContentService digitalContentService;

    @Test
    void testGetDigitalContent_Success() {
        DigitalContent content = new DigitalContent();
        content.setId(100L);
        when(digitalContentRepository.findById(100L)).thenReturn(Optional.of(content));

        assertEquals(content, digitalContentService.getDigitalContent(100L));
    }

    @Test
    void testGetDigitalContent_NotFound() {
        when(digitalContentRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> digitalContentService.getDigitalContent(100L));
    }

    @Test
    void testGetAccessLogs() {
        ContentAccessLog log = new ContentAccessLog();
        when(contentAccessLogRepository.findByContentId(100L)).thenReturn(List.of(log));
        assertEquals(1, digitalContentService.getAccessLogs(100L).size());

        when(contentAccessLogRepository.findAll()).thenReturn(List.of(log));
        assertEquals(1, digitalContentService.getAccessLogs(null).size());
    }

    @Test
    void testUploadDigitalContent_Success() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(digitalContentRepository.save(any(DigitalContent.class))).thenAnswer(inv -> inv.getArgument(0));

        DigitalContentInput input = new DigitalContentInput();
        input.setBookId(1L);
        input.setContentType("pdf");
        input.setContentUrl("http://pdf");
        input.setDrmEnforced(true);
        DigitalContent created = digitalContentService.uploadDigitalContent(input);

        assertNotNull(created);
        assertEquals(1L, created.getBookId());
        assertEquals("pdf", created.getContentType());
        assertTrue(created.isDrmEnforced());
    }

    @Test
    void testUploadDigitalContent_BookNotFound() {
        when(bookServiceClient.existsById(1L)).thenReturn(false);
        DigitalContentInput input = new DigitalContentInput();
        input.setBookId(1L);
        input.setContentType("pdf");
        input.setContentUrl("http://pdf");
        input.setDrmEnforced(true);
        assertThrows(IllegalArgumentException.class, () -> digitalContentService.uploadDigitalContent(input));
    }

    @Test
    void testGrantAccess_Success() {
        when(digitalContentRepository.existsById(100L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(userContentAccessRepository.existsByUserIdAndContentId(10L, 100L)).thenReturn(false);
        when(contentAccessLogRepository.save(any(ContentAccessLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ContentAccessLog log = digitalContentService.grantAccess(100L, 10L);

        assertNotNull(log);
        assertEquals(100L, log.getContentId());
        assertEquals(10L, log.getUserId());
        assertEquals(ContentAccessType.VIEW, log.getAccessType());
        verify(userContentAccessRepository).save(any(UserContentAccess.class));
    }

    @Test
    void testGrantAccess_AlreadyGranted() {
        when(digitalContentRepository.existsById(100L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(userContentAccessRepository.existsByUserIdAndContentId(10L, 100L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> digitalContentService.grantAccess(100L, 10L));
    }

    @Test
    void testRevokeAccess() {
        when(digitalContentRepository.existsById(100L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));

        assertTrue(digitalContentService.revokeAccess(100L, 10L));
        verify(userContentAccessRepository).deleteByUserIdAndContentId(10L, 100L);
    }

    @Test
    void testLogAccess() {
        when(digitalContentRepository.existsById(100L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(contentAccessLogRepository.save(any(ContentAccessLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ContentAccessLog log = digitalContentService.logAccess(100L, 10L, ContentAccessType.DOWNLOAD);

        assertNotNull(log);
        assertEquals(ContentAccessType.DOWNLOAD, log.getAccessType());
    }

    @Test
    void testHasAccess() {
        when(userContentAccessRepository.existsByUserIdAndContentId(10L, 100L)).thenReturn(true);
        assertTrue(digitalContentService.hasAccess(10L, 100L));
    }

    @Test
    void testGetDigitalContentIdsByBook() {
        DigitalContent content = new DigitalContent();
        content.setId(100L);
        when(digitalContentRepository.findByBookId(1L)).thenReturn(List.of(content));

        assertEquals(List.of(100L), digitalContentService.getDigitalContentIdsByBook(1L));
    }

    @Test
    void testCountLogs() {
        when(contentAccessLogRepository.countByContentIdIn(List.of(100L))).thenReturn(5L);
        assertEquals(5L, digitalContentService.countLogsByContents(List.of(100L)));
        assertEquals(0L, digitalContentService.countLogsByContents(null));

        when(contentAccessLogRepository.countByUserId(10L)).thenReturn(3L);
        assertEquals(3L, digitalContentService.countLogsByUser(10L));
    }
}
