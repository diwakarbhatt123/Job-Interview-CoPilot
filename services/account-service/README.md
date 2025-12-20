# Account Service

Status: Skeleton (Milestone 0.1). No implementation yet.

Purpose: Handle account lifecycle and authentication concerns for Interview & Job Copilot. Owns its
data and exposes APIs only via the gateway.

Responsibilities

- Account registration, login/logout, and credential reset flows.
- JWT issuance/validation using shared auth utilities.
- Account-level metadata; audit/security hooks.

Non-Goals

- Frontend-specific UX flows (frontend calls gateway endpoints).
- Shared database with other services.
- Business logic from other domains (fit scoring, prep plans, job analysis).

Interfaces (planned)

- REST APIs surfaced through the gateway (auth endpoints).
- Outbound events/webhooks as needed (TBD).

Data Store (planned)

- Postgres (accounts as source of truth).

Tech Stack

- Java + Spring Boot
- Postgres

Build/Test/Run (when implemented)

- Build: `./gradlew :services:account-service:build`
- Test: `./gradlew :services:account-service:test`
- Run: `./gradlew :services:account-service:bootRun`

Configuration (placeholder)

- Env/secrets: DB creds and JWT signing keys (never committed).
- Database: service-owned schema; connection configured per environment.
