# Interview & Job Copilot

A monorepo for interview and job-prep automation: Spring Boot microservices, a Next.js frontend, an nginx gateway, shared auth libraries, and
Docker Compose for local orchestration.

## Vision / Purpose

Provide pragmatic, rule-driven interview preparation and job search assistance. Start with deterministic, template-based flows and allow LLM
enrichment only where it adds measurable value.

## High-Level Capabilities

- Candidate prep: plan generation, question banks, practice sessions, feedback loops.
- Job tracking: role ingestion, status updates, reminders, notes.
- Content delivery: templates for prep plans, scheduled emails/notifications, shareable links.
- Auth & identity: JWT-based auth utilities shared across services; gateway-level enforcement.
- Platform enablement: API gateway routing, service discovery configs, observability hooks.

## Repository Structure
```
.
├─ gradlew / gradlew.bat
├─ build.gradle.kts          # root config for all services
├─ settings.gradle.kts       # declares all Gradle projects
├─ services/
│  ├─ interview-service/     # Spring Boot, owns interview prep data
│  ├─ job-service/           # Spring Boot, owns job tracking data
│  ├─ notification-service/  # Spring Boot, outbound comms
│  └─ ...                    # additional independent services
├─ frontend/                 # Next.js app
├─ gateway/                  # nginx configs
├─ libs/
│  ├─ auth/                  # JWT/auth utilities
│  └─ ...                    # other shared libraries
├─ infra/
│  └─ docker-compose.yml     # local orchestration
└─ docs/                     # architecture and ADRs
```
## Architecture Principles

- Monorepo: single source of truth for APIs, contracts, and shared libs; synchronized changes land atomically; consistent tooling via one Gradle
  wrapper.
- Independent deployability: each service is its own Gradle project, owns its data store, and is releasable without waiting on others; gateway
  decouples clients from service topology.
- Data ownership: no shared databases; services expose APIs for cross-domain needs.
- MVP first: rule-based, template-driven prep flows; LLM enrichment is optional and isolated behind interfaces.

## Build & Tooling

- Gradle wrapper at root; one project per service.
- Examples:
  - Run all service builds: ./gradlew build
  - Build a single service: ./gradlew :services:interview-service:build
  - Run tests for a service: ./gradlew :services:job-service:test
  - Format/lint (if configured): ./gradlew spotlessApply or check

## Local Development Overview

- Start core stack (services, gateway, supporting deps) via Docker Compose in infra/.
- Develop a service locally:
  - ./gradlew :services:interview-service:bootRun
  - Point frontend or API clients at the gateway base URL exposed by Compose.
- Frontend dev:
  - From frontend/: install deps, npm run dev (or pnpm dev) with envs matching gateway endpoints.
- Configuration and secrets: use .env files and avoid committing secrets; see service-specific docs.

## Documentation Structure

- docs/ for architecture overviews, ADRs, and integration notes.
- Service-level READMEs within each services/*/.
- Frontend docs in frontend/README.md.
- Gateway configuration notes in gateway/README.md.

## Project Status

Early-stage MVP: core services scaffolded, rule-based flows prioritized; LLM features gated and optional.

## Explicit Non-Goals

- Shared databases across services.
- Premature ML-first features; no mandatory LLM dependency for the happy path.
- Tight coupling between frontend and individual services; gateway remains the contract surface.
- Multi-tenant complexity beyond what is needed for initial launch.

## License / Usage Note

Internal project; licensing and redistribution terms to be defined.