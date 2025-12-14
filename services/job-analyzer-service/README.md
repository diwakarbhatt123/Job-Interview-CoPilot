# Job Analyzer Service

Status: Skeleton (Milestone 0.1). No implementation yet.

Purpose: Ingest and analyze job descriptions; extract structured attributes to feed other services.

Responsibilities
- Accept job descriptions and metadata via API.
- Parse/extract skills, seniority, and role attributes (rule-based first).
- Persist analysis results in its own data store.

Non-Goals
- Candidate profile ownership.
- Fit scoring logic (lives in fit-score-service).
- Shared database with other services.

Interfaces (planned)
- REST APIs via the gateway for ingestion and retrieval.
- Optional events to notify downstream consumers (TBD).

Data Store (planned)
- MongoDB (raw JD + structured fields; flexible schema).

Tech Stack
- Java + Spring Boot
- MongoDB

Build/Test/Run (when implemented)
- Build: `./gradlew :services:job-analyzer-service:build`
- Test: `./gradlew :services:job-analyzer-service:test`
- Run: `./gradlew :services:job-analyzer-service:bootRun`

Configuration (placeholder)
- Env/secrets: MongoDB connection, feature flags for enrichment.
- Gateway routing and any external parsing integrations configured via `infra/`.
