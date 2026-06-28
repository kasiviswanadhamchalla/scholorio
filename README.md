# Scholario - Faculty Book & Academic Resource Management System

A comprehensive academic resource management system built with Spring Boot and REST APIs, designed for educational institutions to manage books, courses, digital content, and faculty resources.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Module Structure](#module-structure)
- [REST API Reference](#rest-api-reference)
- [Role-Based Access Control](#role-based-access-control)

## Overview

Scholario provides a complete solution for managing academic resources including:
- Book catalog management with state workflow (Draft → Review → Published → Archived)
- Course management with material assignments
- Digital content access with DRM enforcement
- Book lending and multi-level approval system
- Peer review and approval workflow
- Royalty management for faculty authors
- Usage analytics and reporting

## Technology Stack

### Core Technologies
| Technology | Version |
|-------------|---------|
| Java (JDK) | 25 |
| Spring Boot | 3.3.4 |
| Gradle | 9.5.0 |
| MySQL | 8.4+ |
| Hibernate | 6.x |

### Key Dependencies
- **Spring Boot Starter Web** - Web services support
- **Spring Boot Starter WebFlux** - WebClient for inter-service REST communications
- **Spring Boot Starter Security** - Authentication and authorization
- **Spring Boot Starter Data JPA** - Database access layer
- **Spring Boot Starter OAuth2 Resource Server** - JWT token validation
- **Auth0 Java JWT** (4.4.0) - JWT generation and validation
- **Lombok** (1.18.40) - Boilerplate reduction
- **H2 Database** (2.3.x) - In-memory database for testing
- **Axios** (1.7.9) - Frontend HTTP client

## Architecture

### Port & Service Mapping
* **catalog-service** (Port 8081) - Book Catalog & State Machine
* **lending-service** (Port 8082) - Lending & Approvals
* **member-service** (Port 8083) - User profiles, Departments & Admin Dashboard
* **notification-service** (Port 8084) - Alerts & User notifications
* **api-gateway** (Port 8090) - API Routing / Gateway base URL

### Design Standards
- **REST-First:** All business logic exposed via RESTful HTTP APIs.
- **Pure REST Communication:** Inter-service communication is handled via load-balanced `WebClient` requests.
- **Sealed Classes:** State machines for BookState, IssueState, ReviewStatus, ReservationStatus.
- **Event-Driven:** Spring ApplicationEventPublisher for cross-module communication.
- **JSON Column Persistence:** `@JdbcTypeCode(SqlTypes.JSON)` for state objects in MySQL.
- **Virtual Threads:** Enabled for improved concurrency (`spring.threads.virtual.enabled=true`).

## REST API Reference

### Catalog Service (`/api/catalog`)
- `GET /api/` - List/search books
- `GET /api/{id}` - View book details
- `GET /api/{id}/exists` - Verify book existence
- `POST /api/` - Create a book record (Draft)
- `PUT /api/{id}` - Update book details
- `DELETE /api/{id}` - Delete book (restricted to `SUPER_ADMIN`)
- `POST /api/{id}/publish` - Publish a book
- `POST /api/{id}/submit-review` - Submit a book for review

### Lending Service (`/api/lending`)
- `GET /api/queue` - Retrieve the lending/approval queue
- `POST /api/{id}/approve` - Approve request (Level 1: Assistant Librarian/Librarian; Level 2: Librarian/Super Admin. Checks: No Self-Approval)
- `POST /api/{id}/reject` - Reject request with reason
- `POST /api/{id}/escalate` - Escalate approval request level
- `POST /api/issue` - Issue a book
- `POST /api/return` - Return a book
- `POST /api/renew` - Renew a book
- `POST /api/request` - Place reservation request

### Member Service (`/api/member`)
- `GET /api/dashboard` - KPI summary dashboard
- `GET /api/reports` - Reporting statistics
- `GET /api/reports/export` - Export reports as CSV attachment
- `GET /api/profile` - View user profile
- `PUT /api/profile` - Update user profile
- `GET /api/users` - View all users
- `GET /api/users/{id}` - Fetch user by ID
- `GET /api/users/username/{username}` - Fetch user by username
- `POST /api/users/{userId}/assign-role` - Assign functional roles (restricted to `SUPER_ADMIN`)
- `POST /api/users/{facultyId}/link-department` - Link member to department (restricted to `SUPER_ADMIN`)

### Notification Service (`/api/notification`)
- `GET /api/notifications` - Retrieve list of notifications
- `POST /api/notifications/{id}/read` - Mark notification as read

## Role-Based Access Control

Scholario enforces four distinct roles:
1. **SUPER_ADMIN:** Oversees security, assigns roles, and manages departments.
2. **LIBRARIAN:** Manages book catalogs, handles level-2 approvals, and issues books.
3. **ASSISTANT_LIBRARIAN:** Intermediate role handling level-1 approval requests and circulation workflows.
4. **MEMBER:** Primary library user (formerly Student/Faculty) who searches, reviews, and borrows resources.
