package com.scholario.content.resolver;

import com.scholario.content.dto.DigitalContentInput;
import com.scholario.content.model.ContentAccessLog;
import com.scholario.content.model.DigitalContent;
import com.scholario.content.service.DigitalContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
public class DigitalContentResolver {

    private final DigitalContentService digitalContentService;

    @QueryMapping
    public DigitalContent getDigitalContent(@Argument Long id) {
        return digitalContentService.getDigitalContent(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public List<ContentAccessLog> getAccessLogs(@Argument Long contentId) {
        return digitalContentService.getAccessLogs(contentId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'LIBRARIAN', 'ADMIN')")
    public DigitalContent uploadDigitalContent(@Valid @Argument DigitalContentInput input) {
        return digitalContentService.uploadDigitalContent(input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ContentAccessLog grantAccess(@Argument Long contentId, @Argument Long userId) {
        return digitalContentService.grantAccess(contentId, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public boolean revokeAccess(@Argument Long contentId, @Argument Long userId) {
        return digitalContentService.revokeAccess(contentId, userId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'FACULTY')")
    public List<Long> getDigitalContentIdsByBook(@Argument Long bookId) {
        return digitalContentService.getDigitalContentIdsByBook(bookId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'FACULTY')")
    public int countLogsByContents(@Argument List<Long> contentIds) {
        return (int) digitalContentService.countLogsByContents(contentIds);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'FACULTY')")
    public int countLogsByUser(@Argument Long userId) {
        return (int) digitalContentService.countLogsByUser(userId);
    }
}
