# Services

Backend services for Interview & Job Copilot. Each service is an independent Spring Boot project, owns its data, and is built via the root Gradle wrapper.

## Current Services (skeletons)
- `account-service`: auth/account concerns.
- `fit-score-service`: rule-based fit scoring.
- `job-analyzer-service`: job description ingestion/analysis.
- `prep-plan-service`: prep plan generation/management.
- `profile-service`: candidate profile data/preferences.

## Conventions
- One Gradle project per service; prefer consistent plugin/config across services.
- Google Java Style (IDE + future Gradle formatter).
- Services own their data; no shared DBs.
- External access goes through the gateway; avoid frontend coupling to service internals.
- Rule/template-driven first; LLM enrichment is optional and isolated.

## Build & Test
- Build all: `./gradlew build`
- Build one: `./gradlew :services:<service-name>:build`
- Test one: `./gradlew :services:<service-name>:test`

## Run Locally (when implementations exist)
- `./gradlew :services:<service-name>:bootRun`
- Access via the gateway endpoints configured in `infra/`/`gateway/`.

## Adding a New Service
1) Create `services/<new-service>/` with a Gradle build file and Spring Boot entrypoint.
2) Add the project to `settings.gradle`.
3) Define configs/environment in `infra/compose` and gateway routing in `gateway/`.
4) Document the service in `services/<new-service>/README.md`.

## Non-Goals
- Shared database schemas across services.
- Tight coupling between services and the frontend; the gateway is the contract surface.
