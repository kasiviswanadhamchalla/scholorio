# Scholario API Gateway Operations Manual - Sequential Testing Workflow

This manual details the step-by-step lifecycle flow of Scholario. Run these operations sequentially from **Step 1 to Step 8** to perform an end-to-end integration test of all 12 services via the API Gateway (`http://localhost:8090`).

Configure your requests to use a `Bearer Token` containing your JWT (or `mock-jwt-token-123456` in local mock mode).

---

## Step 1: System Bootstrapping (member-service)

### [1.1] Create Department
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/member/departments`
* **Payload:**
```json
{
  "name": "Physics & Astronomy",
  "code": "PHYS"
}
```

### [1.2] Link Member to Department
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/member/users/3/link-department?departmentId=1`

### [1.3] Assign User Role
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/member/users/2/assign-role?role=LIBRARIAN`

---

## Step 2: Catalog Creation & Peer Review (book-service & approval-service)

### [2.1] Create Book (DRAFT)
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/catalog`
* **Payload:**
```json
{
  "title": "Quantum Mechanics for Engineers",
  "isbn": "978-0131118928",
  "description": "An introductory text on quantum theory for engineering undergraduates.",
  "facultyId": 3
}
```

### [2.2] Submit Book for Peer Review
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/approval/submit?bookId=1&reviewerId=3`

### [2.3] Approve Book in Review
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/approval/1/approve?feedback=Approved by Peer Committee`

### [2.4] Publish Book
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/catalog/1/publish`

---

## Step 3: Digital E-Book Setup (digital-content-service)

### [3.1] Upload E-Book URL
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/digital/content/upload`
* **Payload:**
```json
{
  "bookId": 1,
  "contentType": "PDF",
  "contentUrl": "https://assets.scholario.edu/books/physics-eng.pdf",
  "drmEnforced": true
}
```

---

## Step 4: Lending Workflow & Approvals (lending-service)

### [4.1] Request Book Reservation / Borrow
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/lending/request`
* **Payload:**
```json
{
  "bookId": 1
}
```

### [4.2] Retrieve Pending Lending Queue
* **Method:** `GET`
* **URL:** `http://localhost:8090/api/lending/queue`

### [4.3] Approve Lending Request
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/lending/1/approve`

---

## Step 5: Academic Course Assignment (course-service)

### [5.1] Create Course
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/course`
* **Payload:**
```json
{
  "courseCode": "CS101",
  "title": "Intro to Computer Science",
  "facultyId": 3
}
```

### [5.2] Assign Published Book as Course Material
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/course/materials`
* **Payload:**
```json
{
  "courseId": 1,
  "bookId": 1,
  "mandatory": true
}
```

---

## Step 6: Royalty Policies & Payouts (royalty-service)

### [6.1] Define Royalty Policy
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/royalty/policies`
* **Payload:**
```json
{
  "bookId": 1,
  "royaltyPercentage": 12.50
}
```

### [6.2] Calculate Accumulated Royalty
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/royalty/calculate?bookId=1&totalRevenue=2400.00`

### [6.3] Distribute Royalty to Author
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/royalty/distribute/1`

---

## Step 7: Advanced Book Reservations (reservation-service)

### [7.1] Place Reservation on Book
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/reservation?bookId=1&userId=3`

### [7.2] Retrieve Reservation Queue
* **Method:** `GET`
* **URL:** `http://localhost:8090/api/reservation/queue/1`

### [7.3] Allocate Reserved Book to Queue Head
* **Method:** `POST`
* **URL:** `http://localhost:8090/api/reservation/allocate?bookId=1`

---

## Step 8: Analytics, Notifications, Audit & Search (analytics, search, notification, audit)

### [8.1] Get Dashboard KPIs
* **Method:** `GET`
* **URL:** `http://localhost:8090/api/member/dashboard`

### [8.2] Get Book Usage Analytics
* **Method:** `GET`
* **URL:** `http://localhost:8090/api/analytics/book/1`

### [8.3] Recommend Books for User
* **Method:** `GET`
* **URL:** `http://localhost:8090/api/search/recommend/3`

### [8.4] Get Unread System Alerts
* **Method:** `GET`
* **URL:** `http://localhost:8090/api/notification/api/notifications/unread`

### [8.5] Detect Security Violations
* **Method:** `GET`
* **URL:** `http://localhost:8090/api/audit/detect-unauthorized`
