# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Backend (single app, port 8080) — use IntelliJ bundled Maven if JAVA_HOME is misconfigured
cd fitness-java
mvn compile                              # or: ./mvnw.cmd compile
mvn spring-boot:run -pl fitness-web      # start server

# Frontend — user web client (port 5173)
cd fitness-front/fitness-front-web
npm install && npm run dev

# Frontend — admin panel (port 5174)
cd fitness-front/fitness-front-admin
npm install && npm run dev
```

Maven wrapper at `fitness-java/mvnw.cmd` (Windows). If JAVA_HOME points to wrong JDK, use IntelliJ bundled Maven: `"G:/Java/IntelliJ IDEA 2026.1/plugins/maven/lib/maven3/bin/mvn"`.

## Prerequisites

- Java 17+, Maven 3.8+, Node.js 22+
- MySQL 8.0 — run `fitness-java/docs/schema.sql` to create tables
- Redis (optional — login works without it; refresh token persistence degraded)
- MongoDB 6+ (for AI chat history)
- Env vars: `DB_PASSWORD`, `JWT_SECRET_FITNESS`, `REDIS_PASSWORD`, `MONGODB_FITNESS_URI`, `DEEPSEEK_API_KEY`, `EMBEDDING_API_KEY`, `PINECONE_API_KEY`, `PINECONE_FITNESS_HOST`, `USDA_API_KEY` (optional — food search works with local DB only)

## Architecture

**Monolith with library modules.** `fitness-web` is the sole Spring Boot entry point. It depends on `fitness-common`, `fitness-admin`, and `fitness-ai` — all three are library JARs auto-discovered by Spring component scan from `com.fitness`. Single port (8080).

```
fitness-common   — 61 files: entities (19), enums (6), DTOs, Mapper interfaces (18),
                    config, utils, exception/ErrorCode, SecurityUser
fitness-web      — 32 files: FitnessWebApplication + user/coach controllers (9) + auth
                    (JwtAuthFilter, WebSecurityConfig) + UsdaClient
fitness-admin    — 39 files: admin controllers (13) + services (no main class)
fitness-ai       — 13 files: LangChain4j chat, RAG, 5 Function Calling tools
```

**Mapper interfaces in `fitness-common`** — all extend `BaseMapper<T>` from MyBatis-Plus, no XML needed.

**SecurityUser in `fitness-common`** — needed by both web and ai modules for `@AuthenticationPrincipal`.

## Database (MySQL 8.0, schema at `fitness-java/docs/schema.sql`)

18 tables. Key ones:

| Table | Purpose | Notes |
|-------|---------|-------|
| `user` | Shared across USER/COACH/ADMIN roles | Coach fields: bio, certifications, specialties, experience, hourly_rate |
| `course` | Course catalog | coach_id FK, status: DRAFT/PENDING/APPROVED/REJECTED, rating/ratingCount |
| `course_exercise` | Per-course video lessons | title, video_url (local path or URL), description, sort_order |
| `user_course` | Course subscriptions | progress 0-100, unique(user_id, course_id) |
| `training_record` | Training check-ins | duration, calories, notes per day |
| `diet_record` | Meal logging | meal_type (String, supports custom), food_id FK→food_item, weight_grams, macros |
| `body_metrics` | Body measurements | weight, body_fat, bmi, waist, hip per date |
| `food_item` | Food nutrition DB | ~200 preset Chinese foods + USDA cache, per-100g values |
| `user_meal_type` | Custom meal types per user | name, code, sort_order, is_active (enable/disable per diet phase) |
| `training_plan` | Coach-assigned plans | JSON content, date range |
| `operation_log` | Audit trail | user, action, target, detail, IP |
| `member_card` | Membership card products | duration_days, price, status (soft-delete via status=0) |
| `group_class` | Group class scheduling | capacity, enrollment, location, CANCELLED filtered by default |
| `coach_student` | 1v1 coach-student bindings | ACTIVE/ENDED status |
| `coach_shift` | Weekly coach shifts | day_of_week (1-7), start/end time |
| `sys_role` | RBAC roles | Seed: ADMIN, COACH, USER |
| `sys_permission` | RBAC permissions | 10 seeds grouped by module (user/course/coach/settings) |
| `sys_role_permission` | Role-permission mapping | Unique(role_id, permission_id) |

**Seed data files:** `docs/seed-courses.sql` (10 courses × 6 exercises from free-exercise-db), `docs/seed-foods.sql` (~200 Chinese common foods with per-100g nutrition).

**Existing DB upgrade:** If extending an older schema, also run:
```sql
ALTER TABLE `diet_record`
  ADD COLUMN `food_id`      BIGINT,
  ADD COLUMN `weight_grams` DECIMAL(7,1);
ALTER TABLE `diet_record` MODIFY COLUMN `meal_type` VARCHAR(50) NOT NULL;
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

3 finance routes use `Placeholder.vue`. All other 14 routes are fully implemented.

## User API Endpoints (under `/api`, require auth)

