# Scholario Project Instructions

## REST Architecture
- **API Routing:** The REST endpoints are accessed via the API Gateway mapping routes directly to the microservices:
  - `/api/catalog/**` -> `catalog-service` (Port 8081)
  - `/api/lending/**` -> `lending-service` (Port 8082)
  - `/api/member/**` -> `member-service` (Port 8083)
  - `/api/notification/**` -> `notification-service` (Port 8084)
- **Discovery:** Verify endpoints by inspecting the standard Spring REST `@RestController` annotation and `@RequestMapping` mappings inside each module's controller classes.

## Data Fetching Standards
- **Authenticity:** Always fetch data from the actual backend REST endpoints.
- **No Mocks:** Never write dummy or mock data for features. All components must be integrated with the Spring Boot REST backend.
- **Completeness:** Ensure all implementations are thorough and complete. Do not leave placeholders, half-finished work, or "TODO" comments for backend integration.
- **Verification:** Before implementing a feature, verify the REST operations using the controller class definitions in the backend modules.

## Workflow
- **Research:** Systematically map the REST controllers inside each module to understand the available endpoints.
- **Implementation:** Apply clean REST mappings to the frontend components. Make direct REST requests using the custom Axios-based hooks (`useRestQuery` and `useRestMutation` defined in [useRest.js](file:///C:/Users/kasiv/OneDrive/Desktop/scholario-main/scholario-frontend-jsx/src/hooks/useRest.js)).
- **Validation:** Always verify that the features work seamlessly with the live REST backend.
