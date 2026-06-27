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
- Env vars: `DB_PASSWORD`, `JWT_SECRET_FITNESS`, `REDIS_PASSWORD`, `USDA_API_KEY` (optional — food search works with local DB only)

## Architecture

**Monolith with library modules.** `fitness-web` is the sole Spring Boot entry point. It depends on `fitness-common` and `fitness-admin` — both are library JARs auto-discovered by Spring component scan from `com.fitness`. Single port (8080).

```
fitness-common   — entities (20), enums (6), DTOs, Mapper interfaces (19),
                    config, utils, exception/ErrorCode, SecurityUser
fitness-web      — FitnessWebApplication + user controllers (10) + auth
                    (JwtAuthFilter, WebSecurityConfig) + UsdaClient + UploadController
fitness-admin    — admin controllers (13) + services (no main class)
```

**Mapper interfaces in `fitness-common`** — all extend `BaseMapper<T>` from MyBatis-Plus, no XML needed.

**SecurityUser in `fitness-common`** — needed by both web and admin modules for `@AuthenticationPrincipal`.

## Database (MySQL 8.0, schema at `fitness-java/docs/schema.sql`)

19 tables. Key ones:

| Table | Purpose | Notes |
|-------|---------|-------|
| `user` | Shared across USER/COACH/ADMIN roles | Coach fields: bio, certifications, specialties, experience, hourly_rate, height |
| `course` | Course catalog | coach_id FK, status: DRAFT/PENDING/APPROVED/REJECTED, rating/ratingCount |
| `course_exercise` | Per-course video lessons | title, video_url, description, sort_order — 66 exercises seeded |
| `user_course` | Course subscriptions | progress 0-100, unique(user_id, course_id) |
| `training_record` | Training check-ins | training_type, intensity, duration, calories, notes |
| `diet_record` | Meal logging | meal_type (String, supports custom), food_id FK→food_item, weight_grams, auto-calculated macros |
| `body_metrics` | Body measurements | weight, body_fat, bmi (auto-calculated from height), waist, hip |
| `food_item` | Food nutrition DB | ~200 preset Chinese foods + USDA cache, per-100g values |
| `user_meal_type` | Custom meal types | name, code, sort_order, is_active |
| `workout_plan` | User training templates | name, training_type, exercises (JSON), user_id |
| `training_plan` | Coach-assigned plans | JSON content, date range |
| `operation_log` | Audit trail | user, action, target, detail, IP |
| `member_card` | Membership card products | duration_days, price, status (soft-delete via status=0) |
| `group_class` | Group class scheduling | capacity, enrollment, location, CANCELLED filtered |
| `coach_student` | 1v1 coach-student bindings | ACTIVE/ENDED status |
| `coach_shift` | Weekly coach shifts | day_of_week (1-7), start/end time |
| `sys_role` | RBAC roles | Seed: ADMIN, COACH, USER |
| `sys_permission` | RBAC permissions | 10 seeds grouped by module |
| `sys_role_permission` | Role-permission mapping | Unique(role_id, permission_id) |

**Seed data files:** `docs/seed-courses.sql` (11 courses × 6 exercises from free-exercise-db), `docs/seed-foods.sql` (~200 Chinese common foods with per-100g nutrition).

**Existing DB upgrade:**
```sql
ALTER TABLE `user` ADD COLUMN `height` DECIMAL(5,1);
ALTER TABLE `training_record` ADD COLUMN `training_type` VARCHAR(50), ADD COLUMN `intensity` VARCHAR(20);
ALTER TABLE `diet_record` ADD COLUMN `food_id` BIGINT, ADD COLUMN `weight_grams` DECIMAL(7,1);
ALTER TABLE `diet_record` MODIFY COLUMN `meal_type` VARCHAR(50) NOT NULL;
```

## User API Endpoints (under `/api`, require auth)

| Endpoint | Controller | Methods |
|----------|-----------|---------|
| `/auth/**` | AuthController | POST login/register/refresh/logout |
| `/courses` | CourseController | GET list, GET/:id, POST/:id/subscribe, GET/my |
| `/training/records` | TrainingController | POST, GET (pagination+date), PUT/:id, DELETE/:id |
| `/training/records/stats` | TrainingController | GET (totalDays, streak, typeDistribution, dailyMap) |
| `/training/plans` | WorkoutPlanController | GET, POST, PUT/:id, DELETE/:id |
| `/training/exercises` | TrainingController | GET?keyword= (multi-word OR search course_exercise) |
| `/diet/records` | DietController | POST, GET (by date), PUT/:id, DELETE/:id |
| `/diet/foods` | FoodController | GET/search?keyword=, GET/:id, POST/custom |
| `/diet/meal-types` | UserMealTypeController | GET, POST, PUT/:id, DELETE/:id |
| `/metrics` | BodyMetricsController | POST, GET (date range), GET/latest, PUT/:id, DELETE/:id |
| `/user/profile` | UserController | GET, PUT (nickname/avatarUrl/phone/height + coach fields) |
| `/user/password` | UserController | PUT (oldPassword + newPassword) |
| `/upload/avatar` | UploadController | POST multipart (5MB max, image only) |
| `/coach/**` | CoachController | COACH role required |

