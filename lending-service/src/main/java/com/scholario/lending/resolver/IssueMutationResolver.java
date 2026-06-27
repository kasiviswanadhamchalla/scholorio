package com.scholario.lending.resolver;

import com.scholario.lending.dto.BulkIssueInput;
import com.scholario.lending.dto.IssueInput;
import com.scholario.lending.dto.ReturnInput;
import com.scholario.lending.dto.RenewInput;
import com.scholario.lending.model.IssueRecord;
import com.scholario.lending.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
public class IssueMutationResolver {

    private final IssueService issueService;

    public IssueMutationResolver(IssueService issueService) {
        this.issueService = issueService;
    }

    @MutationMapping
    public IssueRecord issueBook(@Valid @Argument IssueInput input) {
        return issueService.issueBook(input);
    }

    @MutationMapping
    public IssueRecord returnBook(@Valid @Argument ReturnInput input) {
        return issueService.returnBook(input);
    }

    @MutationMapping
    public IssueRecord renewBook(@Valid @Argument RenewInput input) {
        return issueService.renewBook(input);
    }

    @MutationMapping
    public List<IssueRecord> bulkIssueBooks(@Valid @Argument BulkIssueInput input) {
        return issueService.bulkIssueBooks(input);
    }
}
