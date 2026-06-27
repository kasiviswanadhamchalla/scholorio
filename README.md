# Scholario - Faculty Book & Academic Resource Management System

A comprehensive academic resource management system built with Spring Boot and GraphQL, designed for educational institutions to manage books, courses, digital content, and faculty resources.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Module Structure](#module-structure)
- [API Documentation](#api-documentation)
- [Role-Based Access Control](#roles-based-access-control)
- [GraphQL Endpoint](#graphql-endpoint)

## Overview

Scholario provides a complete solution for managing academic resources including:
- Book management with versioning and state workflow (Draft → Review → Published → Archived)
- Course management with material assignments
- Digital content access with DRM enforcement
- Book lending and reservation system
- Peer review and approval workflow
- Royalty management for faculty authors
- Analytics and recommendations
- Security violation detection

## Technology Stack

### Core Technologies
| Technology | Version |
|-------------|---------|
| Java (JDK) | 25 |
| Spring Boot | 3.3.4 |
| Spring GraphQL | 1.3.x |
| Gradle | 9.5.0 |
| MySQL | 8.4+ |
| Hibernate | 6.x |

### Key Dependencies
- **Spring Boot Starter Web** - Web services support
- **Spring Boot Starter WebFlux** - WebClient and HttpGraphQlClient for inter-service GraphQL queries
- **Spring Boot Starter Security** - Authentication and authorization
- **Spring Boot Starter Data JPA** - Database access layer
- **Spring Boot Starter OAuth2 Resource Server** - JWT token validation
- **Spring Boot Starter WebSocket** - Real-time notifications
- **Auth0 Java JWT** (4.4.0) - JWT generation and validation
- **Lombok** (1.18.40) - Boilerplate reduction
- **H2 Database** (2.3.x) - In-memory database for testing

## Architecture

### Multi-Module Structure

Scholario follows a Gradle multi-module architecture with clear separation of concerns:

```
scholario (root)
├── identity-service         # JWT authentication & authorization
├── identity-service         # User management & roles
├── book-service         # Book management with state machine
├── course-service       # Course & material management
├── lending-service      # Book lending system
├── reservation-service      # Reservation system
├── approval-service       # Review & approval workflow
├── royalty-service      # Royalty calculations & payouts
├── digital-content-service      # Digital content & DRM
├── notification-service # WebSocket notifications
├── analytics-service    # Usage analytics & reporting
├── search-service # AI-driven recommendations
└── audit-service   # Security violation detection
```

### Design Patterns

- **GraphQL-First:** All business logic exposed via GraphQL APIs
- **Pure GraphQL Communication:** Inter-service communication is handled via load-balanced GraphQL queries using Spring's `HttpGraphQlClient` over Eureka, with all internal REST controllers and Feign clients eliminated.
- **Sealed Classes:** State machines for BookState, IssueState, ReviewStatus, ReservationStatus
- **Event-Driven:** Spring ApplicationEventPublisher for cross-module communication
- **JSON Column Persistence:** `@JdbcTypeCode(SqlTypes.JSON)` for state objects in MySQL
- **Virtual Threads:** Enabled for improved concurrency (`spring.threads.virtual.enabled=true`)
- **Global Exception Handling:** `@ControllerAdvice` with `@GraphQlExceptionHandler`

## Getting Started

### Prerequisites
- Java 25 JDK
- MySQL 8.4+ running on localhost:3306
- Gradle 9.5.0 (or use included wrapper)

### Database Setup
```sql
CREATE DATABASE IF NOT EXISTS scholario;
CREATE USER 'scholario'@'localhost' IDENTIFIED BY 'Scholario@123';
GRANT ALL PRIVILEGES ON scholario.* TO 'scholario'@'localhost';
FLUSH PRIVILEGES;
```

### Build & Run
```bash
# Clone the repository
git clone <repository-url>
cd scholario

# Build all modules
./gradlew build

# Run the application
./gradlew :identity-service:bootRun (and other services)
```

### Access Points
- **GraphQL Endpoint:** http://localhost:8090/graphql
- **GraphiQL UI:** http://localhost:8090/graphiql
- **H2 Console:** http://localhost:<service-port>/h2-console (e.g., 8081/h2-console for identity-service) (for testing)

### Running Tests
```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :book-service:test

# Run a single test class
./gradlew :identity-service:test --tests "com.scholario.auth.service.AuthServiceTest"
```

## Module Structure

Each module follows a consistent internal structure:
```
scholario-*-module/
├── src/main/java/com/scholario/*/
│   ├── dto/           # Request/response DTOs
│   ├── model/         # JPA entities and state sealed classes
│   ├── repository/    # Spring Data JPA repositories
│   ├── resolver/      # GraphQL query/mutation resolvers
│   ├── service/       # Business logic
│   └── config/        # Module-specific configuration
├── src/main/resources/graphql/*.graphqls  # GraphQL schema
├── src/test/java/     # Unit tests
└── build.gradle
```

## API Documentation

### GraphQL Endpoint
`http://localhost:8090/graphql`

### Authentication

#### Get JWT Token (Login)
**JWT Required**: NO (public endpoint)

```graphql
mutation {
  login(input: {
    username: "string",
    password: "string"
  }) {
    accessToken
    refreshToken
    tokenType
    expiresIn
    user {
      id
      username
      email
      fullName
      roles
    }
  }
}
```

**JSON Request**:
```json
{
  "query": "mutation Login($input: LoginInput!) { login(input: $input) { accessToken refreshToken tokenType expiresIn user { id username email fullName roles } } }",
  "variables": {
    "input": {
      "username": "johndoe",
      "password": "password123"
    }
  }
}
```

**Response**:
```json
{
  "data": {
    "login": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tokenType": "Bearer",
      "expiresIn": 3600,
      "user": {
        "id": "1",
        "username": "johndoe",
        "email": "john@example.com",
        "fullName": "John Doe",
        "roles": "FACULTY"
      }
    }
  }
}
```

#### Using JWT Token
Include the access token in the `Authorization` header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Refresh Token
**JWT Required**: NO (uses refresh token)

```graphql
mutation {
  refreshToken(refreshToken: "your-refresh-token")
}
```

#### Logout
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  logout
}
```

---

## User Management

### Register User
**JWT Required**: NO (public registration)

```graphql
mutation {
  registerUser(input: {
    username: "string",
    email: "string",
    fullName: "string",
    password: "string"
  }) {
    id
    username
    email
    fullName
    roles
  }
}
```

### Update User Profile
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  updateUserProfile(id: "1", input: {
    fullName: "string",
    email: "string"
  }) {
    id
    username
    email
    fullName
    roles
  }
}
```

### Assign Role (Admin Only)
**JWT Required**: YES (requires ADMIN roles)

```graphql
mutation {
  assignRole(userId: "1") {
    id
    username
    roles
  }
}
```

### Link Faculty to Department (Admin Only)
**JWT Required**: YES (requires ADMIN roles)

```graphql
mutation {
  linkFacultyToDepartment(facultyId: "1", departmentId: "1") {
    id
    username
    department {
      id
      name
      code
    }
  }
}
```

### Get User By ID
**JWT Required**: YES (authenticated user)

```graphql
query {
  getUserById(id: "1") {
    id
    username
    email
    fullName
    roles
    department {
      id
      name
      code
    }
  }
}
```

### Get Faculty List
**JWT Required**: YES (authenticated user)

```graphql
query {
  getFacultyList {
    id
    username
    fullName
    email
  }
}
```

### Get Student List
**JWT Required**: YES (authenticated user)

```graphql
query {
  getStudentList {
    id
    username
    fullName
    email
  }
}
```

### Get Departments
**JWT Required**: YES (authenticated user)

```graphql
query {
  getDepartments {
    id
    name
    code
  }
}
```

---

## Book Management

### Create Book (Faculty/Admin)
**JWT Required**: YES (requires FACULTY or ADMIN roles)

```graphql
mutation {
  createBook(input: {
    title: "string",
    isbn: "string",
    facultyId: "1",
    description: "string"
  }) {
    id
    title
    isbn
    facultyId
    description
    versionNumber
    state {
      type
    }
  }
}
```

### Update Book (Faculty/Admin)
**JWT Required**: YES (requires FACULTY or ADMIN roles)

```graphql
mutation {
  updateBook(id: "1", input: {
    title: "string",
    isbn: "string",
    facultyId: "1",
    description: "string"
  }) {
    id
    title
    isbn
    description
  }
}
```

### Delete Book (Admin Only)
**JWT Required**: YES (requires ADMIN roles)

```graphql
mutation {
  deleteBook(id: "1") {
    id
    title
  }
}
```

### Publish Book (Faculty/Admin/Librarian)
**JWT Required**: YES (requires FACULTY, ADMIN, or LIBRARIAN roles)

```graphql
mutation {
  publishBook(id: "1") {
    id
    title
    state {
      type
    }
  }
}
```

### Archive Book (Faculty/Admin/Librarian)
**JWT Required**: YES (requires FACULTY, ADMIN, or LIBRARIAN roles)

```graphql
mutation {
  archiveBook(id: "1") {
    id
    title
    state {
      type
    }
  }
}
```

### Create Book Version (Faculty/Admin)
**JWT Required**: YES (requires FACULTY or ADMIN roles)

```graphql
mutation {
  versionBook(input: {
    parentBookId: "1",
    title: "string",
    isbn: "string",
    description: "string"
  }) {
    id
    title
    isbn
    versionNumber
    parentBookId
  }
}
```

### Get Book By ID
**JWT Required**: YES (authenticated user)

```graphql
query {
  getBookById(id: "1") {
    id
    title
    isbn
    facultyId
    description
    versionNumber
    state {
      type
    }
  }
}
```

### Search Books
**JWT Required**: YES (authenticated user)

```graphql
query {
  searchBooks(title: "Spring", isbn: "978-1617291203") {
    id
    title
    isbn
    facultyId
  }
}
```

### Get Books By Faculty
**JWT Required**: YES (authenticated user)

```graphql
query {
  getBooksByFaculty(facultyId: "1") {
    id
    title
    isbn
    state {
      type
    }
  }
}
```

### Get Book Versions
**JWT Required**: YES (authenticated user)

```graphql
query {
  getBookVersions(bookId: "1") {
    id
    title
    versionNumber
    parentBookId
  }
}
```

---

## Book Review

### Submit Book For Review (Faculty/Admin)
**JWT Required**: YES (requires FACULTY or ADMIN roles)

```graphql
mutation {
  submitBookForReview(bookId: "1", reviewerId: "2") {
    id
    bookId
    reviewerId
    status
    feedback
    createdAt
  }
}
```

### Approve Book (Faculty/Admin)
**JWT Required**: YES (requires FACULTY or ADMIN roles)

```graphql
mutation {
  approveBook(requestId: "1", feedback: "Great work!") {
    id
    status
    feedback
    updatedAt
  }
}
```

### Reject Book (Faculty/Admin)
**JWT Required**: YES (requires FACULTY or ADMIN roles)

```graphql
mutation {
  rejectBook(requestId: "1", feedback: "Needs improvements") {
    id
    status
    feedback
  }
}
```

### Request Changes (Faculty/Admin)
**JWT Required**: YES (requires FACULTY or ADMIN roles)

```graphql
mutation {
  requestChanges(requestId: "1", feedback: "Please revise chapter 3") {
    id
    status
    feedback
  }
}
```

### Get Review Status
**JWT Required**: YES (authenticated user)

```graphql
query {
  getReviewStatus(bookId: "1") {
    id
    bookId
    reviewerId
    status
    feedback
    createdAt
    updatedAt
  }
}
```

### Get Review History
**JWT Required**: YES (authenticated user)

```graphql
query {
  getReviewHistory(bookId: "1") {
    id
    reviewRecordId
    status
    feedback
    performedBy
    timestamp
  }
}
```

---

## Course Management

### Create Course
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  createCourse(input: {
    courseCode: "CS101",
    title: "Introduction to Computer Science",
    description: "string",
    facultyId: "1"
  }) {
    id
    courseCode
    title
    description
    facultyId
  }
}
```

### Update Course
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  updateCourse(id: "1", input: {
    courseCode: "CS101",
    title: "Updated Title",
    description: "string",
    facultyId: "1"
  }) {
    id
    courseCode
    title
  }
}
```

### Get Course By ID
**JWT Required**: YES (authenticated user)

```graphql
query {
  getCourseById(id: "1") {
    id
    courseCode
    title
    description
    facultyId
  }
}
```

### Get Courses By Faculty
**JWT Required**: YES (authenticated user)

```graphql
query {
  getCoursesByFaculty(facultyId: "1") {
    id
    courseCode
    title
    description
  }
}
```

### Assign Book To Course
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  assignBookToCourse(input: {
    courseId: "1",
    bookId: "1",
    mandatory: true
  }) {
    id
    courseId
    bookId
    mandatory
  }
}
```

### Update Course Material
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  updateCourseMaterial(id: "1", mandatory: false) {
    id
    courseId
    bookId
    mandatory
  }
}
```

### Remove Book From Course
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  removeBookFromCourse(id: "1") {
    id
    courseId
    bookId
  }
}
```

### Get Course Materials
**JWT Required**: YES (authenticated user)

```graphql
query {
  getCourseMaterials(courseId: "1") {
    id
    courseId
    bookId
    mandatory
  }
}
```

### Get Books By Course
**JWT Required**: YES (authenticated user)

```graphql
query {
  getBooksByCourse(courseId: "1") {
    id
    title
    isbn
    facultyId
  }
}
```

---

## Book Lending (Librarian/Admin Only)

### Issue Book
**JWT Required**: YES (requires LIBRARIAN or ADMIN roles)

```graphql
mutation {
  issueBook(input: {
    bookId: "1",
    userId: "1"
  }) {
    id
    bookId
    userId
    issueDate
    dueDate
    state {
      type
    }
  }
}
```

### Return Book
**JWT Required**: YES (requires LIBRARIAN or ADMIN roles)

```graphql
mutation {
  returnBook(input: {
    issueId: "1",
    userId: "1"
  }) {
    id
    bookId
    userId
    returnDate
    state {
      type
    }
  }
}
```

### Renew Book
**JWT Required**: YES (requires LIBRARIAN or ADMIN roles)

```graphql
mutation {
  renewBook(input: {
    issueId: "1",
    userId: "1"
  }) {
    id
    bookId
    userId
    dueDate
    renewalCount
    state {
      type
    }
  }
}
```

### Bulk Issue Books
**JWT Required**: YES (requires LIBRARIAN or ADMIN roles)

```graphql
mutation {
  bulkIssueBooks(input: {
    bookIds: ["1", "2", "3"],
    userId: "1"
  }) {
    id
    bookId
    userId
    issueDate
    dueDate
  }
}
```

### Get Issued Books By User
**JWT Required**: YES (authenticated user)

```graphql
query {
  getIssuedBooksByUser(userId: "1") {
    id
    bookId
    userId
    issueDate
    dueDate
    state {
      type
    }
  }
}
```

### Get Issue History
**JWT Required**: YES (authenticated user)

```graphql
query {
  getIssueHistory(userId: "1") {
    id
    bookId
    userId
    issueDate
    returnDate
    state {
      type
    }
  }
}
```

### Get Due Dates
**JWT Required**: YES (authenticated user)

```graphql
query {
  getDueDates {
    id
    bookId
    userId
    dueDate
    state {
      type
    }
  }
}
```

---

## Book Reservation

### Reserve Book
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  reserveBook(input: {
    bookId: "1",
    userId: "1"
  }) {
    id
    bookId
    userId
    reservedAt
    expiresAt
    status
  }
}
```

