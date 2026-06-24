# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Backend (single app on port 8080)
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

## Prerequisites

- Java 17+, Maven 3.8+
- MySQL 8.0 (database `fitness`, schema at `fitness-java/docs/schema.sql`)
- Redis, MongoDB 6+, Node.js 22+
- Environment variables (with defaults in `application.yml`): `DB_PASSWORD`, `JWT_SECRET`, `DEEPSEEK_API_KEY`, `PINECONE_API_KEY`, `PINECONE_HOST`, `MONGODB_URI`

## Architecture

**Monolith with library modules.** `fitness-web` is the sole Spring Boot entry point. It depends on `fitness-common`, `fitness-admin`, and `fitness-ai` — all three are library JARs whose controllers/services/mappers are auto-discovered by Spring component scan from `com.fitness`. There's only one port (8080) and one application.

```
fitness-common   — entities, enums, DTOs, Mapper interfaces, config, utils
fitness-web      — startup class + user/coach controllers + auth
fitness-admin    — admin controllers and services (no main class)
fitness-ai       — LangChain4j chat, RAG, Function Calling tools
```

**Mapper interfaces live in `fitness-common`** (not fitness-web). This lets admin and ai modules use them without circular dependencies. They extend `BaseMapper<T>` from MyBatis-Plus — no XML needed.

## Authentication Flow

1. `POST /api/auth/login` → `AuthServiceImpl` generates access token (30min, subject=userId) and refresh token (7d, stored in Redis as `refresh:<userId>:<jti>`)
2. `JwtAuthFilter` extracts Bearer token → parses JWT → checks Redis blacklist → calls `UserDetailsServiceImpl.loadUserById(Long)` → sets `SecurityUser` as principal
3. Controllers get current user via `@AuthenticationPrincipal SecurityUser securityUser` and call `securityUser.getUser().getId()`
4. Logout adds JWT jti to Redis blacklist with TTL matching remaining validity
5. Refresh rotates both tokens — old refresh token is deleted from Redis

**Role enforcement:** `WebSecurityConfig` restricts `/api/admin/**` to ADMIN and `/api/coach/**` to COACH. Individual controllers use `@PreAuthorize`.

## AI Module (`fitness-ai`)

- **LangChain4j 1.0.0-beta1** with OpenAI-compatible mode connecting to DeepSeek
- `LLMConfig` provides `ChatLanguageModel` and `EmbeddingModel` beans
- **RAG:** `EmbeddingService` vectorizes text via DeepSeek Embedding; `PineconeStoreService` does cosine-similarity search against an in-memory store (MVP — replace with actual Pinecone client)
- **5 Function Calling tools** (`@Tool`): `TrainingTool`, `BodyMetricsTool`, `DietTool`, `CourseTool`, `PlanTool` — each queries MySQL via Mapper and returns structured results
- **SSE streaming:** `AiChatController` returns `Flux<String>` with `MediaType.TEXT_EVENT_STREAM_VALUE`; `AiChatService` uses `AiServices.builder()` + `TokenStream`
- **Chat history:** persisted in MongoDB (`chat_history` collection) via `ChatHistoryRepository`

## Key Patterns

**Entity → VO conversion** happens in Service layer, not Controller. Each ServiceImpl has a private `toVO()` method.

**DTOs** in `fitness-common`: `request/` for incoming, `response/` for outgoing. `ApiResponse<T>` is the universal wrapper; `PageResult<T>` for paginated lists.

**Enum mapping:** All enums implement `IEnum<String>` from MyBatis-Plus for automatic DB mapping. They store the enum name (e.g., `USER`) and have a `desc` field for display (e.g., "普通用户").

**Pagination:** Controllers accept `page` (default 1) and `size` (default 10). Service layer uses MyBatis-Plus `Page<T>`. Response wraps with `PageResult.of(total, page, size, records)`.

**The `user` table** is shared across roles — `role` column distinguishes USER/COACH/ADMIN.
