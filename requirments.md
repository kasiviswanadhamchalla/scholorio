Smart Faculty Book & Academic Resource Management System Overview
You are required to design and develop a Faculty Book Backend System using the following specifications.
Core Technology Stack
Java 21 (modern features mandatory)
Spring Boot + Spring GraphQL
Gradle (mandatory build tool)
OAuth 2.0 Authentication (Spring Security)
MySQL / H2 (Relational Database)
System Capabilities
The system simulates a real-world university academic resource platform handling:
Faculty-authored books & publications
Course material management
Book issuance & reservations
Digital content access
Approval workflows
Licensing & royalties
Analytics & academic insights
Architecture Requirements
Gradle multi-module architecture
MySQL / H2 with optimized indexing
High concurrency for book access & reservations
Event-driven + GraphQL hybrid
Idempotent operations for issuance
Security
OAuth 2.0 with JWT
Roles: STUDENT, FACULTY, LIBRARIAN, ADMIN
Java 21 Features
Records → GraphQL DTOs
Sealed Classes → Book/Issue states
Pattern Matching for switch
Virtual Threads → concurrent access & reservations
Advanced Streams & Optional
Functional Requirements (Action-Based Modules + GraphQL)
All APIs must be exposed via GraphQL (Queries + Mutations + Subscriptions). Modules are defined using action-oriented capabilities (verbs).
Module 1: Create & Manage Faculty Books
Mutations: createBook, updateBook, deleteBook, publishBook, archiveBook, versionBook
Queries: getBookById, searchBooks, getBooksByFaculty, getBookVersions
Book State Flow (Sealed Classes): DRAFT → REVIEW → PUBLISHED → ARCHIVED
Business Rules:
ISBN/unique ID required
Faculty ownership validation
Version control mandatory

Cannot modify published content without versioning
Module 2: Register & Manage Faculty & Students
Mutations: registerUser, updateUserProfile, assignRole, linkFacultyToDepartment
Queries: getUserById, getFacultyList, getStudentList
Business Rules:
Role-based access
Department mapping
Academic hierarchy
Module 3: Assign Books to Courses
Mutations: assignBookToCourse, updateCourseMaterial, removeBookFromCourse
Queries: getCourseMaterials, getBooksByCourse
Business Rules:
Course mapping validation
Mandatory vs optional materials
Version consistency
Module 4: Issue & Manage Book Lending
Mutations: issueBook, returnBook, renewBook, bulkIssueBooks
Queries: getIssuedBooksByUser, getIssueHistory, getDueDates
Issue State Flow (Sealed Classes): REQUESTED → ISSUED → RETURNED → OVERDUE
Business Rules:
Max books per user
Issue duration limits
Renewal restrictions
Late return penalties
Module 5: Reserve & Allocate Books
Mutations: reserveBook, cancelReservation, allocateReservedBook
Queries: getReservationQueue, getUserReservations
Business Rules:
FIFO reservation
Reservation expiry
Auto-allocation
Module 6: Provide Digital Content Access
Mutations: uploadDigitalContent, grantAccess, revokeAccess
Queries: getDigitalContent, getAccessLogs
Business Rules:
Role-based access
DRM enforcement
Download/view restrictions
Module 7: Manage Licensing & Royalties
Mutations: defineRoyaltyPolicy, calculateRoyalty, distributeRoyalty
Queries: getRoyaltyDetails, getRevenueByBook
Core Formula: Royalty = Total Revenue × Royalty Percentage
Business Rules:
Faculty earnings calculation
Revenue sharing models
Periodic payouts
Module 8: Handle Approval & Review Workflows
Mutations: submitBookForReview, approveBook, rejectBook, requestChanges
Queries: getReviewStatus, getReviewHistory
Business Rules:
Multi-level approvals
Reviewer assignment
Feedback loop
Module 9: Send Notifications & Subscriptions
Subscriptions: bookPublished, bookIssued, dueDateReminder, reservationAvailable
Business Rules:
Real-time notifications
Role-based filtering
Module 10: Detect Misuse & Policy Violations
Queries: detectUnauthorizedAccess, analyzeUsagePatterns, getViolationReports
Business Rules:
Excess downloads → flag
Unauthorized sharing detection
Access abuse tracking
Module 11: Authenticate & Authorize Users
Mutations: login, refreshToken, logout
Queries: validateToken
Business Rules:
OAuth token validation
Role-based GraphQL access
Module 12: Analyze Academic Usage & Reports
Queries: getBookUsageAnalytics, getCourseMaterialStats, getFacultyPerformance, getStudentEngagement
Business Rules:
Aggregation queries (MySQL / H2)
Time-based analytics
Role-based dashboards
Module 13: Recommend Books & Optimize Learning
Queries: recommendBooks, suggestCourseMaterials, predictDemand
Business Rules:
Based on usage patterns
Academic trends
Personalized recommendations
GraphQL Schema Example
type Query {
getBookById(id: ID!): Book
getCourseMaterials(courseId: ID!): [Book]
}
type Mutation {
createBook(input: BookInput): Book
issueBook(bookId: ID!, userId: ID!): Issue
}
type Subscription {
bookPublished: Book
}
Bonus (Advanced Expectations)
Recommendation engine for books
Digital content streaming optimization
GraphQL federation
MySQL / H2 indexing & partitioning
Mandatory Testing Requirement
JUnit 5 + Mockito
GraphQL testing
Cover: Lending logic, Reservation system, Royalty calculation, Concurrency scenarios

