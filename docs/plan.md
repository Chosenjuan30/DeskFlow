# DeskFlow — Customer Support Platform
## Full-Stack Architecture & Implementation Plan

> **Architecture**: Modular Monolith backend (single deployable JAR) + React SPA frontend, built side-by-side in IntelliJ.  
> **Target**: Production-ready, containerised, observable, CI/CD-wired, role-driven UI.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Backend Module Map](#3-backend-module-map)
4. [Frontend Module Map](#4-frontend-module-map)
5. [Directory Structure](#5-directory-structure)
6. [Domain Model & Entity Design](#6-domain-model--entity-design)
7. [API Surface](#7-api-surface)
8. [Frontend Pages & Role Views](#8-frontend-pages--role-views)
9. [Kafka Event Catalog](#9-kafka-event-catalog)
10. [SLA & Escalation Engine](#10-sla--escalation-engine)
11. [Security Model](#11-security-model)
12. [Build Phases](#12-build-phases)
13. [Infrastructure & Deployment](#13-infrastructure--deployment)
14. [Testing Strategy](#14-testing-strategy)
15. [Observability](#15-observability)
16. [CI/CD Pipeline](#16-cicd-pipeline)

---

## 1. Project Overview

**DeskFlow** is a full-stack customer support ticketing platform. The backend exposes a REST + WebSocket API (Spring Boot modular monolith); the frontend is a role-aware React SPA. Both live in the same IntelliJ project and are developed in parallel, phase by phase.

| Dimension | Detail |
|-----------|--------|
| **Actors** | Customer, SupportAgent, Supervisor, Admin |
| **Core Workflow** | `OPEN → IN_PROGRESS → PENDING_CUSTOMER → RESOLVED → CLOSED` |
| **Event Triggers** | Ticket created → auto-assign; SLA deadline crossed → escalate |
| **Frontend** | React 18 + TypeScript + Vite — role-specific portals per actor |
| **Deployment** | Backend JAR + Frontend static build in Nginx, both in Docker Compose / Kubernetes |

---

## 2. Technology Stack

### Backend — Runtime
| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.3.x |
| Build | Maven | 3.9.x |

### Backend — Persistence
| Layer | Technology | Version |
|-------|-----------|---------|
| RDBMS | PostgreSQL | 16 |
| ORM | Spring Data JPA + Hibernate | 6.x |
| Migrations | Flyway | 10.x |
| Cache | Redis | 7.x (Spring Data Redis) |

### Backend — Messaging
| Layer | Technology | Version |
|-------|-----------|---------|
| Broker | Apache Kafka | 3.7 |
| Client | Spring Kafka | 3.x |
| Schema | JSON (Jackson) | — |

### Backend — Security
| Layer | Technology |
|-------|-----------|
| Auth | Spring Security 6 |
| Tokens | JWT — `io.jsonwebtoken` (jjwt 0.12.x) |
| Password | BCrypt |

### Backend — API & Docs
| Layer | Technology |
|-------|-----------|
| REST | Spring MVC |
| Real-time | Spring WebSocket (STOMP over SockJS) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI at `/swagger-ui.html`) |
| Validation | Jakarta Bean Validation (Hibernate Validator) |

### Backend — Observability
| Layer | Technology |
|-------|-----------|
| Metrics | Spring Actuator + Micrometer |
| Scraping | Prometheus |
| Dashboards | Grafana |
| Logging | Logback + Logstash JSON encoder |
| Tracing | OpenTelemetry + Zipkin |

### Backend — Testing
| Layer | Technology |
|-------|-----------|
| Unit | JUnit 5 + Mockito |
| Integration | Testcontainers (Postgres, Kafka, Redis) |
| API | MockMvc + RestAssured |
| Coverage | JaCoCo (≥ 80% line coverage gate in CI) |

---

### Frontend — Core
| Layer | Technology | Version |
|-------|-----------|---------|
| Language | TypeScript | 5.x |
| UI Framework | React | 18 |
| Build Tool | Vite | 5.x |
| Routing | React Router | v6 |

### Frontend — State & Data
| Layer | Technology | Purpose |
|-------|-----------|---------|
| Server State | TanStack Query (React Query) v5 | API calls, caching, refetching |
| Client State | Zustand | Auth token, user profile, UI flags |
| HTTP Client | Axios | JWT interceptor, auto-refresh |
| WebSocket | SockJS-client + @stomp/stompjs | Real-time notifications |

### Frontend — UI
| Layer | Technology | Purpose |
|-------|-----------|---------|
| Styling | Tailwind CSS v3 | Utility-first CSS |
| Components | shadcn/ui (Radix UI) | Accessible, unstyled primitives |
| Charts | Recharts | Analytics dashboards |
| Forms | React Hook Form + Zod | Typed forms + validation |
| Dates | date-fns | Date formatting |
| Icons | Lucide React | Icon set |
| Toasts | Sonner | Toast notifications |

### Frontend — Testing
| Layer | Technology | Purpose |
|-------|-----------|---------|
| Unit / Component | Vitest + React Testing Library | Component logic + renders |
| E2E | Playwright | Full user journey tests |
| Coverage | v8 via Vitest | ≥ 70% line coverage |

---

## 3. Backend Module Map

Each module is a Java package with its own sub-packages. No cross-module repository or service injection — only Kafka or Spring application events cross boundaries.

```
┌─────────────────────────────────────────────────────────────┐
│                      DeskFlow Monolith (JAR)                 │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌────────────┐               │
│  │   auth   │  │   user   │  │   ticket   │               │
│  └──────────┘  └──────────┘  └────────────┘               │
│                                                             │
│  ┌────────────┐  ┌──────────┐  ┌──────────────┐           │
│  │ assignment │  │   sla    │  │ notification │           │
│  └────────────┘  └──────────┘  └──────────────┘           │
│                                                             │
│  ┌────────────┐  ┌──────────────────────────┐              │
│  │ escalation │  │        analytics          │              │
│  └────────────┘  └──────────────────────────┘              │
│                                                             │
│  ┌────────────────────────────────────────────────┐        │
│  │                  Shared Kernel                  │        │
│  │  BaseEntity · DomainEvent · GlobalExHandler     │        │
│  │  SecurityConfig · JwtFilter · Utils             │        │
│  └────────────────────────────────────────────────┘        │
│                                                             │
│  ┌────────────────────────────────────────────────┐        │
│  │              Infrastructure Layer               │        │
│  │    KafkaProducer · CacheService · Flyway        │        │
│  └────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

Each module owns:
- `domain/` — JPA entities & value objects
- `repository/` — Spring Data JPA interfaces
- `service/` — business logic
- `controller/` — REST endpoints
- `dto/` — request & response records
- `event/` — domain events produced or consumed
- `exception/` — module-specific exceptions

---

## 4. Frontend Module Map

The frontend mirrors the backend module boundaries. Each frontend module owns its own API layer, hooks, components, and types — nothing is imported across module folders except from `shared/`.

```
┌─────────────────────────────────────────────────────────────┐
│                    DeskFlow React SPA                        │
│                                                             │
│  ┌────────┐  ┌─────────┐  ┌──────────────────────┐        │
│  │  auth  │  │ tickets │  │  agent-dashboard      │        │
│  └────────┘  └─────────┘  └──────────────────────┘        │
│                                                             │
│  ┌──────────────┐  ┌───────────┐  ┌────────────────┐      │
│  │    admin     │  │ analytics │  │ notifications  │      │
│  └──────────────┘  └───────────┘  └────────────────┘      │
│                                                             │
│  ┌────────────────────────────────────────────────┐        │
│  │                   Shared                        │        │
│  │  axiosInstance · queryClient · authStore        │        │
│  │  layouts · ProtectedRoute · RoleGuard           │        │
│  │  ui components (shadcn) · hooks · utils         │        │
│  └────────────────────────────────────────────────┘        │
│                                                             │
│  ┌────────────────────────────────────────────────┐        │
│  │                   Router                        │        │
│  │  Role-based route guards · lazy-loaded pages    │        │
│  └────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

Each frontend module owns:
- `api/` — Axios functions (typed request/response)
- `hooks/` — TanStack Query hooks wrapping the API
- `components/` — presentational + container components
- `types/` — TypeScript interfaces matching backend DTOs

---

## 5. Directory Structure

```
DeskFlow/
│
├── src/                                          ← BACKEND (Spring Boot)
│   ├── main/
│   │   ├── java/com/deskflow/
│   │   │   ├── DeskFlowApplication.java
│   │   │   │
│   │   │   ├── module/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── controller/AuthController.java
│   │   │   │   │   ├── dto/LoginRequest.java
│   │   │   │   │   ├── dto/LoginResponse.java
│   │   │   │   │   ├── dto/RefreshTokenRequest.java
│   │   │   │   │   ├── service/AuthService.java
│   │   │   │   │   └── service/TokenService.java
│   │   │   │   │
│   │   │   │   ├── user/
│   │   │   │   │   ├── controller/UserController.java
│   │   │   │   │   ├── domain/User.java
│   │   │   │   │   ├── domain/Role.java               ← enum
│   │   │   │   │   ├── domain/AgentProfile.java
│   │   │   │   │   ├── dto/CreateUserRequest.java
│   │   │   │   │   ├── dto/UserResponse.java
│   │   │   │   │   ├── repository/UserRepository.java
│   │   │   │   │   ├── repository/AgentProfileRepository.java
│   │   │   │   │   └── service/UserService.java
│   │   │   │   │
│   │   │   │   ├── ticket/
│   │   │   │   │   ├── controller/TicketController.java
│   │   │   │   │   ├── domain/Ticket.java
│   │   │   │   │   ├── domain/TicketStatus.java        ← enum
│   │   │   │   │   ├── domain/TicketPriority.java      ← enum
│   │   │   │   │   ├── domain/TicketCategory.java      ← enum
│   │   │   │   │   ├── domain/TicketStatusUpdate.java
│   │   │   │   │   ├── dto/CreateTicketRequest.java
│   │   │   │   │   ├── dto/UpdateTicketStatusRequest.java
│   │   │   │   │   ├── dto/TicketResponse.java
│   │   │   │   │   ├── event/TicketCreatedEvent.java
│   │   │   │   │   ├── event/TicketStatusChangedEvent.java
│   │   │   │   │   ├── repository/TicketRepository.java
│   │   │   │   │   ├── repository/TicketStatusUpdateRepository.java
│   │   │   │   │   └── service/TicketService.java
│   │   │   │   │
│   │   │   │   ├── assignment/
│   │   │   │   │   ├── controller/AssignmentController.java
│   │   │   │   │   ├── domain/AgentAssignment.java
│   │   │   │   │   ├── dto/AssignmentResponse.java
│   │   │   │   │   ├── event/AgentAssignedEvent.java
│   │   │   │   │   ├── repository/AgentAssignmentRepository.java
│   │   │   │   │   └── service/AssignmentService.java
│   │   │   │   │
│   │   │   │   ├── notification/
│   │   │   │   │   ├── controller/NotificationController.java
│   │   │   │   │   ├── domain/Notification.java
│   │   │   │   │   ├── domain/NotificationType.java
│   │   │   │   │   ├── dto/NotificationResponse.java
│   │   │   │   │   ├── repository/NotificationRepository.java
│   │   │   │   │   ├── service/NotificationService.java
│   │   │   │   │   ├── service/EmailService.java
│   │   │   │   │   └── service/WebSocketNotificationService.java
│   │   │   │   │
│   │   │   │   ├── sla/
│   │   │   │   │   ├── domain/SLAPolicy.java
│   │   │   │   │   ├── domain/SLAStatus.java
│   │   │   │   │   ├── dto/SLAPolicyRequest.java
│   │   │   │   │   ├── dto/SLAComplianceReport.java
│   │   │   │   │   ├── repository/SLAPolicyRepository.java
│   │   │   │   │   ├── service/SLAService.java
│   │   │   │   │   └── scheduler/SLACheckerScheduler.java
│   │   │   │   │
│   │   │   │   ├── escalation/
│   │   │   │   │   ├── controller/EscalationController.java
│   │   │   │   │   ├── domain/EscalationLog.java
│   │   │   │   │   ├── dto/EscalationResponse.java
│   │   │   │   │   ├── event/TicketEscalatedEvent.java
│   │   │   │   │   ├── repository/EscalationLogRepository.java
│   │   │   │   │   └── service/EscalationService.java
│   │   │   │   │
│   │   │   │   └── analytics/
│   │   │   │       ├── controller/AnalyticsController.java
│   │   │   │       ├── dto/AgentPerformanceReport.java
│   │   │   │       ├── dto/SLAComplianceSummary.java
│   │   │   │       ├── dto/TicketVolumeReport.java
│   │   │   │       └── service/AnalyticsService.java
│   │   │   │
│   │   │   ├── shared/
│   │   │   │   ├── config/
│   │   │   │   │   ├── KafkaConfig.java
│   │   │   │   │   ├── RedisConfig.java
│   │   │   │   │   ├── OpenApiConfig.java
│   │   │   │   │   └── WebSocketConfig.java
│   │   │   │   ├── security/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   └── CustomUserDetailsService.java
│   │   │   │   ├── event/
│   │   │   │   │   └── DomainEvent.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── BusinessRuleException.java
│   │   │   │   │   └── UnauthorizedException.java
│   │   │   │   ├── persistence/
│   │   │   │   │   └── BaseEntity.java
│   │   │   │   └── util/
│   │   │   │       ├── PageResponse.java
│   │   │   │       └── DateTimeUtil.java
│   │   │   │
│   │   │   └── infrastructure/
│   │   │       ├── kafka/
│   │   │       │   ├── KafkaProducerService.java
│   │   │       │   ├── consumer/AssignmentEventConsumer.java
│   │   │       │   ├── consumer/NotificationEventConsumer.java
│   │   │       │   └── consumer/EscalationEventConsumer.java
│   │   │       └── cache/
│   │   │           └── CacheService.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── db/migration/
│   │           ├── V1__create_users.sql
│   │           ├── V2__create_tickets.sql
│   │           ├── V3__create_assignments.sql
│   │           ├── V4__create_sla_policies.sql
│   │           ├── V5__create_escalation_logs.sql
│   │           ├── V6__create_notifications.sql
│   │           └── V7__seed_sla_policies.sql
│   │
│   └── test/
│       └── java/com/deskflow/
│           ├── module/auth/AuthServiceTest.java
│           ├── module/ticket/TicketServiceTest.java
│           ├── module/assignment/AssignmentServiceTest.java
│           ├── module/sla/SLAServiceTest.java
│           ├── integration/TicketFlowIntegrationTest.java
│           └── integration/SLAEscalationIntegrationTest.java
│
├── frontend/                                     ← FRONTEND (React + TS + Vite)
│   ├── src/
│   │   ├── modules/
│   │   │   │
│   │   │   ├── auth/
│   │   │   │   ├── api/authApi.ts
│   │   │   │   ├── components/LoginForm.tsx
│   │   │   │   ├── components/RegisterForm.tsx
│   │   │   │   ├── hooks/useLogin.ts
│   │   │   │   ├── hooks/useRegister.ts
│   │   │   │   └── types/auth.types.ts
│   │   │   │
│   │   │   ├── tickets/
│   │   │   │   ├── api/ticketsApi.ts
│   │   │   │   ├── components/TicketList.tsx
│   │   │   │   ├── components/TicketCard.tsx
│   │   │   │   ├── components/TicketDetail.tsx
│   │   │   │   ├── components/CreateTicketForm.tsx
│   │   │   │   ├── components/StatusBadge.tsx
│   │   │   │   ├── components/PriorityBadge.tsx
│   │   │   │   ├── components/TicketTimeline.tsx    ← status history
│   │   │   │   ├── components/TicketFilters.tsx
│   │   │   │   ├── hooks/useTickets.ts
│   │   │   │   ├── hooks/useTicketDetail.ts
│   │   │   │   └── types/ticket.types.ts
│   │   │   │
│   │   │   ├── agent-dashboard/
│   │   │   │   ├── api/agentApi.ts
│   │   │   │   ├── components/AgentQueue.tsx       ← assigned tickets list
│   │   │   │   ├── components/TicketWorkspace.tsx  ← detail + actions
│   │   │   │   ├── components/StatusUpdateForm.tsx
│   │   │   │   ├── components/AgentStats.tsx       ← load, resolved count
│   │   │   │   ├── hooks/useAgentQueue.ts
│   │   │   │   └── types/agent.types.ts
│   │   │   │
│   │   │   ├── admin/
│   │   │   │   ├── api/adminApi.ts
│   │   │   │   ├── components/UserTable.tsx
│   │   │   │   ├── components/CreateAgentModal.tsx
│   │   │   │   ├── components/SLAPolicyEditor.tsx
│   │   │   │   ├── components/SLAPolicyTable.tsx
│   │   │   │   ├── components/AgentExpertiseEditor.tsx
│   │   │   │   ├── hooks/useUsers.ts
│   │   │   │   ├── hooks/useSLAPolicies.ts
│   │   │   │   └── types/admin.types.ts
│   │   │   │
│   │   │   ├── supervisor/
│   │   │   │   ├── api/supervisorApi.ts
│   │   │   │   ├── components/EscalationList.tsx
│   │   │   │   ├── components/EscalationCard.tsx
│   │   │   │   ├── components/ReassignModal.tsx
│   │   │   │   ├── hooks/useEscalations.ts
│   │   │   │   └── types/supervisor.types.ts
│   │   │   │
│   │   │   ├── analytics/
│   │   │   │   ├── api/analyticsApi.ts
│   │   │   │   ├── components/KPICards.tsx
│   │   │   │   ├── components/TicketVolumeChart.tsx  ← Recharts BarChart
│   │   │   │   ├── components/SLAComplianceChart.tsx ← Recharts PieChart
│   │   │   │   ├── components/AgentPerformanceTable.tsx
│   │   │   │   ├── components/ResolutionTrendChart.tsx
│   │   │   │   ├── hooks/useAnalytics.ts
│   │   │   │   └── types/analytics.types.ts
│   │   │   │
│   │   │   └── notifications/
│   │   │       ├── api/notificationsApi.ts
│   │   │       ├── components/NotificationBell.tsx   ← header icon + badge
│   │   │       ├── components/NotificationDropdown.tsx
│   │   │       ├── components/NotificationItem.tsx
│   │   │       ├── hooks/useNotifications.ts
│   │   │       ├── hooks/useWebSocket.ts             ← STOMP connection
│   │   │       └── types/notification.types.ts
│   │   │
│   │   ├── shared/
│   │   │   ├── api/
│   │   │   │   ├── axiosInstance.ts                  ← interceptors, auto-refresh
│   │   │   │   └── queryClient.ts                    ← TanStack Query config
│   │   │   ├── store/
│   │   │   │   └── authStore.ts                      ← Zustand: token, user, logout
│   │   │   ├── components/
│   │   │   │   ├── ui/                               ← shadcn/ui primitives
│   │   │   │   │   ├── button.tsx
│   │   │   │   │   ├── input.tsx
│   │   │   │   │   ├── badge.tsx
│   │   │   │   │   ├── card.tsx
│   │   │   │   │   ├── dialog.tsx
│   │   │   │   │   ├── select.tsx
│   │   │   │   │   ├── table.tsx
│   │   │   │   │   ├── tabs.tsx
│   │   │   │   │   └── ...
│   │   │   │   ├── layouts/
│   │   │   │   │   ├── RootLayout.tsx                ← React Router outlet + providers
│   │   │   │   │   ├── DashboardLayout.tsx           ← sidebar + header shell
│   │   │   │   │   ├── AuthLayout.tsx                ← centered card shell
│   │   │   │   │   └── Sidebar.tsx                   ← role-aware nav links
│   │   │   │   ├── ProtectedRoute.tsx                ← redirect if no token
│   │   │   │   ├── RoleGuard.tsx                     ← redirect if wrong role
│   │   │   │   ├── LoadingSpinner.tsx
│   │   │   │   ├── ErrorBoundary.tsx
│   │   │   │   └── Pagination.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useCurrentUser.ts
│   │   │   │   └── usePagination.ts
│   │   │   ├── types/
│   │   │   │   └── common.types.ts                   ← PageResponse<T>, ApiError
│   │   │   └── utils/
│   │   │       ├── formatDate.ts
│   │   │       └── roleHelpers.ts
│   │   │
│   │   ├── pages/
│   │   │   ├── auth/
│   │   │   │   ├── LoginPage.tsx
│   │   │   │   └── RegisterPage.tsx
│   │   │   ├── customer/
│   │   │   │   ├── CustomerDashboardPage.tsx         ← open ticket count, recent
│   │   │   │   ├── SubmitTicketPage.tsx
│   │   │   │   ├── MyTicketsPage.tsx
│   │   │   │   └── TicketDetailPage.tsx              ← read-only for customer
│   │   │   ├── agent/
│   │   │   │   ├── AgentDashboardPage.tsx            ← queue + stats
│   │   │   │   └── AgentTicketDetailPage.tsx         ← editable workspace
│   │   │   ├── supervisor/
│   │   │   │   ├── SupervisorDashboardPage.tsx       ← all tickets + escalations
│   │   │   │   ├── EscalationsPage.tsx
│   │   │   │   └── SLACompliancePage.tsx
│   │   │   └── admin/
│   │   │       ├── AdminDashboardPage.tsx            ← KPIs + charts
│   │   │       ├── UserManagementPage.tsx
│   │   │       ├── SLAPoliciesPage.tsx
│   │   │       └── AnalyticsPage.tsx
│   │   │
│   │   ├── router/
│   │   │   ├── index.tsx                             ← createBrowserRouter
│   │   │   └── routes.ts                             ← route path constants
│   │   │
│   │   ├── App.tsx
│   │   └── main.tsx
│   │
│   ├── public/
│   │   └── favicon.ico
│   ├── index.html
│   ├── vite.config.ts                               ← proxy /api → :8080 in dev
│   ├── tailwind.config.ts
│   ├── tsconfig.json
│   ├── package.json
│   ├── .env.example                                 ← VITE_API_BASE_URL, VITE_WS_URL
│   └── vitest.config.ts
│
├── docs/                                            ← Documentation
│   ├── README.md
│   ├── plan.md                                      ← this file
│   ├── diagrams/
│   └── adr/
│
├── docker/
│   ├── kafka/server.properties
│   ├── postgres/init.sql
│   └── nginx/
│       ├── nginx.conf                               ← serve frontend + proxy /api
│       └── Dockerfile                               ← nginx + built frontend
│
├── k8s/
│   ├── namespace.yaml
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── hpa.yaml
│   ├── ingress.yaml
│   └── postgres-statefulset.yaml
│
├── .github/
│   └── workflows/
│       ├── ci-backend.yml
│       ├── ci-frontend.yml
│       └── cd.yml
│
├── docker-compose.yml                               ← full local stack
├── docker-compose.override.yml                      ← dev overrides
├── Dockerfile                                       ← backend multi-stage
├── .env.example
├── pom.xml
├── .gitignore
└── README.md
```

---

## 6. Domain Model & Entity Design

### BaseEntity (shared)
```
id           UUID         PK, auto-generated
created_at   TIMESTAMP    auto set on insert
updated_at   TIMESTAMP    auto set on update
```

### User
```
id            UUID
email         VARCHAR(255)  UNIQUE NOT NULL
password      VARCHAR(255)  NOT NULL (BCrypt)
first_name    VARCHAR(100)
last_name     VARCHAR(100)
role          ENUM(CUSTOMER, SUPPORT_AGENT, SUPERVISOR, ADMIN)
active        BOOLEAN       DEFAULT true
created_at / updated_at
```

### AgentProfile  *(one-to-one with User where role = SUPPORT_AGENT)*
```
id             UUID
user_id        UUID  FK → users
expertise      VARCHAR[]   maps to TicketCategory values
max_capacity   INT         default 10
current_load   INT         default 0
available      BOOLEAN     default true
```

### Ticket
```
id                 UUID
reference_no       VARCHAR(20)   UNIQUE  e.g. TKT-20240001
title              VARCHAR(255)
description        TEXT
category           ENUM(BILLING, TECHNICAL, GENERAL, ACCOUNT, SHIPPING)
priority           ENUM(LOW, MEDIUM, HIGH, CRITICAL)
status             ENUM(OPEN, IN_PROGRESS, PENDING_CUSTOMER, RESOLVED, CLOSED)
customer_id        UUID  FK → users
assigned_agent_id  UUID  FK → users (nullable)
sla_policy_id      UUID  FK → sla_policies
sla_deadline       TIMESTAMP
escalated          BOOLEAN   DEFAULT false
created_at / updated_at
```

### TicketStatusUpdate
```
id          UUID
ticket_id   UUID  FK → tickets
changed_by  UUID  FK → users
old_status  ENUM
new_status  ENUM
comment     TEXT
created_at
```

### AgentAssignment
```
id           UUID
ticket_id    UUID  FK → tickets  UNIQUE
agent_id     UUID  FK → users
assigned_at  TIMESTAMP
assigned_by  ENUM(SYSTEM, MANUAL)
notes        TEXT
```

### SLAPolicy
```
id                UUID
name              VARCHAR(100)
priority          ENUM   (one policy per priority level)
resolution_hours  INT    e.g. 4 for CRITICAL, 48 for LOW
warning_hours     INT    notify when this many hours remain
active            BOOLEAN
```

### EscalationLog
```
id            UUID
ticket_id     UUID  FK → tickets
escalated_to  UUID  FK → users (supervisor)
reason        TEXT
escalated_at  TIMESTAMP
resolved      BOOLEAN   DEFAULT false
resolved_at   TIMESTAMP (nullable)
```

### Notification
```
id            UUID
recipient_id  UUID  FK → users
type          ENUM(TICKET_ASSIGNED, STATUS_CHANGED, SLA_WARNING, ESCALATION, SYSTEM)
title         VARCHAR(255)
body          TEXT
channel       ENUM(IN_APP, EMAIL, BOTH)
read          BOOLEAN   DEFAULT false
ticket_id     UUID  (nullable, FK → tickets)
created_at
```

---

## 7. API Surface

All endpoints prefixed `/api/v1`. JWT Bearer required unless marked `[public]`.

### Auth  `/api/v1/auth`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/register` | [public] | Register customer account |
| POST | `/login` | [public] | Obtain access + refresh tokens |
| POST | `/refresh` | [public] | Rotate access token |
| POST | `/logout` | Any | Invalidate refresh token |
| GET | `/me` | Any | Return current user profile |

### Users  `/api/v1/users`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/` | ADMIN | List all users (paginated) |
| POST | `/agents` | ADMIN | Create support agent account |
| GET | `/{id}` | ADMIN | User detail |
| PATCH | `/{id}/activate` | ADMIN | Enable / disable account |
| GET | `/agents` | ADMIN, SUPERVISOR | Agents with load stats |
| PATCH | `/agents/{id}/expertise` | ADMIN | Update agent category expertise |

### Tickets  `/api/v1/tickets`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/` | CUSTOMER | Submit new ticket |
| GET | `/` | AGENT, SUPERVISOR, ADMIN | List all tickets (filterable) |
| GET | `/my` | CUSTOMER | Own tickets |
| GET | `/{id}` | Owner, Agent, Admin | Ticket detail + history |
| PATCH | `/{id}/status` | AGENT, SUPERVISOR | Advance status |
| POST | `/{id}/comments` | Agent, Customer | Add comment |
| GET | `/{id}/history` | Owner, Agent, Admin | Audit trail |

### Assignments  `/api/v1/assignments`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/{ticketId}/assign` | ADMIN, SUPERVISOR | Manually assign |
| POST | `/{ticketId}/reassign` | ADMIN, SUPERVISOR | Reassign |
| GET | `/agent/{agentId}` | AGENT (own), ADMIN | Agent's assignments |

### Notifications  `/api/v1/notifications`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/` | Any | Own notifications |
| PATCH | `/{id}/read` | Any | Mark one read |
| PATCH | `/read-all` | Any | Mark all read |

### SLA  `/api/v1/sla`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/policies` | ADMIN, SUPERVISOR | List policies |
| POST | `/policies` | ADMIN | Create policy |
| PATCH | `/policies/{id}` | ADMIN | Update policy |
| GET | `/compliance` | ADMIN, SUPERVISOR | Compliance report |

### Escalations  `/api/v1/escalations`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/` | SUPERVISOR, ADMIN | Open escalations |
| POST | `/{ticketId}/escalate` | SUPERVISOR | Manual escalate |
| PATCH | `/{id}/resolve` | SUPERVISOR | Resolve escalation |

### Analytics  `/api/v1/analytics`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/dashboard` | ADMIN | Top-level KPIs |
| GET | `/agents/performance` | ADMIN, SUPERVISOR | Per-agent stats |
| GET | `/tickets/volume` | ADMIN | Volume by category/priority/date |
| GET | `/sla/summary` | ADMIN | SLA compliance %, breaches |

### WebSocket  `ws://host/ws`
- Endpoint: `/ws` (SockJS)
- Subscribe: `/user/queue/notifications` — personal real-time push
- Subscribe: `/topic/dashboard` — live admin dashboard updates

---

## 8. Frontend Pages & Role Views

### Role Routing Rules
| Role | Default redirect after login | Accessible pages |
|------|------------------------------|-----------------|
| CUSTOMER | `/customer/dashboard` | Customer pages only |
| SUPPORT_AGENT | `/agent/dashboard` | Agent pages + notifications |
| SUPERVISOR | `/supervisor/dashboard` | Agent + supervisor + analytics |
| ADMIN | `/admin/dashboard` | Everything |

---

### Customer Portal
| Page | Route | Key Components |
|------|-------|---------------|
| Login | `/login` | `LoginForm` — email/password → JWT stored in Zustand |
| Register | `/register` | `RegisterForm` — name, email, password, Zod validation |
| My Dashboard | `/customer/dashboard` | Open tickets count card, recent tickets list, quick "New Ticket" button |
| Submit Ticket | `/customer/tickets/new` | `CreateTicketForm` — title, description, category (select), priority (select) |
| My Tickets | `/customer/tickets` | `TicketList` with status filter tabs (All / Open / In Progress / Resolved) |
| Ticket Detail | `/customer/tickets/:id` | `TicketDetail` (read-only) + `TicketTimeline` (status history) |

---

### Support Agent Portal
| Page | Route | Key Components |
|------|-------|---------------|
| Agent Dashboard | `/agent/dashboard` | `AgentStats` (load, resolved today, SLA warning count) + `AgentQueue` (assigned tickets) |
| Ticket Workspace | `/agent/tickets/:id` | Full ticket detail, `StatusUpdateForm` (dropdown: next allowed status + comment), ticket timeline |

---

### Supervisor Portal
| Page | Route | Key Components |
|------|-------|---------------|
| Supervisor Dashboard | `/supervisor/dashboard` | All tickets list with advanced filters (status, priority, category, agent, date range) |
| Escalations | `/supervisor/escalations` | `EscalationList` with open/resolved tabs, `ReassignModal` |
| SLA Compliance | `/supervisor/sla` | Date-range SLA compliance table by priority |

---

### Admin Portal
| Page | Route | Key Components |
|------|-------|---------------|
| Admin Dashboard | `/admin/dashboard` | `KPICards` (total tickets, open, SLA breach rate, avg resolution time) + `TicketVolumeChart` (bar, last 30 days) + `SLAComplianceChart` (pie) |
| Analytics | `/admin/analytics` | `AgentPerformanceTable`, `ResolutionTrendChart`, date range pickers |
| User Management | `/admin/users` | `UserTable` with role filter, `CreateAgentModal`, activate/deactivate toggle |
| SLA Policies | `/admin/sla` | `SLAPolicyTable`, inline `SLAPolicyEditor` modal (create/edit per priority) |

---

### Shared UI Elements (all roles)
- **Header bar** — DeskFlow logo, current user name, `NotificationBell` with unread count badge
- **Sidebar** — role-aware navigation links (hidden on auth pages)
- **`NotificationDropdown`** — slides down from bell; lists recent notifications, mark read, click-to-navigate to ticket
- **Real-time** — on login, WebSocket connects to `/ws`; incoming STOMP messages append to notification list and update the bell badge without a page reload

---

## 9. Kafka Event Catalog

### Topics
| Topic | Partitions | Retention | Description |
|-------|-----------|-----------|-------------|
| `deskflow.tickets.created` | 6 | 7d | New ticket submitted |
| `deskflow.tickets.status-changed` | 6 | 7d | Every status transition |
| `deskflow.assignments.created` | 3 | 7d | Agent assigned |
| `deskflow.sla.warning` | 3 | 3d | Warning threshold crossed |
| `deskflow.sla.breached` | 3 | 3d | Deadline crossed → escalate |
| `deskflow.escalations.created` | 3 | 7d | Escalation created |
| `deskflow.notifications.send` | 6 | 1d | Internal notification fan-out |

### Key Event Schemas

#### `TicketCreatedEvent`
```json
{
  "eventId": "uuid", "occurredAt": "2024-01-15T10:30:00Z",
  "ticketId": "uuid", "referenceNo": "TKT-20240001",
  "category": "TECHNICAL", "priority": "HIGH", "customerId": "uuid"
}
```

#### `TicketStatusChangedEvent`
```json
{
  "eventId": "uuid", "occurredAt": "2024-01-15T11:00:00Z",
  "ticketId": "uuid", "oldStatus": "OPEN", "newStatus": "IN_PROGRESS",
  "changedBy": "uuid", "customerId": "uuid", "agentId": "uuid"
}
```

#### `SLABreachedEvent`
```json
{
  "eventId": "uuid", "occurredAt": "2024-01-15T14:00:00Z",
  "ticketId": "uuid", "priority": "HIGH",
  "slaDeadline": "2024-01-15T13:00:00Z",
  "currentStatus": "IN_PROGRESS", "assignedAgentId": "uuid"
}
```

### Consumer Groups
| Consumer Group | Topics Consumed | Action |
|----------------|----------------|--------|
| `assignment-service` | `tickets.created` | Run assignment algorithm |
| `notification-service` | `status-changed`, `assignments.created`, `sla.warning`, `sla.breached`, `escalations.created` | Persist + push |
| `escalation-service` | `sla.breached` | Create EscalationLog, notify supervisor |
| `analytics-service` | `tickets.created`, `status-changed` | Update aggregate stats |

---

## 10. SLA & Escalation Engine

### SLA Policy Resolution
1. Ticket created → look up active `SLAPolicy` for that priority
2. `sla_deadline = created_at + resolution_hours`
3. Deadline stored on `Ticket` entity

### SLA Checker Scheduler *(runs every 60 s)*
- Query all `OPEN / IN_PROGRESS / PENDING_CUSTOMER` tickets
- `now > deadline - warning_hours` AND not warned → `sla.warning` event
- `now > deadline` AND not escalated → `sla.breached`, set `ticket.escalated = true`

### Escalation Flow
```
sla.breached event
      ↓
EscalationEventConsumer
      ↓
EscalationService.escalate(ticketId)
  → find Supervisor (round-robin among active supervisors)
  → create EscalationLog
  → publish escalations.created
      ↓
NotificationEventConsumer
  → in-app + email to Supervisor
  → in-app to assigned Agent
  → WebSocket push to both
```

---

## 11. Security Model

### JWT Strategy
| Token | TTL | Storage | Notes |
|-------|-----|---------|-------|
| Access | 15 min | Zustand memory (never localStorage) | HS512 signed |
| Refresh | 7 days | Redis `refresh:{userId}` | Hash compared on rotate |
| Blacklist | Until access expires | Redis `blacklist:{jti}` | Set on logout |

### Axios JWT Interceptor (Frontend)
```
Request interceptor  → attach Authorization: Bearer <accessToken>
Response interceptor → on 401: call /auth/refresh, retry original request once
                       on second 401: clear Zustand, redirect to /login
```

### Role-Based Access Control
| Role | Capabilities |
|------|-------------|
| `CUSTOMER` | Submit + view own tickets, own notifications |
| `SUPPORT_AGENT` | View/update assigned tickets, notifications |
| `SUPERVISOR` | All agent capabilities + all tickets, escalations, reassign, SLA reports |
| `ADMIN` | Full access: user management, SLA config, analytics, all of above |

### Frontend Route Guards
- `ProtectedRoute` — wraps all authenticated routes; reads Zustand token, redirects to `/login` if absent
- `RoleGuard` — wraps role-specific route groups; reads user role, redirects to role home if unauthorised

---

## 12. Build Phases

> Each phase delivers a working vertical slice: backend API + frontend UI together.  
> Backend and frontend tasks are listed side by side for each phase.

---

### Phase 0 — Project Bootstrap  *(2–3 days)*

**Backend**
- [ ] Convert IntelliJ project to Maven (`pom.xml` with Spring Boot parent + all deps)
- [ ] `application.yml` (dev profile) — datasource, Redis, Kafka, JWT secret
- [ ] `docker-compose.yml` — PostgreSQL, Redis, Kafka, Zookeeper, Kafka UI, MailHog
- [ ] `BaseEntity`, `GlobalExceptionHandler`, `OpenApiConfig`
- [ ] `GET /actuator/health` returns UP

**Frontend**
- [ ] Scaffold Vite + React + TypeScript project inside `frontend/`
- [ ] Install and configure: Tailwind CSS, shadcn/ui, React Router v6, Zustand, TanStack Query, Axios, SockJS + STOMP, Recharts, React Hook Form + Zod, date-fns, Lucide, Sonner
- [ ] `vite.config.ts` — dev proxy: `/api → http://localhost:8080`
- [ ] `RootLayout`, `AuthLayout`, `DashboardLayout`, `Sidebar` shell
- [ ] `ProtectedRoute` + `RoleGuard` skeletons
- [ ] `axiosInstance.ts` with base URL from env
- [ ] Verify: Vite dev server starts, proxy reaches Spring Boot health endpoint

**Done when**: Docker stack is up, Spring Boot returns 200 on `/actuator/health`, Vite dev server proxies to it.

---

### Phase 1 — Identity & Access  *(3–4 days)*


**Backend**
- [ ] Flyway V1 — `users`, `agent_profiles` tables
- [ ] `User`, `AgentProfile` JPA entities
- [ ] `UserRepository`, `UserService`
- [ ] `AuthService` — register, login, refresh, logout (Redis)
- [ ] `JwtTokenProvider` — issue / validate / extract
- [ ] `JwtAuthenticationFilter` → Spring Security filter chain
- [ ] `SecurityConfig` — public routes, CORS (`localhost:5173`), stateless
- [ ] Unit tests: `AuthServiceTest`, `JwtTokenProviderTest`
- [ ] Integration test: `POST /auth/login` returns valid token

**Frontend**
- [ ] `authStore.ts` (Zustand) — `accessToken`, `user`, `setAuth`, `clearAuth`
- [ ] `authApi.ts` — `login()`, `register()`, `refresh()`, `logout()`, `me()`
- [ ] Axios request + response interceptors (token attach, auto-refresh on 401)
- [ ] `LoginPage.tsx` — React Hook Form + Zod schema, calls `login()`, stores token
- [ ] `RegisterPage.tsx` — customer self-registration
- [ ] `ProtectedRoute` — reads `authStore`, redirects if no token
- [ ] `RoleGuard` — reads `authStore.user.role`, redirects to role home
- [ ] Router: `/login`, `/register`, and base role redirects wired up
- [ ] `GET /auth/me` called on app mount to hydrate user from valid token

**Done when**: A user can register, log in, receive a JWT, and the frontend stores it + re-validates on reload.

---

### Phase 2 — Ticket Core  *(4–5 days)*

**Backend**
- [ ] Flyway V2 — `tickets`, `ticket_status_updates` tables
- [ ] Enums: `TicketStatus`, `TicketPriority`, `TicketCategory`
- [ ] `Ticket`, `TicketStatusUpdate` entities
- [ ] Reference number generator (`TKT-YYYYNNNNN`)
- [ ] `TicketService` — create, query (paged + filterable), status transition with guard
- [ ] Allowed transitions enforced in service layer:
  ```
  OPEN           → IN_PROGRESS        (agent)
  IN_PROGRESS    → PENDING_CUSTOMER   (agent)
  PENDING_CUSTOMER → IN_PROGRESS      (agent)
  IN_PROGRESS    → RESOLVED           (agent)
  RESOLVED       → CLOSED             (system after 72 h / customer)
  ```
- [ ] `TicketController` — full REST surface
- [ ] Publish `TicketCreatedEvent` + `TicketStatusChangedEvent` on Kafka
- [ ] Unit tests: transition guards, reference number, pagination
- [ ] Integration test: full create-to-resolve flow

**Frontend**
- [ ] `ticket.types.ts` — `Ticket`, `TicketStatus`, `TicketPriority`, `TicketCategory`
- [ ] `ticketsApi.ts` — `createTicket()`, `getMyTickets()`, `getTicket()`, `updateStatus()`
- [ ] `useTickets.ts`, `useTicketDetail.ts` — TanStack Query hooks
- [ ] `SubmitTicketPage` — form (title, description, category select, priority select), submit → toast
- [ ] `MyTicketsPage` — paginated list with status filter tabs, `StatusBadge`, `PriorityBadge`
- [ ] `TicketDetail` (customer, read-only) — ticket metadata + `TicketTimeline` (status history)
- [ ] `CustomerDashboardPage` — open ticket count card + recent tickets list

**Done when**: Customer can submit a ticket and see it in their list with correct status and priority badges.

---

### Phase 3 — Assignment Engine  *(2–3 days)*

**Backend**
- [ ] Flyway V3 — `agent_assignments` table
- [ ] `AgentAssignment` entity
- [ ] `AssignmentService` — auto-assign algorithm:
  1. Filter agents with matching expertise
  2. Pick lowest `current_load` who is `available`
  3. Fallback: any available agent, lowest load
  4. Increment `AgentProfile.current_load`
- [ ] `AssignmentEventConsumer` — consumes `tickets.created`
- [ ] Manual assign / reassign endpoints
- [ ] Publish `AgentAssignedEvent`
- [ ] Unit tests: algorithm edge cases (no agents, all at capacity)

**Frontend**
- [ ] `agentApi.ts` — `getAgentQueue()`, `getAssignment(ticketId)`
- [ ] `useAgentQueue.ts` — TanStack Query, polls or invalidates on WS event
- [ ] `AgentDashboardPage` — `AgentStats` widget (load, resolved today) + `AgentQueue` list
- [ ] `AgentTicketDetailPage` — full ticket view, placeholder `StatusUpdateForm` (enabled in Phase 4)
- [ ] Admin: `adminApi.ts` — `assignTicket()`, `reassignTicket()`; `ReassignModal.tsx` stub

**Done when**: Submitting a ticket automatically assigns it to an available agent; agent can see it in their queue.

---

### Phase 4 — Notification System  *(3–4 days)*

**Backend**
- [ ] Flyway V6 — `notifications` table
- [ ] `Notification` entity
- [ ] `NotificationService` — persist + push
- [ ] `EmailService` — JavaMailSender + Thymeleaf HTML templates
- [ ] `WebSocketConfig` — STOMP `/ws` endpoint, simple in-memory message broker
- [ ] `WebSocketNotificationService` — `SimpMessagingTemplate` → `/user/queue/notifications`
- [ ] `NotificationEventConsumer` — fans out from all relevant Kafka topics
- [ ] Notification REST: list, mark read, mark all read
- [ ] Integration test: status change → notification persisted + WS message fired

**Frontend**
- [ ] `useWebSocket.ts` — SockJS + STOMP connect on login, disconnect on logout, subscribe to `/user/queue/notifications`
- [ ] Incoming WS message → invalidate `useNotifications` query + increment bell badge
- [ ] `NotificationBell.tsx` — unread count badge in header
- [ ] `NotificationDropdown.tsx` — slide-down list, click → navigate to ticket, mark read
- [ ] `notificationsApi.ts` + `useNotifications.ts` hook
- [ ] `StatusUpdateForm` in `AgentTicketDetailPage` — enabled: dropdown (allowed next statuses) + comment textarea + submit

**Done when**: Agent updates a ticket status → customer receives an in-app notification in real-time (bell badge increments, dropdown shows the notification).

---

### Phase 5 — SLA Engine  *(2–3 days)*

**Backend**
- [ ] Flyway V4 — `sla_policies` table; V7 — seed (CRITICAL=4h, HIGH=8h, MEDIUM=24h, LOW=48h)
- [ ] `SLAPolicy` entity
- [ ] `SLAService` — calculate deadline on ticket creation
- [ ] `SLACheckerScheduler` — 60-second fixed-delay
- [ ] Publish `sla.warning` and `sla.breached` events
- [ ] Admin CRUD endpoints for policies
- [ ] SLA compliance report endpoint
- [ ] Unit tests: deadline calc, warning threshold, breach detection

**Frontend**
- [ ] SLA deadline display on `TicketDetail` and `AgentTicketDetailPage` — coloured countdown (green → amber → red)
- [ ] `SLAPoliciesPage` (Admin) — `SLAPolicyTable` + `SLAPolicyEditor` modal (create/edit)
- [ ] `SLACompliancePage` (Supervisor) — date-range picker + compliance table by priority
- [ ] Notifications: SLA warning toasts on agent dashboard when broker pushes the event

**Done when**: Tickets show SLA deadline; admin can edit policies; SLA warning notifications arrive in real-time.

---

### Phase 6 — Escalation Module  *(2 days)*

**Backend**
- [ ] Flyway V5 — `escalation_logs` table
- [ ] `EscalationLog` entity
- [ ] `EscalationService` — supervisor selection (round-robin), log creation
- [ ] `EscalationEventConsumer` — consumes `sla.breached`
- [ ] Manual escalation endpoint (Supervisor)
- [ ] Escalation resolution endpoint
- [ ] Unit tests: supervisor routing, escalation flow

**Frontend**
- [ ] `EscalationsPage` (Supervisor) — open/resolved tabs, `EscalationCard` (ticket ref, reason, time escalated)
- [ ] Resolve escalation button → PATCH → invalidate query
- [ ] `SupervisorDashboardPage` — escalation count KPI card + link to escalations page
- [ ] Admin: show escalated badge on tickets in all-tickets list

**Done when**: A breached SLA auto-escalates; supervisor sees the escalation and can mark it resolved.

---

### Phase 7 — Analytics Module  *(2–3 days)*

**Backend**
- [ ] `AnalyticsService` — JPQL/native queries:
  - Ticket volume by date, category, priority
  - Average resolution time per agent
  - SLA compliance % by priority + date range
  - Agent performance: tickets resolved, avg handle time
- [ ] `AnalyticsController` — 4 endpoints
- [ ] `@Cacheable` on all analytics endpoints (Redis, TTL 5 min)
- [ ] Integration test: analytics after seeded data

**Frontend**
- [ ] `AnalyticsPage` (Admin) — `AgentPerformanceTable`, `ResolutionTrendChart`, date range pickers
- [ ] `AdminDashboardPage` — `KPICards` (total, open, SLA breach rate, avg resolution), `TicketVolumeChart` (BarChart, 30-day), `SLAComplianceChart` (PieChart by priority)
- [ ] `analyticsApi.ts` + `useAnalytics.ts` hooks with 5-min stale time (matches Redis cache)

**Done when**: Admin dashboard renders live charts from real data.

---

### Phase 8 — Production Hardening  *(3–4 days)*

**Backend**
- [ ] Rate limiting — Bucket4j (per-IP, per-user, per-endpoint)
- [ ] Request/response logging filter — MDC `traceId`, `userId`, `ticketId`
- [ ] Global exception handler — RFC 7807 Problem Details JSON
- [ ] Input sanitisation — HTML strip on text fields
- [ ] Pagination enforced — max page size 100
- [ ] Actuator secured — only `/actuator/health` public
- [ ] Multi-stage Dockerfile (builder → JRE 21-slim)
- [ ] k8s manifests — deployment, HPA, ingress, secrets
- [ ] GitHub Actions `ci-backend.yml` (build + test + JaCoCo gate)

**Frontend**
- [ ] Error boundaries on all page-level components
- [ ] 404 and unauthorised fallback pages
- [ ] Accessibility pass — ARIA labels, keyboard nav, focus ring (Radix handles most)
- [ ] Bundle analysis (`vite-bundle-visualizer`) — lazy-load heavy pages
- [ ] `frontend/Dockerfile` — `node:20-alpine` build stage → `nginx:alpine` serve stage
- [ ] `docker/nginx/nginx.conf` — serve `dist/`, proxy `/api` and `/ws` to backend
- [ ] GitHub Actions `ci-frontend.yml` (type-check + Vitest + Playwright smoke)
- [ ] `cd.yml` — build both images, push to registry, deploy to k8s

**Done when**: Both Docker images build cleanly, CI passes, Playwright smoke test logs in and creates a ticket end-to-end.

---

## 13. Infrastructure & Deployment

### docker-compose.yml (local full-stack)
```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment: { POSTGRES_DB: deskflow, POSTGRES_USER: deskflow, POSTGRES_PASSWORD: deskflow_secret }
    ports: ["5432:5432"]
    volumes: [pgdata:/var/lib/postgresql/data]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment: { ZOOKEEPER_CLIENT_PORT: 2181 }

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    ports: ["8090:8080"]
    environment: { KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092 }

  mailhog:
    image: mailhog/mailhog
    ports: ["1025:1025", "8025:8025"]   # 8025 = web UI

  backend:
    build: .
    ports: ["8080:8080"]
    depends_on: [postgres, redis, kafka]
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DATABASE_URL: jdbc:postgresql://postgres:5432/deskflow
      REDIS_HOST: redis
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      MAIL_HOST: mailhog

  frontend:
    build: ./frontend
    ports: ["3000:80"]                  # nginx serving built React
    depends_on: [backend]
```

### Backend Dockerfile (multi-stage)
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S deskflow && adduser -S deskflow -G deskflow
COPY --from=builder /app/target/deskflow-*.jar app.jar
USER deskflow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Dockerfile (multi-stage)
```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json .
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY ../docker/nginx/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### Nginx Config (`docker/nginx/nginx.conf`)
```nginx
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;

  # SPA fallback
  location / {
    try_files $uri $uri/ /index.html;
  }

  # Proxy REST API
  location /api/ {
    proxy_pass http://backend:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }

  # Proxy WebSocket
  location /ws {
    proxy_pass http://backend:8080/ws;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
  }
}
```

### Kubernetes (production)
- Backend `Deployment` — 2 replicas min, `RollingUpdate`, HPA 2→10 on CPU 70%
- Frontend `Deployment` — nginx static, 2 replicas, lightweight
- `Ingress` — nginx with TLS termination; routes `/api` and `/ws` to backend service, everything else to frontend service
- `ConfigMap` — non-secret env vars
- `Secret` — DB password, JWT secret, mail credentials (sealed-secrets pattern)
- `PodDisruptionBudget` — min 1 available per deployment during rolling updates
- `PostgreSQL` — StatefulSet or managed (Cloud SQL / RDS / Supabase)
- `Redis` — managed (Elasticache / Cloud Memorystore)
- `Kafka` — managed (Confluent Cloud / MSK) or Strimzi operator

---

## 14. Testing Strategy

### Backend — Unit Tests (JUnit 5 + Mockito)
- Every `*Service` has a `*ServiceTest`; all external deps mocked
- Test: status transition guards, SLA deadline calc, assignment algorithm edge cases
- Target: ≥ 80% line coverage (JaCoCo gate in CI)

### Backend — Integration Tests (Testcontainers)
- `@SpringBootTest` with Postgres, Redis, Kafka containers spun up per test class
- `MockMvc` / `RestAssured` for HTTP flows
- Kafka consumer tests: publish event, assert consumer side-effects in DB

### Frontend — Unit / Component Tests (Vitest + React Testing Library)
- Test hooks in isolation (mock Axios, assert TanStack Query state)
- Test form validation (Zod schemas, error messages)
- Test `ProtectedRoute` and `RoleGuard` redirect behaviour
- Target: ≥ 70% line coverage

### Frontend — E2E Tests (Playwright)
- Full stack running in Docker Compose
- Key journeys:
  - Customer registers → logs in → submits ticket → sees it in My Tickets
  - Agent logs in → sees ticket in queue → updates status → customer notification appears
  - Admin logs in → views analytics dashboard → charts render

### Key Scenarios
| Test | Validates |
|------|-----------|
| Ticket created → Kafka → agent assigned | Assignment engine + event flow |
| Agent updates status → WS push → customer notification | WebSocket + notification module |
| SLA deadline crosses → escalation created | SLA scheduler + escalation |
| Invalid status transition → 422 | Business rule guard |
| JWT expired → 401 → auto-refresh → retry | Axios interceptor |
| Non-owner accessing ticket → 403 | Ownership check |
| Login → submit ticket → see in list (E2E) | Full stack vertical |

---

## 15. Observability

### Metrics (Prometheus + Grafana)
Custom `MeterRegistry` metrics:
- `deskflow_tickets_created_total` (counter, tags: category, priority)
- `deskflow_tickets_resolution_seconds` (histogram)
- `deskflow_sla_breaches_total` (counter, tags: priority)
- `deskflow_assignments_duration_ms` (timer)

### Structured Logging (Logback JSON)
Every log line includes: `traceId`, `userId`, `ticketId`, `module`.

### Distributed Tracing
- OpenTelemetry Java agent attached at container startup
- Trace context propagated through Kafka headers
- Zipkin backend (or OTLP → Tempo)

---

## 16. CI/CD Pipeline

### `ci-backend.yml`
```
Trigger: push / PR to main or develop
Steps: checkout → JDK 21 setup → Maven cache → mvn verify (JaCoCo gate) → upload coverage
```

### `ci-frontend.yml`
```
Trigger: push / PR to main or develop
Steps: checkout → Node 20 setup → npm ci → tsc --noEmit → vitest run → playwright test (Docker Compose)
```

### `cd.yml`
```
Trigger: push to main (both CI jobs pass)
Steps:
  1. Docker login (GHCR)
  2. Build + tag backend image (SHA + latest)
  3. Build + tag frontend image (SHA + latest)
  4. Push both images
  5. [Staging] kubectl apply → staging namespace, run smoke tests
  6. [Production] kubectl apply → prod namespace (manual approval gate required)
```

---

## Milestones Summary

| Phase | Backend deliverable | Frontend deliverable | Est. Days |
|-------|--------------------|--------------------|-----------|
| 0 | Spring Boot boots, Docker stack up | Vite app starts, proxies to backend | 2–3 |
| 1 | Auth API (register, login, JWT) | Login/Register pages + JWT storage + route guards | 3–4 |
| 2 | Ticket CRUD + status workflow | Customer portal (submit, list, detail) | 4–5 |
| 3 | Auto-assignment engine | Agent queue dashboard | 2–3 |
| 4 | Notifications + WebSocket | Real-time bell, status update form | 3–4 |
| 5 | SLA engine + scheduler | SLA deadline UI + admin policy editor | 2–3 |
| 6 | Escalation module | Supervisor escalation panel | 2 |
| 7 | Analytics API + Redis cache | Admin charts + KPI dashboard | 2–3 |
| 8 | Rate limiting, k8s, CI/CD hardening | Error boundaries, nginx Dockerfile, CI/CD | 3–4 |
| **Total** | | | **~23–31 days** |

---

## Next Steps (immediate — Phase 0)

**Backend**
1. Replace stub `Main.java` with `DeskFlowApplication.java` (`@SpringBootApplication`)
2. Write `pom.xml` with Spring Boot parent + all backend dependencies
3. Write `application.yml`, `application-dev.yml`
4. Write `docker-compose.yml` — spin up Postgres, Redis, Kafka, MailHog
5. Verify `GET /actuator/health` → 200 UP

**Frontend**
6. `cd frontend && npm create vite@latest . -- --template react-ts`
7. Install all frontend dependencies
8. Configure Tailwind + shadcn/ui init
9. Set up `vite.config.ts` dev proxy to `:8080`
10. Build `RootLayout`, `AuthLayout`, `DashboardLayout` skeletons + router

Both run simultaneously in IntelliJ — backend on `:8080`, frontend on `:5173` (dev) or `:3000` (Docker).