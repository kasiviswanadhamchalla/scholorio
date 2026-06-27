# Scholario End-to-End Role-Based Workflows

This document outlines the step-by-step workflows for each roles. All GraphQL operations have been expanded to select **all available fields** and use **hardcoded values** for direct use in Postman or GraphiQL without variables.

---

## 🟢 1. Admin Workflow (System Setup)

### [1.1] Register Admin Account
```graphql
mutation {
  registerUser(input: {
    username: "admin_user",
    password: "AdminPassword123!",
    email: "admin@scholario.edu",
    fullName: "System Administrator"
  }) {
    id
    username
    email
    fullName
    roles
    department { id name code }
    createdAt
    updatedAt
  }
}
```

### [1.2] Login
```graphql
mutation {
  login(input: {
    username: "admin_user",
    password: "AdminPassword123!"
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
      department { id name code }
      createdAt
      updatedAt
    }
  }
}
```

### [1.3] Setup Departments
```graphql
mutation {
  createDepartment(input: {
    name: "Computer Science",
    code: "CS"
  }) {
    id
    name
    code
  }
}
```

### [1.4] Monitor Security (Violations)
```graphql
query {
  getViolationReports {
    id
    username
    type
    severity
    description
    timestamp
  }
}
```

---

## 🔵 2. Faculty Workflow (Content & Course Management)

### [2.1] Author a Book (Draft)
```graphql
mutation {
  createBook(input: {
    title: "Modern Java Development",
    isbn: "978-3-16-148410-0",
    description: "A comprehensive guide to Java 21+",
    facultyId: "2"
  }) {
    id
    title
    isbn
    description
    facultyId
    versionNumber
    parentBookId
    createdAt
    updatedAt
    state {
      type
    }
  }
}
```

### [2.2] Submit for Review
```graphql
mutation {
  submitBookForReview(bookId: "1", reviewerId: "3") {
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

### [2.3] Manage Digital Assets
```graphql
mutation {
  uploadDigitalContent(input: {
    bookId: "1",
    contentType: "PDF",
    contentUrl: "https://assets.scholario.edu/books/java-modern.pdf",
    drmEnforced: true
  }) {
    id
    bookId
    contentType
    contentUrl
    drmEnforced
    createdAt
    updatedAt
  }
}
```

### [2.4] Course Setup & Material Assignment
```graphql
mutation {
  createCourse(input: {
    courseCode: "CS101",
    title: "Introduction to Programming",
    description: "Learn the basics of coding with Java",
    facultyId: "2"
  }) {
    id
    courseCode
    title
    description
    facultyId
    createdAt
    updatedAt
  }
}

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
    createdAt
    updatedAt
  }
}
```

### [2.5] Track Royalties & Analytics
```graphql
mutation {
  defineRoyaltyPolicy(input: {
    bookId: "1",
    royaltyPercentage: 15.5
  }) {
    id
    bookId
    royaltyPercentage
    effectiveFrom
    createdAt
  }
}

mutation {
  calculateRoyalty(bookId: "1", totalRevenue: 10000.0) {
    id
    bookId
    calculatedRoyalty
    calculationDate
    payoutStatus
    revenuePeriodStart
    revenuePeriodEnd
  }
}

query {
  getBookUsageAnalytics(bookId: "1") {
    bookId
    title
    totalIssues
    totalReservations
    digitalAccessCount
    lastAccessedAt
  }
}
```

---

## 🟠 3. Reviewer Workflow (Quality Control)

### [3.1] View Review History
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

### [3.2] Approve Book
```graphql
mutation {
  approveBook(requestId: "1", feedback: "The content is accurate and well-structured. Highly recommended for students.") {
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

---

## 🟣 4. Student Workflow (Learning & Library)

### [4.1] Discovery & Recommendations
```graphql
query {
  searchBooks(title: "Java") {
    id
    title
    isbn
    description
    state { type }
    createdAt
  }
}

query {
  recommendBooks(userId: "4") {
    bookId
    title
    recommendationReason
    confidenceScore
  }
}
```

### [4.2] Physical Library (Lending & Reservations)
```graphql
mutation {
  issueBook(input: {
    bookId: "1",
    userId: "4"
  }) {
    id
    bookId
    userId
    issueDate
    dueDate
    renewalCount
    penaltyAmount
    state {
      type
      ... on Issued { issuedAt returnBy }
      ... on Overdue { overdueSince penaltyAmount }
    }
  }
}

mutation {
  reserveBook(input: {
    bookId: "2",
    userId: "4"
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

### [4.3] Access Digital Content
```graphql
query {
  getDigitalContent(id: "1") {
    id
    bookId
    contentType
    contentUrl
    drmEnforced
  }
}

query {
  suggestCourseMaterials(courseId: "1") {
    bookId
    title
    courseTitle
    suggestionReason
  }
}
```

### [4.4] Notifications
```graphql
query {
  getNotificationsByUser(userId: "4") {
    id
    type
    message
    userId
    relatedEntityId
    read
    createdAt
  }
}
```

---

## 🟡 5. Real-Time Subscriptions
*Note: Subscriptions typically require a WebSocket connection.*

### [5.1] Watch for New Publications
```graphql
subscription {
  bookPublished {
    id
    type
    message
    userId
    relatedEntityId
    read
    createdAt
  }
}
```
