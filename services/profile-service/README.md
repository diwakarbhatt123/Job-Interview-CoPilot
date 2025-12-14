# Profile Service

Status: Skeleton (Milestone 0.1). No implementation yet.

Purpose: Manage candidate profile data and preferences consumed by other services.

Responsibilities
- Store and serve candidate profile attributes and preferences.
- Provide profile reads/writes via the gateway.
- Supply downstream services (fit-score-service, prep-plan-service) with profile context.

Non-Goals
- Auth/account management (account-service).
- Fit scoring or job analysis logic.
- Shared database with other services.

Interfaces (planned)
- REST APIs via the gateway for CRUD.
- Optional events on profile changes (TBD).

Data Store (planned)
- Postgres (profiles, applications, associations to accounts).

Tech Stack
- Java + Spring Boot
- Postgres

Build/Test/Run (when implemented)
- Build: `./gradlew :services:profile-service:build`
- Test: `./gradlew :services:profile-service:test`
- Run: `./gradlew :services:profile-service:bootRun`

Configuration (placeholder)
- Env/secrets: DB connection.
- Gateway routing defined in `infra/`/`gateway/`.
