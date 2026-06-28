# Scholario React Frontend

This is the React web portal for Scholario, built using Vite, Material-UI, and Axios.

## Features

- **Portals:**
  - **Super Admin Portal:** Security audit log, assign user roles, manage departments.
  - **Librarian / Assistant Librarian Portal:** Circulation, inventory management, stock controls, and lending request approvals.
  - **Member Portal:** Book catalog search, user dashboards, issues/renewals, and course assignments.
- **Role Switching:** Interactive portal switcher allows administrators and developers to switch between `SUPER_ADMIN`, `LIBRARIAN`, `ASSISTANT_LIBRARIAN`, and `MEMBER` roles dynamically.
- **Axios-Based REST Client:** The application communicates entirely via standard REST endpoints configured through a proxy mapping backends (`/api/catalog`, `/api/lending`, `/api/member`, `/api/notification`).

## Running Locally

To start the development server, run:
```bash
npm run dev
```

To build for production, run:
```bash
npm run build
```
