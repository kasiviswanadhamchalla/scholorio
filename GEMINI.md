# Scholario Project Instructions

## GraphQL Architecture
- **Endpoint:** The backend GraphQL endpoint is located at `http://localhost:8090/graphql`. In the frontend, the Apollo Client is configured to use `/graphql` as the relative URI.
- **Discovery:** Always discover GraphQL queries, mutations, and types by reading the schema files directly in the backend modules. Do **NOT** rely on `scholario-api-tester` or external documentation for schema definitions.
- **Schema Locations:**
  - `identity-service/src/main/resources/graphql/schema.graphqls`
  - `book-service/src/main/resources/graphql/schema.graphqls`
  - `lending-service/src/main/resources/graphql/schema.graphqls`
  - `reservation-service/src/main/resources/graphql/schema.graphqls`
  - `approval-service/src/main/resources/graphql/schema.graphqls`
  - `course-service/src/main/resources/graphql/schema.graphqls`
  - `digital-content-service/src/main/resources/graphql/schema.graphqls`
  - `royalty-service/src/main/resources/graphql/schema.graphqls`
  - `notification-service/src/main/resources/graphql/schema.graphqls`
  - `analytics-service/src/main/resources/graphql/schema.graphqls`
  - `search-service/src/main/resources/graphql/schema.graphqls`
  - `audit-service/src/main/resources/graphql/schema.graphqls`

## Data Fetching Standards
- **Authenticity:** Always fetch data from the actual backend endpoints.
- **No Mocks:** Never write dummy or mock data for features. All components must be integrated with the GraphQL backend.
- **Completeness:** Ensure all implementations are thorough and complete. Do not leave placeholders, half-finished work, or "TODO" comments for backend integration.
- **Verification:** Before implementing a feature, verify the GraphQL operations using the schema definitions in the modules. Use `grep_search` or `glob` to find relevant types and fields in the `.graphqls` files.

## Workflow
- **Research:** Systematically map the backend schemas. Use `read_file` on the `.graphqls` files listed above to understand the available API.
- **Implementation:** Apply surgical changes to the frontend components to integrate them with the backend. Use the `client.ts` configuration in `scholario-frontend/src/graphql/client.ts`.
- **Validation:** Always verify that the feature works with the live backend.
