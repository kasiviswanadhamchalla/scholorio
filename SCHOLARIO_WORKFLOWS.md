# Scholario REST API End-to-End Workflows & Dummy Data Examples

This document outlines the workflows and contains realistic copy-pasteable dummy data payloads to test the Spring Boot REST API endpoints.

---

## 🟢 1. Super Admin Workflow (System Setup & Auditing)

### [1.1] Create Departments
* **Request:** `POST /api/member/departments`
* **Headers:** `Authorization: Bearer <SUPER_ADMIN_JWT>`
* **Payload:**
```json
{
  "name": "Computer Science & Engineering",
  "code": "CSE"
}
```

* **Another Department Payload:**
```json
{
  "name": "Electrical & Electronics Engineering",
  "code": "EEE"
}
```

### [1.2] Assign Roles to Users
* **Request:** `POST /api/member/users/2/assign-role?role=LIBRARIAN`
* **Headers:** `Authorization: Bearer <SUPER_ADMIN_JWT>`

* **Another Assign Request:**
* **Request:** `POST /api/member/users/3/assign-role?role=MEMBER`
* **Headers:** `Authorization: Bearer <SUPER_ADMIN_JWT>`

### [1.3] Link Member to Department
* **Request:** `POST /api/member/users/3/link-department?departmentId=1`
* **Headers:** `Authorization: Bearer <SUPER_ADMIN_JWT>`

---

## 🔵 2. Librarian & Assistant Librarian Workflow (Circulation & Approvals)

### [2.1] Catalog a New Book (Draft)
* **Request:** `POST /api/catalog`
* **Headers:** `Authorization: Bearer <LIBRARIAN_JWT>`
* **Payload:**
```json
{
  "title": "Introduction to Algorithms, Fourth Edition",
  "isbn": "978-0262046305",
  "description": "The classic guide to computer algorithms, fully updated.",
  "facultyId": 3
}
```

### [2.2] Submit Book for Review
* **Request:** `POST /api/catalog/1/submit-review`
* **Headers:** `Authorization: Bearer <LIBRARIAN_JWT>`

### [2.3] Retrieve Pending Approval Queue
* **Request:** `GET /api/lending/queue`
* **Headers:** `Authorization: Bearer <LIBRARIAN_JWT>`

### [2.4] Approve Lending Request (Level 1 / Level 2)
* **Request:** `POST /api/lending/1/approve`
* **Headers:** `Authorization: Bearer <LIBRARIAN_JWT>`

### [2.5] Reject Lending Request with Reason
* **Request:** `POST /api/lending/1/reject`
* **Headers:** `Authorization: Bearer <LIBRARIAN_JWT>`
* **Payload:**
```json
{
  "reason": "This book is currently reserved for course reference only."
}
```

### [2.6] Publish Catalog Book
* **Request:** `POST /api/catalog/1/publish`
* **Headers:** `Authorization: Bearer <LIBRARIAN_JWT>`

---

## 🟣 3. Member Workflow (Search, Lending, & Reservations)

### [3.1] Discover Books in Catalog
* **Request:** `GET /api/catalog?title=Algorithms`
* **Headers:** `Authorization: Bearer <MEMBER_JWT>`

### [3.2] Submit Lending Request
* **Request:** `POST /api/lending/request`
* **Headers:** `Authorization: Bearer <MEMBER_JWT>`
* **Payload:**
```json
{
  "bookId": 1
}
```

### [3.3] View Personal Borrowed Books
* **Request:** `GET /api/lending/my-issued`
* **Headers:** `Authorization: Bearer <MEMBER_JWT>`

### [3.4] Renew Lending Period
* **Request:** `POST /api/lending/renew`
* **Headers:** `Authorization: Bearer <MEMBER_JWT>`
* **Payload:**
```json
{
  "issueId": 1
}
```

---

## 🟡 4. Course & Digital Content Management Workflow

### [4.1] Create Course
* **Request:** `POST /api/courses`
* **Headers:** `Authorization: Bearer <FACULTY_JWT>`
* **Payload:**
```json
{
  "courseCode": "CS-301",
  "title": "Design and Analysis of Algorithms",
  "description": "An advanced study of design paradigms and complexity analysis.",
  "facultyId": 3
}
```

### [4.2] Assign Published Book to Course
* **Request:** `POST /api/courses/materials`
* **Headers:** `Authorization: Bearer <FACULTY_JWT>`
* **Payload:**
```json
{
  "courseId": 1,
  "bookId": 1,
  "mandatory": true
}
```

### [4.3] Upload Digital Content (DRM Enforced)
* **Request:** `POST /api/content/upload`
* **Headers:** `Authorization: Bearer <FACULTY_JWT>`
* **Payload:**
```json
{
  "bookId": 1,
  "contentType": "PDF",
  "contentUrl": "https://assets.scholario.edu/ebooks/algorithms-4e.pdf",
  "drmEnforced": true
}
```
