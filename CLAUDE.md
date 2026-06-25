# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Backend (single app, port 8080)
cd fitness-java
mvn compile                              # compile all modules
mvn spring-boot:run -pl fitness-web      # start server

# Frontend — user web client (port 5173)
cd fitness-front/fitness-front-web
npm install && npm run dev

# Frontend — admin panel (port 5174)
cd fitness-front/fitness-front-admin
npm install && npm run dev
```

Maven wrapper at `fitness-java/mvnw.cmd` (Windows) — no global Maven needed.

## Prerequisites

- Java 17+, Maven 3.8+, Node.js 22+
- MySQL 8.0 — run `fitness-java/docs/schema.sql` to create tables
- Redis (optional — login works without it; refresh token persistence degraded)
- MongoDB 6+ (for AI chat history)
- Env vars: `DB_PASSWORD`, `JWT_SECRET_FITNESS`, `REDIS_PASSWORD`, `MONGODB_FITNESS_URI`, `DEEPSEEK_API_KEY`, `EMBEDDING_API_KEY`, `PINECONE_API_KEY`, `PINECONE_FITNESS_HOST`

## Architecture

**Monolith with library modules.** `fitness-web` is the sole Spring Boot entry point. It depends on `fitness-common`, `fitness-admin`, and `fitness-ai` — all three are library JARs auto-discovered by Spring component scan from `com.fitness`. Single port (8080).

```
fitness-common   — 55 Java files: entities (16), enums (6), DTOs, Mapper interfaces (15),
                    config, utils, exception/ErrorCode, SecurityUser
fitness-web      — 25 Java files: FitnessWebApplication + user/coach controllers + auth
                    (JwtAuthFilter, WebSecurityConfig, AuthServiceImpl)
fitness-admin    — 39 Java files: admin controllers (13) + services (no main class)
fitness-ai       — 13 Java files: LangChain4j chat, RAG, 5 Function Calling tools
```

**Mapper interfaces in `fitness-common`** — all extend `BaseMapper<T>` from MyBatis-Plus, no XML needed.

**SecurityUser in `fitness-common`** — needed by both web and ai modules for `@AuthenticationPrincipal`.

## Database (MySQL 8.0, schema at `fitness-java/docs/schema.sql`)

16 tables. Key ones:

| Table | Purpose | Notes |
|-------|---------|-------|
| `user` | Shared across USER/COACH/ADMIN roles | Coach fields: bio, certifications, specialties, experience, hourly_rate |
| `course` | Course catalog | coach_id FK, status enum (DRAFT/PENDING/APPROVED/REJECTED) |
| `user_course` | Course subscriptions | progress 0-100, unique(user_id, course_id) |
| `training_record` | Training check-ins | duration, calories, notes per day |
| `diet_record` | Meal logging | macro tracking (protein/carbs/fat) |
| `body_metrics` | Body measurements | weight, body_fat, bmi, waist, hip per date |
| `training_plan` | Coach-assigned plans | JSON content, date range |
| `operation_log` | Audit trail | user, action, target, detail, IP |
| `member_card` | Membership card products | duration_days, price, status (soft-delete via status=0) |
| `group_class` | Group class scheduling | capacity, enrollment, location, CANCELLED filtered by default |
| `coach_student` | 1v1 coach-student bindings | ACTIVE/ENDED status |
| `coach_shift` | Weekly coach shifts | day_of_week (1-7), start/end time |
| `sys_role` | RBAC roles | Seed: ADMIN, COACH, USER |
| `sys_permission` | RBAC permissions | 10 seeds grouped by module (user/course/coach/settings) |
| `sys_role_permission` | Role-permission mapping | Unique(role_id, permission_id) |

**Existing DB upgrade note:** `user` table was extended with 5 coach columns. If DB already exists, run:
```sql
ALTER TABLE `user`
  ADD COLUMN `bio` VARCHAR(500),
  ADD COLUMN `certifications` VARCHAR(500),
  ADD COLUMN `specialties` VARCHAR(300),
  ADD COLUMN `experience` INT,
  ADD COLUMN `hourly_rate` DECIMAL(10,2);
