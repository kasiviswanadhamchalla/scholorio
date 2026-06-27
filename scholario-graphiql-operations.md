# Scholario GraphiQL Operations Manual

This document provides a step-by-step testing manual for the Scholario GraphQL API using GraphiQL.

## How to Use
1.  **Query Pane:** Copy the GraphQL code into the main "Query" pane.
2.  **Variables Pane:** Copy the JSON from the "Variables" section into the "Query Variables" pane (bottom left).
3.  **Auth Headers:** For authenticated requests, add the following to the "HTTP HEADERS" pane:
    ```json
    { "Authorization": "Bearer YOUR_TOKEN_HERE" }
    ```

---

## 1. Authentication & Registration

### [1.1] Register a New User
Run this to create an account. Change the roles to `ADMIN`, `FACULTY`, `STUDENT`, or `LIBRARIAN`.

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
Use the credentials you registered with. Copy the `accessToken` for future requests.

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
query GetMyProfile {
  getMyProfile {
    id
    username
    fullName
    roles
    email
    department {
      name
      code
    }
  }
}
```

### [2.2] Update My Profile
**Query:**
```graphql
mutation UpdateUserProfile($input: ProfileInput!) {
  updateUserProfile(input: $input) {
    id
    fullName
    email
  }
}
```

### [2.3] List All Faculty
**Query:**
```graphql
query GetFacultyList {
  getFacultyList {
    id
    fullName
    email
  }
}
```

### [2.4] Assign Role (ADMIN ONLY)
**Query:**
```graphql
mutation AssignRole($userId: ID!, $roles: [Role!]!) {
  assignRole(userId: $userId, role: $roles) {
    id
    roles
  }
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
    "title": "Introduction to Algorithms",
    "isbn": "978-0262033848",
    "description": "A comprehensive guide to algorithms"
  }
}
```

### [3.2] Publish Book
**Query:**
```graphql
mutation PublishBook($id: ID!) {
  publishBook(id: $id) {
    id
    state {
      type
    }
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

---

## 5. Courses & Materials

### [5.1] Create a Course
**Query:**
```graphql
mutation CreateCourse($input: CourseInput!) {
  createCourse(input: $input) {
    id
    courseCode
    title
  }
}
```

---

## 6. Reservations

### [6.1] Reserve a Book
**Query:**
```graphql
mutation ReserveBook($bookId: ID!) {
  reserveBook(bookId: $bookId) {
    id
    status
    reservedAt
  }
}
```

### [6.2] Get My Reservations
**Query:**
```graphql
query GetMyReservations {
  getUserReservations {
    id
    bookId
    status
  }
}
```

---

## 7. Lending

### [7.1] Get My Issued Books
**Query:**
```graphql
query GetMyIssuedBooks {
  getMyIssuedBooks {
    id
    bookId
    dueDate
    state {
      type
    }
  }
}
```

### [7.2] Issue Book (LIBRARIAN ONLY)
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

---

## 8. Recommendations

### [8.1] Recommend Books
**Query:**
```graphql
query Recommend {
  recommendBooks {
    bookId
    title
    recommendationReason
    confidenceScore
  }
}
```

---

## 9. Notifications

### [13.1] Get My Notifications
**Query:**
```graphql
query GetMyNotifications {
  getMyNotifications {
    id
    message
    read
  }
}
```

### [13.2] Mark All As Read
**Query:**
```graphql
mutation MarkAllAsRead {
  markAllNotificationsAsRead
}
```
