package com.scholario.recommendation.resolver;

import com.scholario.recommendation.client.IdentityServiceClient;
import com.scholario.recommendation.client.UserDto;
import com.scholario.recommendation.dto.BookRecommendation;
import com.scholario.recommendation.dto.CourseMaterialSuggestion;
import com.scholario.recommendation.dto.DemandPrediction;
import com.scholario.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RecommendationResolver {

    private final RecommendationService recommendationService;
    private final IdentityServiceClient identityServiceClient;

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

    private Long getCurrentUserId() {
        String username = getCurrentUsername();
        try {
            UserDto user = identityServiceClient.getUserByUsername(username);
            if (user == null) {
                throw new IllegalArgumentException("User not found in identity service: " + username);
            }
            return user.id();
        } catch (Exception e) {
            log.error("Failed to fetch user from identity-service via Feign", e);
            throw new IllegalStateException("Authentication service unavailable", e);
        }
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT', 'LIBRARIAN')")
    public List<BookRecommendation> recommendBooks() {
        return recommendationService.recommendBooks(getCurrentUserId());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'LIBRARIAN', 'ADMIN')")
    public List<CourseMaterialSuggestion> suggestCourseMaterials(@Argument Long courseId) {
        return recommendationService.suggestCourseMaterials(courseId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public List<DemandPrediction> predictDemand() {
        return recommendationService.predictDemand();
    }
}