### Diet Module Architecture

**Smart + manual dual-mode entry.** Search foods from local 200-item DB (supplemented by USDA FoodData Central API if `USDA_API_KEY` is set) → select a food → enter weight in grams → backend auto-calculates macros (`food_item.per_100g / 100 * weight_grams`). Quick manual entry available as fallback.

**Custom meal types.** `DietRecord.mealType` is a plain String (not enum), allowing per-user custom meal types via `user_meal_type` table. Default: BREAKFAST/LUNCH/DINNER/SNACK.

**USDA client.** `UsdaClient` is a thin RestTemplate wrapper. API key optional — when absent, only local DB is searched. Results auto-cached to `food_item`.

### Training Module Architecture

**Training types & intensity.** `training_record` has `training_type` (11 presets: 胸部/背部/腿部/肩部/手臂/核心/有氧/瑜伽/高强度间歇/全身/其他) and `intensity` (EASY/MEDIUM/HARD).

**Workout session flow.** Training.vue → "开始今日训练" → setup dialog (auto-filter exercises by training type from course_exercise, per-exercise sets/rest settings) → WorkoutSession.vue (guided set-by-set, rest timer with SVG ring, auto-advance) → completion summary → auto-saves TrainingRecord.

**Stats.** `GET /records/stats` returns totalDays, currentStreak, typeDistribution, dailyRecords (for heatmap).

**Workout plans.** Users can save exercise combinations as reusable templates (`workout_plan` table).

### BodyMetrics Module

**BMI auto-calculation.** When `user.height` is set, BMI = weight / (height/100)² is computed automatically on add/update. BMI category labels: 偏瘦/标准/偏重/肥胖.

## Admin API Endpoints (all under `/api/admin`, require ADMIN role)

| Endpoint | Controller | Methods |
|----------|-----------|---------|
| `/dashboard` | DashboardController | GET |
| `/users` | AdminUserController | GET (keyword+role+status), POST, PUT |
| `/coaches` | AdminCoachController | GET (list+keyword), PUT (profile) |
| `/courses` | AdminCourseController | GET (list+status), PUT (approve/reject) |
| `/group-classes` | AdminGroupClassController | GET, POST, PUT, PUT/cancel |
| `/bookings` | AdminBookingController | GET |
| `/coach-students` | AdminCoachStudentController | GET (filters), POST, PUT/end |
| `/shifts` | AdminCoachShiftController | GET (?coachId), POST (batch save) |
| `/cards` | AdminMemberCardController | GET (status=1), POST, PUT, DELETE (soft) |
| `/metrics` | AdminMetricsController | GET (list+userId) |
| `/logs` | AdminLogController | GET (list+keyword) |
| `/roles` | AdminRoleController | GET, GET permissions, PUT permissions |
| `/coach-performance` | CoachPerformanceController | GET (aggregated stats) |

**Role filtering:** `/users` supports comma-separated roles, e.g. `?role=ADMIN,COACH`.

## Authentication Flow

1. `POST /api/auth/login` → access token (30min, sub=userId, claim=role, jti=UUID) + refresh token (7d)
2. `JwtAuthFilter` extracts Bearer token → parses JWT → checks Redis blacklist → `UserDetailsServiceImpl.loadUserById(Long)` → sets `SecurityUser` principal
3. Controllers: `@AuthenticationPrincipal SecurityUser securityUser` → `securityUser.getUser().getId()`
4. Logout blacklists JWT jti in Redis (best-effort)
5. Refresh rotates both tokens (best-effort Redis ops)

**Role enforcement:** `WebSecurityConfig` + `@PreAuthorize`: `/api/admin/**` → ADMIN, `/api/coach/**` → COACH. Static files `/uploads/**` are permitted to all.

**First admin:** Register via user web app → SQL promote → admin panel. Or use StaffManage page.

## Key Patterns

**Entity → VO** in Service layer (`private toVO()`), not Controller.

**DTOs:** `fitness-common`: `dto/request/`, `dto/response/`. `ApiResponse<T>` universal wrapper (`code, message, data`); `PageResult<T>` for pagination (`total, page, size, records`).

**Enums:** Implement `IEnum<String>` with `@EnumValue`. Config: `mybatis-plus.type-enums-package: com.fitness.enums`. Exception: `DietRecord.mealType` uses plain String.

