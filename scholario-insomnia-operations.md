# Scholario Insomnia Operations Manual with Dummy Data

This document provides a testing manual for the Scholario REST API using Insomnia, complete with copy-pasteable dummy data.

---

## 1. Catalog Service (`http://localhost:8081`)

### [1.1] List/Search Books
* **Method:** `GET`
* **URL:** `http://localhost:8081/api`

### [1.2] Create a Book (DRAFT)
* **Method:** `POST`
* **URL:** `http://localhost:8081/api`
* **Payload:**
```json
{
  "title": "Quantum Mechanics for Engineers",
  "isbn": "978-0131118928",
  "description": "An introductory text on quantum theory for engineering undergraduates.",
  "facultyId": 3
}
```

### [1.3] View Book Details
* **Method:** `GET`
* **URL:** `http://localhost:8081/api/1`

### [1.4] Publish Book
* **Method:** `POST`
* **URL:** `http://localhost:8081/api/1/publish`

### [1.5] Submit for Review
* **Method:** `POST`
* **URL:** `http://localhost:8081/api/1/submit-review`

---

## 2. Lending Service (`http://localhost:8082`)

### [2.1] Retrieve Pending Approval Queue
* **Method:** `GET`
* **URL:** `http://localhost:8082/api/queue`

### [2.2] Approve Lending Request
* **Method:** `POST`
* **URL:** `http://localhost:8082/api/1/approve`

### [2.3] Reject Request with Reason
* **Method:** `POST`
* **URL:** `http://localhost:8082/api/1/reject`
* **Payload:**
```json
{
  "reason": "The physical copy is currently reserved for the reference section."
}
```

### [2.4] Request Book Reservation / Lending Request
* **Method:** `POST`
* **URL:** `http://localhost:8082/api/request`
* **Payload:**
```json
{
  "bookId": 1
}
```

---

## 3. Member Service (`http://localhost:8083`)

### [3.1] Create Department
* **Method:** `POST`
* **URL:** `http://localhost:8083/api/departments`
* **Payload:**
```json
{
  "name": "Physics & Astronomy",
  "code": "PHYS"
}
```

### [3.2] Link Member to Department
* **Method:** `POST`
* **URL:** `http://localhost:8083/api/users/3/link-department?departmentId=1`

### [3.3] Get Admin Dashboard KPIs
* **Method:** `GET`
* **URL:** `http://localhost:8083/api/dashboard`

### [3.4] Get Monthly Analytics Report
* **Method:** `GET`
* **URL:** `http://localhost:8083/api/reports`

### [3.5] Export Reports as CSV File
* **Method:** `GET`
* **URL:** `http://localhost:8083/api/reports/export`

### [3.6] View User Profile
* **Method:** `GET`
* **URL:** `http://localhost:8083/api/profile`

### [3.7] Assign User Role
* **Method:** `POST`
* **URL:** `http://localhost:8083/api/users/2/assign-role?role=LIBRARIAN`

---

## 4. Digital Content Service (`http://localhost:8085` or direct context)

### [4.1] Upload E-Book URL
* **Method:** `POST`
* **URL:** `http://localhost:8081/api/content/upload`
* **Payload:**
```json
{
  "bookId": 1,
  "contentType": "PDF",
  "contentUrl": "https://assets.scholario.edu/books/physics-eng.pdf",
  "drmEnforced": true
}
```
