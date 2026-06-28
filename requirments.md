Smart Faculty Book & Academic Resource Management System Requirements

This document outlines the requirements and capabilities of the Scholario microservices system.

## Core Technology Stack
* **Java 21/25:** Standard Java 21+ features (virtual threads, records, sealed classes, pattern matching)
* **Spring Boot:** Backend application framework
* **Spring REST / WebClient:** Replaces the legacy GraphQL architecture with pure REST endpoints and inter-service communications
* **Gradle:** Multi-module build tool
* **OAuth 2.0 / Keycloak:** Authentication and authorization
* **MySQL / H2:** Persistent database layer
* **React + Vite + Axios:** Portal frontend (replacing Apollo Client)

## System Capabilities
The system simulates a university academic resource platform handling:
- Faculty-authored books & publications
- Course material management
- Book issuance & reservations
- Digital content access
- Multi-level lending approval workflows (with no self-approval checks and escalation logic)
- Licensing & royalties
- Analytics & academic insights (including KPI summary and CSV export)
- Security violation detection

## Architecture & Security
- **Multi-Module Layout:** Separated microservices (catalog, lending, member, notification, etc.) routed via a centralized API Gateway.
- **REST Endpoints:** Every action (creation, updates, lending requests, approvals, reports) is accessed using standard HTTP methods (GET, POST, PUT, DELETE) and JSON payloads.
- **Role-Based Access:** Standard user roles mapped to `SUPER_ADMIN`, `LIBRARIAN`, `ASSISTANT_LIBRARIAN`, and `MEMBER`.
- **Unit Testing:** JUnit 5 and Mockito are utilized across all modules to verify service logic and REST controller behavior.
