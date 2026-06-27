package com.scholario.violation.resolver;

import com.scholario.violation.model.ViolationReport;
import com.scholario.violation.service.ViolationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class MisuseQueryResolver {

    private final ViolationService violationService;

    public MisuseQueryResolver(ViolationService violationService) {
        this.violationService = violationService;
    }

    @QueryMapping
    public List<ViolationReport> detectUnauthorizedAccess() {
        return violationService.detectUnauthorizedAccess();
    }

    @QueryMapping
    public List<ViolationReport> analyzeUsagePatterns() {
        return violationService.analyzeUsagePatterns();
    }

    @QueryMapping
    public List<ViolationReport> getViolationReports(@Argument String username) {
        return violationService.getViolationReports(username);
    }
}
