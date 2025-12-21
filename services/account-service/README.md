# Account Service

Status: Active (M1.1–M1.4 implemented).

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

Interfaces

- REST APIs surfaced through the gateway (auth endpoints).
- Outbound events/webhooks as needed (TBD).

Endpoints (current)

- `POST /auth/register` — create account
- `POST /auth/login` — authenticate and return JWT
- `GET /auth/authenticate` — gateway verification; returns `X-User-Id`

Data Store

- Postgres (accounts as source of truth).

Tech Stack

- Java + Spring Boot
- Postgres

Build/Test/Run

- Build: `./gradlew :services:account-service:build`
- Test: `./gradlew :services:account-service:test`
- Run: `./gradlew :services:account-service:bootRun`

Configuration

- Env/secrets: DB creds and JWT signing keys (never committed).
- JWT config: `auth.jwt.*` (issuer, expirationSeconds, key paths/aliases/password).
- Key material: private key in PKCS#12 for signing; public key PEM for verification.
