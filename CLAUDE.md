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

Maven wrapper available at `fitness-java/mvnw.cmd` (Windows) — no global Maven needed.

## Prerequisites

- Java 17+, Maven 3.8+, Node.js 22+
- MySQL 8.0 — run `fitness-java/docs/schema.sql` to create tables
- Redis (optional — login works without it; refresh token persistence degraded)
- MongoDB 6+ (for AI chat history)
- Environment variables: `DB_PASSWORD`, `JWT_SECRET_FITNESS`, `REDIS_PASSWORD`, `MONGODB_FITNESS_URI`, `DEEPSEEK_API_KEY`, `EMBEDDING_API_KEY`, `PINECONE_API_KEY`, `PINECONE_FITNESS_HOST`

## Architecture

**Monolith with library modules.** `fitness-web` is the sole Spring Boot entry point. It depends on `fitness-common`, `fitness-admin`, and `fitness-ai` — all three are library JARs whose controllers/services/mappers are auto-discovered by Spring component scan from `com.fitness`. Single port (8080).

```
fitness-common   — entities, enums, DTOs, Mapper interfaces, config, utils, SecurityUser
fitness-web      — startup class + user/coach controllers + auth (JwtAuthFilter, WebSecurityConfig)
fitness-admin    — admin controllers and services (no main class)
fitness-ai       — LangChain4j chat, RAG, Function Calling tools
```

**Mapper interfaces live in `fitness-common`** — lets admin and ai modules use them without circular deps. All extend `BaseMapper<T>` from MyBatis-Plus, no XML needed.

**SecurityUser lives in `fitness-common`** — needed by both web and ai modules for `@AuthenticationPrincipal`.

## Authentication Flow

1. `POST /api/auth/login` → `AuthServiceImpl` generates access token (30min, subject=userId) + refresh token (7d). Redis storage is best-effort — login succeeds even if Redis is down.
2. `JwtAuthFilter` extracts Bearer token → parses JWT → checks Redis blacklist → calls `UserDetailsServiceImpl.loadUserById(Long)` → sets `SecurityUser` as principal.
3. Controllers get current user via `@AuthenticationPrincipal SecurityUser securityUser` → `securityUser.getUser().getId()`.
4. Logout blacklists JWT jti in Redis (best-effort, ignored if Redis unavailable).
5. Refresh rotates both tokens (best-effort Redis ops).

**Role enforcement:** `WebSecurityConfig` + `@PreAuthorize`: `/api/admin/**` → ADMIN, `/api/coach/**` → COACH.

**First admin setup:** Register via user web app → `UPDATE user SET role = 'ADMIN' WHERE username = '...'` → login to admin panel. Subsequent admins can be promoted via admin UI (UserManage page role dropdown).

## AI Module (`fitness-ai`)

- **LangChain4j 1.0.0-beta1** via OpenAI-compatible mode → DeepSeek chat + embedding
- **RAG:** `EmbeddingService` → DeepSeek Embedding; `PineconeStoreService` → in-memory cosine similarity (MVP, replace with actual Pinecone client)
- **5 Function Calling tools** (`@Tool`): TrainingTool, BodyMetricsTool, DietTool, CourseTool, PlanTool — each queries MySQL via Mapper
- **SSE streaming:** `AiChatController` → `Flux<String>` with `TEXT_EVENT_STREAM_VALUE`
- **Chat history:** MongoDB `chat_history` collection via `ChatHistoryRepository`

## Key Patterns

**Entity → VO conversion** in Service layer, not Controller. Each ServiceImpl has private `toVO()`.

**DTOs:** `fitness-common`: `dto/request/`, `dto/response/`. `ApiResponse<T>` universal wrapper; `PageResult<T>` for paginated lists. `PageResult.of(total, page, size, records)`.

**Enums:** Implement `IEnum<String>`, stored as enum name. Must set `mybatis-plus.type-enums-package: com.fitness.enums` in application.yml.

**Pagination:** Controllers accept `page` (default 1), `size` (default 10). Service uses MyBatis-Plus `Page<T>`.

**The `user` table** is shared across roles — `role` column: USER/COACH/ADMIN.

## Dependency Versions & API Gotchas

- **Lombok** version managed by Spring Boot 3.5.0 (1.18.38). Parent POM must configure `annotationProcessorPaths` for `maven-compiler-plugin` or Lombok won't be found at compile time.
- **JJWT 0.12.x** API: `Jwts.parserBuilder()` → `Jwts.parser()`, `setSigningKey()` → `verifyWith()`, `parseClaimsJws()` → `parseSignedClaims()`, `getBody()` → `getPayload()`, `setSubject()` → `subject()`, `setId()` → `id()`, `setIssuedAt()` → `issuedAt()`, `setExpiration()` → `expiration()`.
- **MyBatis-Plus 3.5.9**: `PaginationInnerInterceptor` was removed. Pagination works via `MybatisPlusInterceptor` alone (auto-detects DB type). Must add `mybatis-plus-extension` as explicit dependency in common POM.
- **CORS**: `allowCredentials(false)` when using wildcard `*` origin patterns. JWT goes via Authorization header, not cookies, so credentials aren't needed.

## Frontend Gotchas

- **Element Plus must be globally registered** in `main.js`: `app.use(ElementPlus)` + `import 'element-plus/dist/index.css'`.
- **Pinia stores cannot be called in axios interceptors** (outside setup context). Read/write `localStorage` directly in `utils/request.js` instead.
- **Circular dependency:** `stores/user.js` must import `request` directly from `@/utils/request`, NOT from `@/api/auth` (which imports request, creating a loop).
- Admin panel `utils/request.js` uses separate localStorage keys (`admin_token`, `admin_user`).
- Login/Register pages share `/api/auth` with the user web app; admin login calls the same endpoint but validates `role === 'ADMIN'` client-side.
