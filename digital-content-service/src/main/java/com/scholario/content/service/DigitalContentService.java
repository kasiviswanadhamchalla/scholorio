package com.scholario.content.service;

import com.scholario.content.client.BookServiceClient;
import com.scholario.content.client.IdentityServiceClient;
import com.scholario.content.dto.DigitalContentInput;
import com.scholario.content.model.ContentAccessLog;
import com.scholario.content.model.ContentAccessType;
import com.scholario.content.model.DigitalContent;
import com.scholario.content.model.UserContentAccess;
import com.scholario.content.repository.ContentAccessLogRepository;
import com.scholario.content.repository.DigitalContentRepository;
import com.scholario.content.repository.UserContentAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DigitalContentService {

    private final DigitalContentRepository digitalContentRepository;
    private final ContentAccessLogRepository contentAccessLogRepository;
    private final UserContentAccessRepository userContentAccessRepository;
    private final BookServiceClient bookServiceClient;
    private final IdentityServiceClient identityServiceClient;

    public DigitalContent getDigitalContent(Long id) {
        return digitalContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Digital content not found with id: " + id));
    }

    public List<ContentAccessLog> getAccessLogs(Long contentId) {
        if (contentId != null) {
            return contentAccessLogRepository.findByContentId(contentId);
        }
        return contentAccessLogRepository.findAll();
    }

    @Transactional
    public DigitalContent uploadDigitalContent(DigitalContentInput input) {
        validateBookExists(input.getBookId());

        DigitalContent content = DigitalContent.builder()
                .bookId(input.getBookId())
                .contentType(input.getContentType())
                .contentUrl(input.getContentUrl())
                .drmEnforced(input.isDrmEnforced())
                .build();
        return digitalContentRepository.save(content);
    }

    @Transactional
    public ContentAccessLog grantAccess(Long contentId, Long userId) {
        validateContentExists(contentId);
        validateUserExists(userId);

        if (userContentAccessRepository.existsByUserIdAndContentId(userId, contentId)) {
            throw new IllegalArgumentException("Access already granted for this user and content");
        }

        UserContentAccess access = UserContentAccess.builder()
                .userId(userId)
                .contentId(contentId)
                .build();
        userContentAccessRepository.save(access);

        return logAccess(contentId, userId, ContentAccessType.VIEW);
    }

    @Transactional
    public boolean revokeAccess(Long contentId, Long userId) {
        validateContentExists(contentId);
        validateUserExists(userId);

        userContentAccessRepository.deleteByUserIdAndContentId(userId, contentId);
        return true;
    }

    @Transactional
    public ContentAccessLog logAccess(Long contentId, Long userId, ContentAccessType type) {
        validateContentExists(contentId);
        validateUserExists(userId);

        ContentAccessLog log = ContentAccessLog.builder()
                .contentId(contentId)
                .userId(userId)
                .accessType(type)
                .build();
        return contentAccessLogRepository.save(log);
    }

    public boolean hasAccess(Long userId, Long contentId) {
        return userContentAccessRepository.existsByUserIdAndContentId(userId, contentId);
    }

    private void validateBookExists(Long bookId) {
        Boolean exists = bookServiceClient.existsById(bookId);
        if (exists == null || !exists) {
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }
    }

    private void validateContentExists(Long contentId) {
        if (!digitalContentRepository.existsById(contentId)) {
            throw new IllegalArgumentException("Digital content not found with id: " + contentId);
        }
    }

    private void validateUserExists(Long userId) {
        if (identityServiceClient.getUserById(userId) == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
    }

    public List<Long> getDigitalContentIdsByBook(Long bookId) {
        return digitalContentRepository.findByBookId(bookId).stream()
                .map(DigitalContent::getId)
                .toList();
    }

    public long countLogsByContents(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return 0;
        }
        return contentAccessLogRepository.countByContentIdIn(contentIds);
    }

    public long countLogsByUser(Long userId) {
        return contentAccessLogRepository.countByUserId(userId);
    }
}
