# Scholario Technology Stack Inventory

This document provides a comprehensive overview of the technologies, frameworks, and library versions used across the Scholario multi-module project.

## Core Infrastructure

| Technology | Version | Scope |
| :--- | :--- | :--- |
| **Java (JDK)** | 25 (Toolchain) / 21 Features | Global |
| **Gradle** | 9.5.0 | Global |
| **Spring Boot** | 3.3.4 | Global (BOM Managed) |
| **Spring Dependency Management** | 1.1.6 | Global |
| **Spring Framework** | 6.2.x (via Boot 3.3.4) | Global |
| **Spring GraphQL** | 1.3.x (via Boot 3.3.4) | Global |
| **MySQL Connector/J** | Managed by Boot 3.3.4 | Runtime (Persistence) |
| **Hibernate** | Managed by Boot 3.3.4 | Runtime (Persistence) |

---

## Module-Specific Dependencies & Testing

| Module | Core Logic | GraphQL | Persistence | Unit Testing |
| :--- | :--- | :--- | :--- | :--- |
| **identity-service** | User/Role | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **book-service** | Book/State | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **course-service** | Course/Maps | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **lending-service** | Issue/Return| Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **identity-service** | JWT/Auth | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **reservation-service**| Reservation | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **approval-service** | Peer Review | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **royalty-service**| Royalty/Calc| Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **notification-service** | Real-time | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **analytics-service**    | Aggregation | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **digital-content-service**      | Digital/DRM | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **search-service**| Suggestions | Enabled | JPA/MySQL | **Yes (Service & Resolver)** |
| **audit-service**    | Security/Log| Enabled | JPA/MySQL | **Yes (Service & Resolver)** |

---

## Shared Technology Standards

- **Java Version:** Java 25 Toolchain targeting Java 21 language features (Virtual Threads, Pattern Matching, Sealed Classes).
- **Virtual Threads:** Enabled project-wide via `spring.threads.virtual.enabled=true`.
- **Database:** MySQL 8.4+ for production; H2 used for local testing across all modules.
- **Unit Testing:** JUnit 5 (JUnit Jupiter) and Mockito are used across all modules for comprehensive testing of services and GraphQL resolvers.
- **Lombok:** Used extensively for boilerplate reduction (Getters, Setters, Constructors, Builders).
- **JSON Persistence:** Sealed class hierarchies are persisted as JSON in MySQL using `@JdbcTypeCode(SqlTypes.JSON)`.
- **Inter-service Communication:** Handled via asynchronous Kafka events and synchronous Spring `HttpGraphQlClient` (GraphQL over WebClient) requests; REST endpoints and Feign clients are eliminated.