**Pagination:** Controllers accept `page` (default 1), `size` (default 10). Services use MyBatis-Plus `Page<T>`. Response via `PageResult.of()`.

**Soft-delete:** MemberCard and GroupClass use status-based deletion (set status=0/CANCELLED). List queries filter these out.

**Ownership checks:** All user-data PUT/DELETE endpoints verify `userId` matches the authenticated user, throwing `BusinessException` with appropriate ErrorCode (e.g., `DIET_RECORD_NOT_OWNER`, `TRAINING_RECORD_NOT_OWNER`, `METRICS_NOT_OWNER`).

**Auto-calculation patterns:**
- Diet: macros from `food_item.per_100g / 100 * weight_grams`
- BodyMetrics: BMI from `weight / (height/100)²`

**File upload:** `POST /api/upload/avatar` accepts multipart image files (5MB max), stored to `{user.dir}/uploads/avatars/`, served via `WebConfig` at `/uploads/**`. Security permits `/uploads/**` without auth.

## Frontend Conventions

**Both apps:** Vue 3 + Vite + Pinia + Vue Router 5 + Element Plus + Axios. `@/` alias maps to `src/`.

**Vite proxy:** `/api` and `/uploads` proxied to `http://localhost:8080` in dev mode.

**Global design system** at `src/styles/`:
- `variables.css` — CSS custom properties (brand red #D93831, spacing 8px grid, typography, shadows, transitions) + Element Plus theme override
- `reset.css` — base reset
- `transitions.css` — page fade, dialog scale, skeleton loading, count-up animation
- `global.css` — `.page-title` (red `//` slash prefix), `.section-title`, `.stat-card`, `.btn-primary`

**Web app layout:**
```
src/
  api/        — per-module wrappers (auth, course, training, diet, metrics)
  components/ — MacroRing.vue, TrendSparkline.vue
  composables/— useCountUp.js
  stores/     — user.js (Pinia, localStorage-persisted)
  utils/      — request.js (Axios with Bearer token + refresh interceptor)
  router/     — Vue Router with auth/guest/role guards
  layouts/    — UserLayout.vue (black nav + red highlights + footer + mobile TabBar)
  views/      — 12 .vue pages + WorkoutSession.vue
  styles/     — Global CSS design system
```

**Admin app layout:**
```
src/
  components/ — Placeholder.vue
  layouts/    — AdminLayout.vue (sidebar + header + breadcrumb)
  stores/     — admin.js (separate keys: admin_token, admin_user)
  utils/      — request.js (two Axios instances: api for /api/auth, request for /api/admin)
  router/     — nested routes under AdminLayout
  views/      — 15 .vue pages
```

**Admin axios instances:** `utils/request.js` exports two:
- `api` — base: `VITE_API_BASE_URL` — for auth (`/api/auth/login`)
- `request` (default) — base: `VITE_API_BASE_URL/api/admin` — for all admin APIs

## Dependency Versions & Gotchas

- **Spring Boot 3.5.0**, **MyBatis-Plus 3.5.9**, **JJWT 0.12.6**
- **Lombok** managed by Spring Boot parent (1.18.38). Parent POM must configure `annotationProcessorPaths`.
- **JJWT 0.12.x**: `Jwts.parser()` → `verifyWith()` → `.build().parseSignedClaims(token).getPayload()`. `subject()`/`id()`/`issuedAt()`/`expiration()`.
- **MyBatis-Plus 3.5.9**: `PaginationInnerInterceptor` removed. Pagination via `MybatisPlusInterceptor` alone. Must add `mybatis-plus-extension`.
- **CORS**: `allowCredentials(false)` when using wildcard `*` origin (JWT via Authorization header, not cookies).
- **Element Plus** must be globally registered: `app.use(ElementPlus)` + `import 'element-plus/dist/index.css'`.
- **Pinia stores** cannot be called in axios interceptors. Read/write `localStorage` directly in `utils/request.js`.
- **Circular dependency:** `stores/user.js` imports `request` from `@/utils/request`, NOT from `@/api/auth`.
- **Separate auth keys:** Admin uses `admin_token`/`admin_user`, web uses `accessToken`/`refreshToken`/`user`.
- **DietRecord.mealType** is String, not enum — supports custom meal types. Validation is `@NotBlank`.
- **Spring placeholders** with env var defaults must use `${VAR:}` syntax.
- **el-upload** doesn't use Axios — needs Vite proxy or full URL for action. Also needs `Authorization` header set manually via `:headers`.
- **el-popconfirm** wraps content in a block element, which can break inline button layouts — wrap action buttons in a flex container (`.action-cell { display:flex }`).
- **Static file serving** needs both `WebConfig.addResourceHandlers` AND `WebSecurityConfig` permitAll for the path.