### Cancel Reservation
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  cancelReservation(reservationId: "1") {
    id
    bookId
    userId
    status
  }
}
```

### Allocate Reserved Book
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  allocateReservedBook(bookId: "1") {
    id
    bookId
    userId
    status
  }
}
```

### Get Reservation Queue
**JWT Required**: YES (authenticated user)

```graphql
query {
  getReservationQueue(bookId: "1") {
    id
    bookId
    userId
    reservedAt
    status
  }
}
```

### Get User Reservations
**JWT Required**: YES (authenticated user)

```graphql
query {
  getUserReservations(userId: "1") {
    id
    bookId
    userId
    reservedAt
    expiresAt
    status
  }
}
```

---

## Royalty Management (Admin Only)

### Define Royalty Policy
**JWT Required**: YES (requires ADMIN roles)

```graphql
mutation {
  defineRoyaltyPolicy(input: {
    bookId: "1",
    facultyId: "1",
    royaltyPercentage: 10.5,
    sharingModel: { "type": "percentage", "facultyShare": 70 }
  }) {
    id
    bookId
    facultyId
    royaltyPercentage
    sharingModel
  }
}
```

### Calculate Royalty
**JWT Required**: YES (requires ADMIN roles)

```graphql
mutation {
  calculateRoyalty(bookId: "1", totalRevenue: 1000.0) {
    id
    bookId
    facultyId
    totalRevenue
    calculatedRoyalty
    payoutStatus
  }
}
```

