package com.scholario.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/graphql")
@Slf4j
public class GraphQLGatewayController {

    private final WebClient.Builder webClientBuilder;

    public GraphQLGatewayController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map>> routeGraphQLRequest(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        String query = (String) requestBody.get("query");
        String serviceName = determineService(query);
        log.info("Routing GraphQL request to service: {} based on query preview: {}", serviceName, 
                query != null ? query.substring(0, Math.min(query.length(), 60)).replace("\n", " ") : "null");

        WebClient.RequestHeadersSpec<?> requestSpec = webClientBuilder.build()
                .post()
                .uri("http://" + serviceName + "/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody);

        if (authHeader != null) {
            requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, authHeader);
        }

        return requestSpec.retrieve()
                .toEntity(Map.class)
                .onErrorResume(e -> {
                    log.error("Error routing GraphQL request to service: {}", serviceName, e);
                    return Mono.just(ResponseEntity.status(500).body(Map.of(
                            "errors", java.util.List.of(Map.of(
                                    "message", "Gateway error forwarding request: " + e.getMessage()
                            ))
                    )));
                });
    }

    private String determineService(String query) {
        if (query == null) {
            return "book-service";
        }

        // Identity / User Service
        if (query.contains("registerUser") || query.contains("updateUserProfile") ||
                query.contains("assignRole") || query.contains("linkFacultyToDepartment") ||
                query.contains("getUserById") || query.contains("getMyProfile") ||
                query.contains("getFacultyList") || query.contains("getStudentList") ||
                query.contains("getUnassignedUsers") || query.contains("getDepartments") ||
                query.contains("login") || query.contains("refreshToken") || query.contains("logout") ||
                query.contains("getUserByUsername") || query.contains("existsUser")) {
            return "identity-service";
        }

        // Reservation Service
        if (query.contains("reserveBook") || query.contains("cancelReservation") ||
                query.contains("allocateReservedBook") || query.contains("getReservationQueue") ||
                query.contains("getUserReservations") || query.contains("countReservationByBook") ||
                query.contains("countReservationByUser") || query.contains("countPendingReservations")) {
            return "reservation-service";
        }

        // Course Service
        if (query.contains("createCourse") || query.contains("updateCourse") ||
                query.contains("assignBookToCourse") || query.contains("updateCourseMaterial") ||
                query.contains("removeBookFromCourse") || query.contains("getCourseById") ||
                query.contains("getCoursesByFaculty") || query.contains("getCourseMaterials") ||
                query.contains("getBooksByCourse") || query.contains("countCoursesByFaculty")) {
            return "course-service";
        }

        // Review Service
        if (query.contains("submitBookForReview") || query.contains("approveBook") ||
                query.contains("rejectBook") || query.contains("requestChanges") ||
                query.contains("getReviewStatus") || query.contains("getReviewHistory")) {
            return "approval-service";
        }

        // Lending Service
        if (query.contains("issueBook") || query.contains("returnBook") ||
                query.contains("renewBook") || query.contains("bulkIssueBooks") ||
                query.contains("getLendingHistory") || query.contains("getLendingRecordById") ||
                query.contains("calculateFine") || query.contains("getOverdueRecords") ||
                query.contains("getMyIssuedBooks") || query.contains("getMyIssueHistory") ||
                query.contains("getDueDates") || query.contains("countLendingByBook") ||
                query.contains("countLendingByUser") || query.contains("countActiveLending") ||
                query.contains("countOverdueLending") || query.contains("countReturnedTodayLending") ||
                query.contains("getIssuesByBook") || query.contains("getIssuesByUser")) {
            return "lending-service";
        }

        // Digital Content Service
        if (query.contains("getDigitalContent") || query.contains("getAccessLogs") ||
                query.contains("getDigitalContentIdsByBook") || query.contains("countLogsByContents") ||
                query.contains("countLogsByUser") || query.contains("uploadDigitalContent") ||
                query.contains("grantAccess") || query.contains("revokeAccess")) {
            return "digital-content-service";
        }

        // Royalty Service
        if (query.contains("getRoyaltyDetails") || query.contains("getRevenueByBook") ||
                query.contains("defineRoyaltyPolicy") || query.contains("calculateRoyalty") ||
                query.contains("distributeRoyalty")) {
            return "royalty-service";
        }

        // Notification Service
        if (query.contains("getMyNotifications") || query.contains("getUnreadNotifications") ||
                query.contains("getUnreadNotificationCount") || query.contains("markNotificationAsRead") ||
                query.contains("markAllNotificationsAsRead")) {
            return "notification-service";
        }

        // Search & Recommendation Service
        if (query.contains("recommendBooks") || query.contains("suggestCourseMaterials") ||
                query.contains("predictDemand")) {
            return "search-service";
        }

        // Audit Service (Violation Module)
        if (query.contains("detectUnauthorizedAccess") || query.contains("analyzeUsagePatterns") ||
                query.contains("getViolationReports")) {
            return "audit-service";
        }

        // Default to Book Service
        return "book-service";
    }
}