| Endpoint | Controller | Methods |
|----------|-----------|---------|
| `/auth/**` | AuthController | POST login/register/refresh/logout |
| `/courses` | CourseController | GET list, GET/:id, POST/:id/subscribe, GET/my |
| `/training/records` | TrainingController | POST, GET (date range + pagination) |
| `/diet/records` | DietController | POST, GET (by date), PUT/:id, DELETE/:id |
| `/diet/foods` | FoodController | GET/search?keyword=, GET/:id, POST/custom |
| `/diet/meal-types` | UserMealTypeController | GET, POST, PUT/:id, DELETE/:id |
| `/metrics` | BodyMetricsController | POST, GET (date range), GET/latest |
| `/user/profile` | UserController | GET, PUT |
| `/coach/**` | CoachController | COACH role required |
| `/ai/chat` | AiChatController | GET (SSE streaming) |

### Diet Module Architecture

**Smart + manual dual-mode entry.** Users can search foods from a local 200-item database (supplemented by USDA FoodData Central API if `USDA_API_KEY` is set), select a food → enter weight in grams → backend auto-calculates calories and macros (`food_item.per_100g / 100 * weight_grams`). Quick manual entry still available as fallback.

**Custom meal types.** `DietRecord.mealType` is a plain String (not enum), allowing per-user custom meal types via `user_meal_type` table. Default types: BREAKFAST/LUNCH/DINNER/SNACK. Users can add/disable/delete custom types (e.g., bulk phase adds extra meals, cut phase disables them).

**USDA client.** `UsdaClient` is a thin RestTemplate wrapper. API key optional — when absent, only local DB is searched. USDA results are auto-cached to `food_item` table.

## Admin API Endpoints (all under `/api/admin`, require ADMIN role)

| Endpoint | Controller | Methods |
|----------|-----------|---------|
| `/dashboard` | DashboardController | GET |
| `/users` | AdminUserController | GET (list+keyword+role+status), POST, PUT (role/status) |
| `/coaches` | AdminCoachController | GET (list+keyword), PUT (profile fields) |
| `/courses` | AdminCourseController | GET (list+status filter), PUT (approve/reject) |
| `/group-classes` | AdminGroupClassController | GET (list, excludes CANCELLED), POST, PUT, PUT/cancel |
| `/bookings` | AdminBookingController | GET (list) |
| `/coach-students` | AdminCoachStudentController | GET (list+filters), POST, PUT/end |
| `/shifts` | AdminCoachShiftController | GET (?coachId), POST (batch save) |
| `/cards` | AdminMemberCardController | GET (status=1), POST, PUT, DELETE (soft: status=0) |
| `/metrics` | AdminMetricsController | GET (list+userId filter) |
| `/logs` | AdminLogController | GET (list+keyword search) |
| `/roles` | AdminRoleController | GET list, GET permissions, GET/:id/permissions, PUT/:id/permissions |
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
- **RAG:** `EmbeddingService` + `PineconeStoreService`
- **5 `@Tool` functions:** TrainingTool, BodyMetricsTool, DietTool, CourseTool, PlanTool — each queries MySQL via Mapper
- **SSE streaming:** `GET /api/ai/chat?message=&sessionId=` → `Flux<String>` with `TEXT_EVENT_STREAM_VALUE`
- **Chat history:** MongoDB `chat_history` collection (userId, sessionId, messages[])

## Key Patterns

**Entity → VO** in Service layer (`private toVO()`), not Controller.

**DTOs:** `fitness-common`: `dto/request/`, `dto/response/`. `ApiResponse<T>` universal wrapper (`code, message, data`); `PageResult<T>` for pagination (`total, page, size, records`).

**Enums:** Implement `IEnum<String>` with `@EnumValue`. Config: `mybatis-plus.type-enums-package: com.fitness.enums`. Exception: `DietRecord.mealType` uses plain String for custom meal type support.

**Pagination:** Controllers accept `page` (default 1), `size` (default 10). Services use MyBatis-Plus `Page<T>`. Response via `PageResult.of(total, page, size, records)`.

**Soft-delete pattern:** MemberCard and GroupClass use status-based deletion (set status=0/CANCELLED). List queries filter these out by default.

**Coach-shift batch save:** Delete all existing shifts for a coach, then insert new ones (transactional).

**Ownership checks:** Diet and other user-data endpoints verify `userId` matches the authenticated user before update/delete, throwing `BusinessException` with appropriate ErrorCode.

**Auto-calculation:** `DietServiceImpl.autoCalculate()` — when `foodId` + `weightGrams` is provided, macros are computed: `food_item.per_100g / 100 * weight_grams`. When `foodId` is null, manual values are used as-is.

## Frontend Conventions

**Both apps (web + admin):** Vue 3 + Vite + Pinia + Vue Router 5 + Element Plus + Axios. `@/` alias maps to `src/`.

**Web app directory layout:**
```
src/
  api/        — per-module API wrappers (auth.js, course.js, training.js, diet.js, metrics.js, ai.js)
  stores/     — Pinia stores (user.js for auth state, localStorage-persisted)
  utils/      — request.js (Axios instance with token injection + refresh interceptor)
  router/     — Vue Router with auth/guest/role guards
  layouts/    — UserLayout.vue (top nav + <router-view>)
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
- **DietRecord.mealType** is String, not MealType enum — changed to support custom meal types. Validation is `@NotBlank` instead of `@NotNull`.
- **Spring placeholders** with env var defaults must use `${VAR:}` syntax (colon provides empty default). Without it Spring throws `PlaceholderResolutionException` on startup.
