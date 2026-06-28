# Scholario Microservices Startup Sequence

To start the Scholario microservice application successfully, follow this execution order:

## 1. Prerequisites (Infrastructure)
1. **Database:** Ensure your MySQL service is running on `localhost:3306`.
2. **Kafka Broker:** Ensure your local Apache Kafka broker is running on `localhost:9092` (needed for inter-service communication events).

---

## 2. Core Infrastructure Services
These services must be started first so that other services can retrieve configurations and register themselves.

| Start Order | Service Name | Gradle Command | Port | Description |
| :--- | :--- | :--- | :--- | :--- |
| **1** | `config-server` | `./gradlew :config-server:bootRun` | `8888` | Configures and serves application parameters to all services. |
| **2** | `eureka-server` | `./gradlew :eureka-server:bootRun` | `8761` | Service registry for discovery. |

*Wait ~10 seconds after starting each of the above before proceeding.*

---

## 3. Core Domain Microservices
These microservices handle the primary business logic. They should be booted next.

| Start Order | Service Name | Gradle Command | Port | Description |
| :--- | :--- | :--- | :--- | :--- |
| **3** | `identity-service` (`member-service`) | `./gradlew :identity-service:bootRun` | `8083` | User registration, authentication, and role mappings. |
| **4** | `book-service` (`catalog-service`) | `./gradlew :book-service:bootRun` | `8081` | Book registration and inventory. |
| **5** | `lending-service` | `./gradlew :lending-service:bootRun` | `8082` | Circulation, borrowing, and approval management. |
| **6** | `notification-service` | `./gradlew :notification-service:bootRun` | `8084` | User alerts and websocket pushes. |

---

## 4. Supporting & Secondary Microservices
These services handle auxiliary features and can be started in any order after the core domain services are running.

* `course-service` -> `./gradlew :course-service:bootRun` (Port `8086`)
* `reservation-service` -> `./gradlew :reservation-service:bootRun` (Port `8087`)
* `approval-service` -> `./gradlew :approval-service:bootRun` (Port `8088`)
* `royalty-service` -> `./gradlew :royalty-service:bootRun` (Port `8089`)
* `digital-content-service` -> `./gradlew :digital-content-service:bootRun` (Port `8091`)
* `search-service` -> `./gradlew :search-service:bootRun` (Port `8092`)
* `analytics-service` -> `./gradlew :analytics-service:bootRun` (Port `8093`)
* `audit-service` -> `./gradlew :audit-service:bootRun` (Port `8094`)

---

## 5. API Gateway
The gateway should be started after all other microservices have registered with Eureka so that it can route requests correctly.

| Start Order | Service Name | Gradle Command | Port | Description |
| :--- | :--- | :--- | :--- | :--- |
| **7** | `api-gateway` | `./gradlew :api-gateway:bootRun` | `8080` | Centralized API entry point. |

---

## 6. Frontend Portal
Once all backend services and the gateway are active, run the React client:

```bash
cd scholario-frontend-jsx
npm run dev
```
Access the application at: `http://localhost:5173`.
