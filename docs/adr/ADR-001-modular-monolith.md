# ADR-001 — Modular Monolith over Microservices

| Field | Value |
|-------|-------|
| **Status** | Accepted |
| **Date** | 2026-05-27 |
| **Deciders** | Project team |

---

## Context

DeskFlow needs to be production-ready with clear domain boundaries (Auth, Tickets, Assignments, SLA, Escalation, Notifications, Analytics) but is being built by a small team and does not yet have the operational maturity (independent deploy pipelines, service mesh, distributed tracing across dozens of services) that microservices demand.

## Decision

Build DeskFlow as a **modular monolith**: a single deployable Spring Boot JAR internally organised as eight isolated domain modules. Each module owns its own entities, repositories, services, and controllers. Cross-module communication happens only through Kafka events (async) or Spring application events (sync in-process), never through direct repository or service bean injection across module boundaries.

## Alternatives Considered

| Option | Pros | Cons |
|--------|------|------|
| Microservices from day 1 | Maximum scalability | High operational overhead, network latency, distributed transactions, complex CI/CD for a small team |
| Classic layered monolith (no module separation) | Simplest to start | Quickly becomes a big ball of mud; hard to reason about domain boundaries |
| **Modular Monolith** ✅ | Clean domain boundaries, single deploy, easy to reason about, low operational overhead | Scaling must scale the whole app; requires discipline not to cross module boundaries |

## Consequences

- ✅ One Docker image to build, test, and deploy.
- ✅ No distributed transaction complexity — Kafka provides eventual consistency where needed.
- ✅ Domain boundaries enforced by package structure and module-level dependency rules.
- ✅ Can be split into microservices later by extracting modules one by one (Kafka topics already serve as the seam).
- ⚠️ Horizontal scaling scales all modules together — acceptable at this stage.
- ⚠️ Team must actively resist the temptation to call across module boundaries via direct bean injection.