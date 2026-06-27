# Scholario Insomnia Operations Manual

This document provides a testing manual for the Scholario GraphQL API using Insomnia.

## How to Use in Insomnia
1.  **Request Type:** Create a new "HTTP Request".
2.  **Method & URL:** Set Method to `POST` and URL to `http://localhost:8090/graphql`.
3.  **Body Tab:** Select the "Body" tab and choose `GraphQL`.
4.  **Query Field:** Copy the code from the "Query" blocks below.
5.  **Variables Field:** Copy the JSON from the "Variables" blocks below.
6.  **Auth Tab:** Go to the "Auth" tab, select `Bearer Token`, and paste your `accessToken`.

---

## 1. Authentication & Registration

### [1.1] Register a New User
**Query:**
```graphql
mutation RegisterUser($input: UserInput!) {
  registerUser(input: $input) {
    id
    username
    email
    fullName
    roles
  }
}
```

**Variables:**
```json
{
  "input": {
    "username": "faculty_jane",
    "password": "Password123!",
    "email": "jane@scholario.edu",
    "fullName": "Jane Doe",
    "roles": "FACULTY"
  }
}
```

### [1.2] Login
**Query:**
```graphql
mutation Login($input: LoginInput!) {
  login(input: $input) {
    accessToken
    refreshToken
    tokenType
    expiresIn
    user {
      id
      username
      roles
    }
  }
}
```

**Variables:**
```json
{
  "input": {
    "username": "faculty_jane",
    "password": "Password123!"
  }
}
```

---

## 2. User & Profile Management

### [2.1] Get My Profile
**Query:**
```graphql
query GetUserById($id: ID!) {
  getUserById(id: $id) {
    id
    username
    fullName
    roles
    department {
      name
      code
    }
  }
}
```

**Variables:**
```json
{
  "id": "PASTE_USER_ID_HERE"
}
```

---

## 3. Book Management

### [3.1] Create a New Book (DRAFT)
**Query:**
```graphql
mutation CreateBook($input: BookInput!) {
  createBook(input: $input) {
    id
    title
    isbn
    state {
      type
    }
  }
}
```

**Variables:**
```json
{
  "input": {
    "title": "Quantum Computing 101",
    "isbn": "ISBN-978-Q-001",
    "facultyId": "PASTE_FACULTY_ID_HERE",
    "description": "Introduction to Quantum bits and gates."
  }
}
```

---

## 4. Reviews & Approvals

### [4.1] Submit Book for Review
**Query:**
```graphql
mutation SubmitForReview($bookId: ID!, $reviewerId: ID) {
  submitBookForReview(bookId: $bookId, reviewerId: $reviewerId) {
    id
    status
  }
}
```

**Variables:**
```json
{
  "bookId": "PASTE_BOOK_ID_HERE",
  "reviewerId": "PASTE_ANOTHER_FACULTY_ID_HERE"
}
```

---

## 5. Courses & Materials

### [5.1] Assign Book to Course
**Query:**
```graphql
mutation AssignBookToCourse($input: CourseMaterialInput!) {
  assignBookToCourse(input: $input) {
    id
    mandatory
  }
}
```

**Variables:**
```json
{
  "input": {
    "courseId": "PASTE_COURSE_ID_HERE",
    "bookId": "PASTE_BOOK_ID_HERE",
    "mandatory": true
  }
}
```

---

## 6. Reservations

### [6.1] Reserve a Book
**Query:**
```graphql
mutation ReserveBook($input: ReservationInput!) {
  reserveBook(input: $input) {
    id
    status
    reservedAt
  }
}
```

**Variables:**
```json
{
  "input": {
    "bookId": "PASTE_BOOK_ID_HERE",
    "userId": "PASTE_STUDENT_ID_HERE"
  }
}
```

---

## 7. Lending

### [7.1] Issue Book
**Query:**
```graphql
mutation IssueBook($input: IssueInput!) {
  issueBook(input: $input) {
    id
    dueDate
    state {
      type
    }
  }
}
```

**Variables:**
```json
{
  "input": {
    "bookId": "PASTE_BOOK_ID_HERE",
    "userId": "PASTE_USER_ID_HERE"
  }
}
```

---

## 8. Royalties

### [8.1] Calculate Payout
**Query:**
```graphql
mutation CalculateRoyalty($bookId: ID!, $totalRevenue: Float!) {
  calculateRoyalty(bookId: $bookId, totalRevenue: $totalRevenue) {
    id
    calculatedRoyalty
    payoutStatus
  }
}
```

**Variables:**
```json
{
  "bookId": "PASTE_BOOK_ID_HERE",
  "totalRevenue": 10000.0
}
```

---

## 9. Violations

### [9.1] Get Violation Reports
**Query:**
```graphql
query GetViolationReports {
  getViolationReports {
    username
    type
    severity
    description
  }
}
```

---

## 10. Digital Content

### [10.1] Upload Digital Content
**Query:**
```graphql
mutation UploadContent($input: DigitalContentInput!) {
  uploadDigitalContent(input: $input) {
    id
    contentUrl
  }
}
```

**Variables:**
```json
{
  "input": {
    "bookId": "PASTE_BOOK_ID",
    "contentType": "PDF",
    "contentUrl": "http://example.com/book.pdf",
    "drmEnforced": true
  }
}
```

---

## 11. Analytics

### [11.1] Get Book Usage Analytics
**Query:**
```graphql
query BookAnalytics($bookId: ID!) {
  getBookUsageAnalytics(bookId: $bookId) {
    totalIssues
    totalReservations
    digitalAccessCount
  }
}
```

**Variables:**
```json
{ "bookId": "PASTE_BOOK_ID" }
```

---

## 12. Recommendations

### [12.1] Recommend Books
**Query:**
```graphql
query Recommend($userId: ID!) {
  recommendBooks(userId: $userId) {
    bookId
    title
    recommendationReason
    confidenceScore
  }
}
```

**Variables:**
```json
{ "userId": "PASTE_USER_ID" }
```

---

## 13. Notifications

### [13.1] Get My Notifications
**Query:**
```graphql
query GetNotifications($userId: ID!) {
  getNotificationsByUser(userId: $userId) {
    id
    message
    read
  }
}
```

**Variables:**
```json
{
  "userId": "PASTE_USER_ID_HERE"
}
```

---

## 14. Subscriptions (Real-Time)

### [14.1] Watch Book Publications
**Subscription:**
```graphql
subscription OnBookPublished {
  bookPublished {
    message
    relatedEntityId
  }
}
```

> **Note:** Subscriptions in Insomnia require a WebSocket connection. Ensure your version of Insomnia supports GraphQL Subscriptions over WebSockets.
