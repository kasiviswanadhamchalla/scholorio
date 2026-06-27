# Keycloak Setup Guide for Scholario

This guide provides step-by-step instructions to configure Keycloak for the Scholario project.

## 1. Prerequisites

- Keycloak server (Docker or Standalone) running on `http://localhost:8080`.
- Access to the Keycloak Administration Console.

## 2. Create the Scholario Realm

1. Log in to the Keycloak Admin Console.
2. Click on the realm dropdown in the top-left corner and select **Create Realm**.
3. Name the realm: `scholario`.
4. Click **Create**.

## 3. Create Functional Roles

Scholario uses several roles to control access to different modules.

1. Navigate to **Realm Settings** -> **Roles**.
2. Click **Create Role** for each of the following:
   - `ADMIN`: Full system access.
   - `LIBRARIAN`: Access to lending, books, and inventory management.
   - `FACULTY`: Access to course management and content uploading.
   - `STUDENT`: Access to borrowing and viewing content.
   - `UNASSIGNED`: Default roles for new users before they are assigned a specific roles.

## 4. Create Clients

### 4.1 Frontend Client (Public)

This client is used by the React application.

1. Navigate to **Clients** -> **Create Client**.
2. **Client ID**: `scholario-frontend`.
3. **Client Protocol**: `openid-connect`.
4. **Access Type**: `public`.
5. **Valid Redirect URIs**: `http://localhost:5173/*` (Standard Vite dev port).
6. **Web Origins**: `*` (or `http://localhost:5173`).
7. **Capability Config**:
   - Authentication Flow: Standard flow (Check).
   - Direct Access Grants: (Check).

### 4.2 Backend Client (Confidential)

This client is used by the Spring Boot backend for admin tasks and roles synchronization.

1. Navigate to **Clients** -> **Create Client**.
2. **Client ID**: `scholario-backend`.
3. **Client Protocol**: `openid-connect`.
4. **Access Type**: `confidential`.
5. **Service Accounts Enabled**: `ON`.
6. **Authorization Enabled**: `OFF`.
7. Click **Save**.
8. Go to the **Credentials** tab and copy the **Client Secret**. You will need this for the backend `application.yml`.

## 5. User Synchronization & Mappers

Ensure that roles and user information are correctly mapped in the JWT token.

1. Go to **Client Scopes** -> **roles**.
2. Go to the **Mappers** tab.
3. Ensure **realm roles** mapper is present and configured to add to the ID token and Access token.
4. Ensure **client roles** mapper is present (if you choose to use client-specific roles).

*Note: The Scholario backend is configured to look for roles in both `realm_access` and `resource_access` claims.*

## 6. Create a Test User

1. Navigate to **Users** -> **Add user**.
2. **Username**: `testuser`.
3. **Email**: `test@scholario.com`.
4. **First Name**: `Test`.
5. **Last Name**: `User`.
6. Click **Create**.
7. Go to the **Credentials** tab and set a password (e.g., `password`). Turn **Temporary** to `OFF`.
8. Go to the **Role Mappings** tab.
9. Assign one or more roles (e.g., `STUDENT`, `ADMIN`).

## 7. Project Configuration

### 7.1 Backend (`config-local/identity-service.yml`)

Update the following properties if your setup differs:

```yaml
scholario:
  keycloak:
    admin:
      server-url: http://localhost:8080
      realm: scholario
      client-id: scholario-backend
      client-secret: <YOUR_CLIENT_SECRET>
```

### 7.2 Frontend (`scholario-frontend/.env`)

Create or update the `.env` file in the `scholario-frontend` directory:

```env
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=scholario
VITE_KEYCLOAK_CLIENT_ID=scholario-frontend
```

## 8. Running with Docker (Optional)

If you don't have Keycloak installed, you can run it using Docker:

```bash
docker run -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.0.0 \
  start-dev
```

## 9. Troubleshooting

- **CORS Errors**: Ensure `Web Origins` in the `scholario-frontend` client is set to `http://localhost:5173`.
- **Invalid Token**: Check the `issuer-uri` in the backend `application.yml`. It should match exactly what Keycloak reports in the `.well-known/openid-configuration` endpoint.
- **Roles missing**: Ensure the user has the roles assigned in the "Role Mappings" tab and that the "roles" scope is included in the login request.
