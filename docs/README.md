# DeskFlow — Documentation

This folder contains all project documentation, architecture diagrams, and decision records for the DeskFlow customer support platform.

---

## Contents

### 📋 [`plan.md`](./plan.md)
The full project implementation plan — technology stack, module map, directory structure, domain model, API surface, Kafka event catalog, SLA engine design, security model, build phases, and CI/CD pipeline.

---

### 🗂️ [`diagrams/`](./diagrams/)

Visual references for the system architecture. Diagrams are authored in [Mermaid](https://mermaid.js.org/) (`.mmd`) and/or exported as images (`.png` / `.svg`).

| File | Description |
|------|-------------|
| `architecture-overview.mmd` | High-level modular monolith module map |
| `domain-model.mmd` | Entity-relationship diagram of all core entities |
| `ticket-workflow.mmd` | State machine for ticket status transitions |
| `kafka-event-flow.mmd` | Kafka topics, producers, and consumer groups |
| `sla-escalation-flow.mmd` | SLA checker → breach → escalation sequence |
| `auth-flow.mmd` | JWT login / refresh / logout sequence diagram |
| `deployment-topology.mmd` | Docker Compose + Kubernetes deployment topology |

> Add diagrams here as each module is built. Keep Mermaid source files alongside any exported images.

---

### 📐 [`adr/`](./adr/)

Architecture Decision Records — lightweight docs capturing *why* a key technical decision was made, the alternatives considered, and the consequences.

| File | Decision |
|------|----------|
| `ADR-001-modular-monolith.md` | Why modular monolith over microservices |
| `ADR-002-kafka-for-events.md` | Why Kafka over RabbitMQ / direct HTTP |
| `ADR-003-jwt-stateless-auth.md` | JWT + Redis refresh vs. session-based auth |
| `ADR-004-flyway-migrations.md` | Flyway for schema management |
| `ADR-005-postgres-primary-db.md` | PostgreSQL as primary store |

> Create an ADR whenever a significant architectural choice is made that isn't self-evident from the code.

---

## Diagram Tooling

Render `.mmd` files locally:
```bash
# Install Mermaid CLI
npm install -g @mermaid-js/mermaid-cli

# Render a diagram to PNG
mmdc -i diagrams/architecture-overview.mmd -o diagrams/architecture-overview.png
```

Or paste the `.mmd` contents into [mermaid.live](https://mermaid.live) for an instant preview.