### Distribute Royalty
**JWT Required**: YES (requires ADMIN roles)

```graphql
mutation {
  distributeRoyalty(recordId: "1") {
    id
    payoutStatus
    distributedAt
  }
}
```

### Get Royalty Details
**JWT Required**: YES (requires ADMIN roles)

```graphql
query {
  getRoyaltyDetails(bookId: "1") {
    id
    facultyId
    totalRevenue
    calculatedRoyalty
    payoutStatus
  }
}
```

### Get Revenue By Book
**JWT Required**: YES (requires ADMIN roles)

```graphql
query {
  getRevenueByBook(bookId: "1")
}
```

---

## Notification Management

### Get Notifications By User
**JWT Required**: YES (authenticated user)

```graphql
query {
  getNotificationsByUser(userId: "1") {
    id
    type
    message
    read
    createdAt
  }
}
```

### Get Unread Notifications
**JWT Required**: YES (authenticated user)

```graphql
query {
  getUnreadNotifications(userId: "1") {
    id
    type
    message
    read
    createdAt
  }
}
```

### Get Unread Notification Count
**JWT Required**: YES (authenticated user)

```graphql
query {
  getUnreadNotificationCount(userId: "1")
}
```

### Mark Notification As Read
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  markNotificationAsRead(id: "1") {
    id
    read
  }
}
```

### Mark All Notifications As Read
**JWT Required**: YES (authenticated user)

```graphql
mutation {
  markAllNotificationsAsRead(userId: "1")
}
```

---

## Violation Detection (Admin Only)

### Detect Unauthorized Access
**JWT Required**: YES (requires ADMIN roles)

```graphql
query {
  detectUnauthorizedAccess {
    id
    username
    type
    severity
    description
    detectedAt
    resolved
  }
}
```

### Analyze Usage Patterns
**JWT Required**: YES (requires ADMIN roles)

```graphql
query {
  analyzeUsagePatterns {
    id
    username
    type
    severity
    description
    detectedAt
    resolved
  }
}
```

### Get Violation Reports
**JWT Required**: YES (requires ADMIN roles)

```graphql
query {
  getViolationReports(username: "johndoe") {
    id
    username
    type
    severity
    description
    detectedAt
    resolved
  }
}
```

---

## Token Validation

### Validate Token
**JWT Required**: NO (but requires token as argument)

```graphql
query {
  validateToken(token: "your-jwt-token") {
    valid
    username
    roles
    expiresAt
  }
}
```

---

## Role-Based Access Control

| Role | Description |
|------|-------------|
| ADMIN | Full system access, user management, book deletion, royalty management, violation detection |
| FACULTY | Create/update books, submit for review, publish/archive books |
| LIBRARIAN | Issue/return/renew books, publish/archive books |
| STUDENT | View books, reserve books, view own loans |

## Book State Flow

```
DRAFT → REVIEW → PUBLISHED → ARCHIVED
```

- **DRAFT**: Initial state when book is created
- **REVIEW**: Book submitted for review
- **PUBLISHED**: Book approved and published
- **ARCHIVED**: Book is no longer active

## Enums

### Role
```
ADMIN, FACULTY, STUDENT, LIBRARIAN
```

### ViolationType
```
UNAUTHORIZED_ACCESS, EXCESSIVE_DOWNLOADS, SUSPICIOUS_SHARING, ACCESS_ABUSE
```

### ViolationSeverity
```
LOW, MEDIUM, HIGH, CRITICAL
```

## Issue State Types

- **Requested**: Book issue requested
- **Issued**: Book currently issued (has issueDate, dueDate)
- **Returned**: Book returned (has returnDate, penaltyAmount)
- **Overdue**: Book overdue (has dueDate, penaltyAmount)

## Configuration

The application configuration is in `config-local/` (e.g. `config-local/identity-service.yml` and other config files):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scholario
    username: scholario
    password: Scholario@123
  jpa:
    hibernate:
      ddl-auto: update
  graphql:
    graphiql:
      enabled: true
    path: /graphql
server:
  port: 8090 # API Gateway port
```
