# Prep Plan Service

Status: Skeleton (Milestone 0.1). No implementation yet.

Purpose: Generate and manage interview preparation plans using templates first; LLM enrichment optional and isolated.

Responsibilities
- Create/update prep plans from templates and user/job context.
- Track plan progress/status and milestones.
- Expose plan data via the gateway.

Non-Goals
- Fit scoring logic (fit-score-service).
- Profile data ownership (profile-service).
- Shared database with other services.

Interfaces (planned)
- REST APIs via the gateway for CRUD and status updates.
- Optional notifications/events for plan milestones (TBD).

Data & External
- Structured plan storage in its own data store (TBD, likely Postgres via profile-service integration for persistence).
- Optional GPT usage to enrich task titles/descriptions; templates drive structure.

Tech Stack
- Java + Spring Boot
- GPT API (optional enrichment only)

Build/Test/Run (when implemented)
- Build: `./gradlew :services:prep-plan-service:build`
- Test: `./gradlew :services:prep-plan-service:test`
- Run: `./gradlew :services:prep-plan-service:bootRun`

Configuration (placeholder)
- Env/secrets: DB connection, GPT API key, feature flags for enrichment.
- Integration points: gateway routing; optional outbound notifications configured in `infra/`.