```

## Admin Panel Menu Structure

6 menu groups with 17 routes under `/` (nested in AdminLayout):

```
数据概览    /dashboard
会员管理    /members/list, /members/cards, /members/metrics
课程管理    /courses/group, /courses/pt, /courses/bookings, /courses/review
教练管理    /coaches/profile, /coaches/schedule, /coaches/performance
财务管理    /finance/transactions, /finance/bills, /finance/refunds  ← all Placeholder
系统设置    /settings/staff, /settings/roles, /settings/logs
```

3 finance routes use `Placeholder.vue` (no payment system yet). All other 14 routes are fully implemented with backend APIs.

## Admin API Endpoints (all under `/api/admin`, require ADMIN role)

| Endpoint | Controller | Methods |
|----------|-----------|---------|
| `/dashboard` | DashboardController | GET |
| `/users` | AdminUserController | GET (list+keyword+role+status filter), POST (create), PUT (role/status) |
| `/coaches` | AdminCoachController | GET (list+keyword), PUT (profile fields) |
| `/courses` | AdminCourseController | GET (list+status filter), PUT (approve/reject) |
| `/group-classes` | AdminGroupClassController | GET (list+status, excludes CANCELLED by default), POST, PUT, PUT/cancel |
| `/bookings` | AdminBookingController | GET (list) |
| `/coach-students` | AdminCoachStudentController | GET (list+coachId+studentId+status), POST, PUT/end |
| `/shifts` | AdminCoachShiftController | GET (?coachId), POST (batch save, deletes old then inserts) |
| `/cards` | AdminMemberCardController | GET (list, status=1 only), POST, PUT, DELETE (soft: status=0) |
| `/metrics` | AdminMetricsController | GET (list+userId filter) |
| `/logs` | AdminLogController | GET (list+keyword search) |
| `/roles` | AdminRoleController | GET (list), GET/permissions, GET/{id}/permissions, PUT/{id}/permissions |
| `/coach-performance` | CoachPerformanceController | GET (aggregated stats per coach) |

**Role filtering:** `/users` supports comma-separated roles, e.g. `?role=ADMIN,COACH`.

## Authentication Flow

1. `POST /api/auth/login` → access token (30min, sub=userId, claim=role, jti=UUID) + refresh token (7d)
2. `JwtAuthFilter` extracts Bearer token → parses JWT → checks Redis blacklist → `UserDetailsServiceImpl.loadUserById(Long)` → sets `SecurityUser` principal
3. Controllers: `@AuthenticationPrincipal SecurityUser securityUser` → `securityUser.getUser().getId()`
4. Logout blacklists JWT jti in Redis (best-effort)
5. Refresh rotates both tokens (best-effort Redis ops)

**Role enforcement:** `WebSecurityConfig` + `@PreAuthorize`: `/api/admin/**` → ADMIN, `/api/coach/**` → COACH. Admin controllers also have `@PreAuthorize("hasRole('ADMIN')")` at class level.

**First admin:** Register via user web app → SQL promote → admin panel. Or use StaffManage page to create admin accounts directly.

## AI Module (`fitness-ai`)

- **LangChain4j 1.0.0-beta1** OpenAI-compatible mode → DeepSeek (`deepseek-chat`) for chat, DashScope (`text-embedding-v3`) for embeddings
- **RAG:** `EmbeddingService` + `PineconeStoreService` (MVP: in-memory cosine similarity)
- **5 `@Tool` functions:** TrainingTool, BodyMetricsTool, DietTool, CourseTool, PlanTool — each queries MySQL via Mapper
- **SSE streaming:** `GET /api/ai/chat?message=&sessionId=` → `Flux<String>` with `TEXT_EVENT_STREAM_VALUE`
- **Chat history:** MongoDB `chat_history` collection (userId, sessionId, messages[])

## Key Patterns

**Entity → VO** in Service layer (`private toVO()`), not Controller.

**DTOs:** `fitness-common`: `dto/request/`, `dto/response/`. `ApiResponse<T>` universal wrapper (`code, message, data`); `PageResult<T>` for pagination (`total, page, size, records`).

**Enums:** Implement `IEnum<String>` with `@EnumValue`. Config: `mybatis-plus.type-enums-package: com.fitness.enums`.

**Pagination:** Controllers accept `page` (default 1), `size` (default 10). Services use MyBatis-Plus `Page<T>`. Response via `PageResult.of(total, page, size, records)`.

**Soft-delete pattern:** MemberCard and GroupClass use status-based deletion (set status=0/CANCELLED). List queries filter these out by default.

**Coach-shift batch save:** Delete all existing shifts for a coach, then insert new ones (transactional).

## Frontend Conventions

**Both apps (web + admin):** Vue 3 + Vite + Pinia + Vue Router 5 + Element Plus + Axios. `@/` alias maps to `src/`.

**Web app directory layout:**
```
src/
  api/        — per-module API wrappers (auth.js, course.js, training.js, etc.)
  stores/     — Pinia stores (user.js for auth state, localStorage-persisted)
  utils/      — request.js (Axios instance with token injection + refresh interceptor)
  router/     — Vue Router with auth/guest/role guards
  views/      — 12 .vue pages
```

**Admin app directory layout:**
```
src/
  components/ — Placeholder.vue (reusable empty-state)
  layouts/    — AdminLayout.vue (sidebar + header + breadcrumb + <router-view>)
  stores/     — admin.js (separate localStorage keys: admin_token, admin_user)
  utils/      — request.js (two Axios instances: `api` for /api/auth, `request` for /api/admin)
  router/     — nested routes under AdminLayout
  views/      — 15 .vue pages
```

**Admin axios instances:** `utils/request.js` exports two:
- `api` — base: `VITE_API_BASE_URL` — used for auth (`/api/auth/login`)
- `request` (default) — base: `VITE_API_BASE_URL/api/admin` — used for all admin APIs

## Dependency Versions & Gotchas

- **Spring Boot 3.5.0**, **MyBatis-Plus 3.5.9**, **JJWT 0.12.6**, **LangChain4j 1.0.0-beta1**
- **Lombok** managed by Spring Boot parent (1.18.38). Parent POM must configure `annotationProcessorPaths` for `maven-compiler-plugin`.
- **JJWT 0.12.x**: `Jwts.parser()` → `verifyWith()` → `.build().parseSignedClaims(token).getPayload()`. `subject()`/`id()`/`issuedAt()`/`expiration()` (not `set*`).
- **MyBatis-Plus 3.5.9**: `PaginationInnerInterceptor` removed. Pagination via `MybatisPlusInterceptor` alone (auto-detects DB). Must add `mybatis-plus-extension` dependency.
- **CORS**: `allowCredentials(false)` when using wildcard `*` origin patterns (JWT goes via Authorization header, not cookies).
- **Element Plus** must be globally registered: `app.use(ElementPlus)` + `import 'element-plus/dist/index.css'`.
- **Pinia stores** cannot be called in axios interceptors (outside setup context). Read/write `localStorage` directly in `utils/request.js`.
- **Circular dependency:** `stores/user.js` must import `request` from `@/utils/request`, NOT from `@/api/auth`.
- **Separate auth keys:** Admin uses `admin_token`/`admin_user`, web uses `accessToken`/`refreshToken`/`user` in localStorage.
