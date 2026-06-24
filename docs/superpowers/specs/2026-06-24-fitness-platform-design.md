# 线上健身平台 — 设计规格说明书

**日期:** 2026-06-24  
**版本:** 1.0  
**状态:** 已批准

---

## 1. 概述

### 1.1 项目定位

综合健身管理平台，提供课程学习、训练跟练、饮食记录、身体数据追踪和 AI 智能教练功能。纯 Web 端运行，API 设计预留移动端扩展。

### 1.2 用户角色

| 角色 | 权限范围 |
|------|---------|
| USER | 浏览课程、跟练打卡、记录饮食/身体数据、与 AI 教练对话 |
| COACH | 继承 USER + 发布课程、管理学员、下发训练计划 |
| ADMIN | 全局管理：用户/教练审核、课程审核、数据统计、系统配置 |

---

## 2. 技术选型

| 类别 | 选型 | 说明 |
|------|------|------|
| 前端框架 | Vue 3 + Vite | 已搭建 fitness-front-web / fitness-front-admin |
| UI 组件库 | Element Plus | 已安装，统一两端 UI |
| 状态管理 | Pinia | 已安装 |
| 路由 | Vue Router 5 | 已安装 |
| HTTP 客户端 | Axios | 已安装 |
| CSS 预处理 | Sass (scss) | 已安装 sass-embedded |
| 后端框架 | Spring Boot 3.x | Maven 多模块管理 |
| ORM | MyBatis-Plus | Lambda 查询 + 代码生成 |
| 认证 | Spring Security + JWT | access_token 30min + refresh_token 7d |
| Token 管理 | Redis | 黑名单登出 + refresh_token 存储 |
| 关系数据库 | MySQL 8.0 | 核心业务数据 |
| 文档数据库 | MongoDB | AI 对话历史多轮存储 |
| 向量数据库 | Pinecone | RAG 知识库检索 |
| AI 框架 | LangChain4j | LLM 编排 + RAG + Function Calling |
| 大语言模型 | DeepSeek API | Chat 模型 + Embedding 模型，预留多模型切换 |
| 对象存储 | 阿里云 OSS | 课程视频、图片、用户头像上传 |
| 构建工具 | Maven | 父 POM 管理三个子模块 |

---

## 3. 项目结构

```
fitness/
├── fitness-front/
│   ├── fitness-front-web/      用户端 Vue 3 项目
│   └── fitness-front-admin/    管理端 Vue 3 项目
├── fitness-java/
│   ├── pom.xml                 父 POM
│   ├── fitness-common/         公共模块
│   │   └── src/main/java/com/fitness/
│   │       ├── entity/         MyBatis-Plus 实体
│   │       ├── dto/            请求/响应 DTO
│   │       ├── enums/          枚举（角色、课程状态等）
│   │       ├── exception/      全局异常 + 业务异常
│   │       ├── utils/          工具类
│   │       └── config/         共享配置（MyBatis-Plus、Redis 等）
│   ├── fitness-web/            用户端 API + 教练功能 API
│   │   └── src/main/java/com/fitness/
│   │       ├── controller/     REST 控制器
│   │       ├── service/        业务逻辑
│   │       ├── mapper/         MyBatis-Plus Mapper
│   │       └── security/       JWT Filter + Spring Security 配置
│   ├── fitness-admin/          管理端 API
│   │   └── src/main/java/com/fitness/
│   │       ├── controller/
│   │       ├── service/
│   │       └── mapper/
│   └── fitness-ai/             AI 助手模块
│       └── src/main/java/com/fitness/
│           ├── controller/     AiChatController（SSE 流式）
│           ├── service/        AiChatService（编排层）
│           ├── tool/           5 个 @Tool 定义
│           ├── rag/            Embedding + Pinecone 检索
│           ├── memory/         MongoDB 对话历史 + ChatMemory
│           └── config/         LLM / Pinecone 配置
└── docs/
    └── superpowers/
        └── specs/             设计规格文档
```

---

## 4. 核心数据模型（MySQL）

### 4.1 ER 概要

```
user ──┬── user_course ──── course
       ├── training_record
       ├── diet_record
       ├── body_metrics
       └── training_plan (coach → user)
```

### 4.2 核心表

**user** — 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| username | VARCHAR(50) | 唯一 |
| password | VARCHAR(255) | BCrypt |
| nickname | VARCHAR(50) | 昵称 |
| avatar_url | VARCHAR(500) | OSS 头像 |
| phone | VARCHAR(20) | |
| role | ENUM | USER / COACH / ADMIN |
| status | TINYINT | 0 禁用 1 正常 |
| create_time | DATETIME | |
| update_time | DATETIME | |

**course** — 课程表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| coach_id | BIGINT | FK → user.id |
| title | VARCHAR(200) | |
| description | TEXT | |
| cover_url | VARCHAR(500) | OSS |
| video_url | VARCHAR(500) | OSS |
| category | VARCHAR(50) | 增肌/减脂/瑜伽/有氧/力量 |
| difficulty | ENUM | BEGINNER / INTERMEDIATE / ADVANCED |
| duration | INT | 分钟 |
| price | DECIMAL(10,2) | 0 表示免费 |
| status | TINYINT | 0 下架 1 上架 2 审核中 |
| create_time | DATETIME | |
| update_time | DATETIME | |

**user_course** — 用户课程关联
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| user_id | BIGINT | FK |
| course_id | BIGINT | FK |
| progress | INT | 0-100 百分比 |
| purchase_time | DATETIME | |

**training_record** — 训练记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| user_id | BIGINT | FK |
| course_id | BIGINT | FK（可空，自由训练） |
| duration | INT | 分钟 |
| calories | INT | 消耗热量 |
| notes | VARCHAR(500) | 备注 |
| record_date | DATE | |

**diet_record** — 饮食记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| user_id | BIGINT | FK |
| meal_type | ENUM | BREAKFAST/LUNCH/DINNER/SNACK |
| food_name | VARCHAR(100) | |
| calories | INT | |
| protein | DECIMAL(5,1) | g |
| carbs | DECIMAL(5,1) | g |
| fat | DECIMAL(5,1) | g |
| image_url | VARCHAR(500) | 可空 |
| record_date | DATE | |
| create_time | DATETIME | |

**body_metrics** — 身体数据
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| user_id | BIGINT | FK |
| weight | DECIMAL(5,1) | kg |
| body_fat | DECIMAL(4,1) | 百分比 |
| bmi | DECIMAL(4,1) | 自动计算 |
| waist | DECIMAL(5,1) | 可空 |
| hip | DECIMAL(5,1) | 可空 |
| record_date | DATE | |

**training_plan** — 教练下发训练计划
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| coach_id | BIGINT | FK → user.id |
| user_id | BIGINT | FK → user.id |
| title | VARCHAR(200) | |
| content | TEXT | 结构化计划 JSON |
| start_date | DATE | |
| end_date | DATE | |
| status | TINYINT | 进行中/已完成/已过期 |
| create_time | DATETIME | |

---

## 5. AI 助手设计

### 5.1 架构流程

```
用户提问 → fitness-ai 模块
  ├── 1. Pinecone RAG 检索健身知识片段
  ├── 2. MySQL 查询用户训练/饮食/身体数据（via @Tool）
  ├── 3. MongoDB 读取历史对话上下文（LangChain4j ChatMemory）
  ├── 4. 组装 Prompt → DeepSeek Chat API
  └── 5. SSE 流式返回答案 → MongoDB 保存本轮对话
```

### 5.2 Function Calling Tools

| Tool | 方法签名 | 功能 |
|------|---------|------|
| TrainingTool | queryTrainingRecords(userId, days) | 查询用户近 N 天训练记录 |
| BodyMetricsTool | queryBodyMetrics(userId, days) | 查询身体数据趋势 |
| DietTool | queryDietRecords(userId, date) | 查询某日饮食记录 |
| CourseTool | queryCourses(keyword, category, difficulty) | 搜索课程库 |
| PlanTool | generateTrainingPlan(userId, goal, weeks) | 生成结构化训练计划 |

### 5.3 MongoDB 对话记录结构

```json
{
  "userId": 1001,
  "sessionId": "sess_xxx",
  "messages": [
    { "role": "USER", "content": "...", "timestamp": "..." },
    { "role": "ASSISTANT", "content": "...", "toolCalls": [...], "timestamp": "..." }
  ],
  "createdAt": "...",
  "updatedAt": "..."
}
```

### 5.4 Pinecone 知识库

- 初始化：收集健身知识文档（训练原理、营养学、解剖学、运动康复等），分段 Embedding 存入 Pinecone
- 检索：用户提问向量化 → Pinecone 相似度搜索 → Top-K 相关片段注入 Prompt
- 更新：后期支持通过管理端新增/编辑知识条目

---

## 6. 认证与安全

### 6.1 JWT 方案

- 登录成功后签发：
  - `access_token`: JWT，有效期 30 分钟，携带 userId + role
  - `refresh_token`: UUID，有效期 7 天，存入 Redis（key: `refresh:<userId>:<uuid>`, value: access_token 信息）
- 前端请求头：`Authorization: Bearer <access_token>`
- 登出时：access_token 加入 Redis 黑名单（key: `blacklist:<jti>`, TTL 为 token 剩余有效期）
- 刷新：POST `/api/auth/refresh`，验证 refresh_token → 签发新 access_token

### 6.2 权限控制

- Spring Security + `@PreAuthorize` 注解
- 三级权限：`ROLE_USER`、`ROLE_COACH`、`ROLE_ADMIN`
- 数据隔离：Service 层通过当前登录用户 ID 做数据过滤（用户只能操作自己的数据）

---

## 7. MVP 功能清单

### 用户端
- [x] 注册/登录（JWT + 密码）
- [x] 个人资料管理
- [x] 课程浏览（分类/难度筛选、搜索）
- [x] 课程详情 + 视频播放
- [x] 课程订阅/购买
- [x] 训练打卡记录（跟课程/自由训练）
- [x] 饮食记录（手动录入 + 热量统计）
- [x] 身体数据记录 + 趋势图表
- [x] AI 健身助手对话（SSE 流式）

### 教练端
- [x] 课程发布与管理（上传视频/封面）
- [x] 查看学员列表及训练数据
- [x] 制定下发训练计划

### 管理端
- [x] 用户/教练列表管理（查看、禁用）
- [x] 课程审核（通过/驳回）
- [x] 数据统计面板（用户数、课程数、活跃度）

---

## 8. 非功能需求

- **响应式设计**：适配 320px ~ 1920px 宽度
- **视频播放**：支持断点续播、播放进度上报
- **API 限流**：Spring 拦截器 + Redis 滑动窗口，防止恶意请求
- **文件上传**：前端直传 OSS（STS 临时凭证），后端签发上传策略
- **API 文档**：SpringDoc / Knife4j 自动生成

---

## 9. 开发顺序建议

| 阶段 | 内容 | 依赖 |
|------|------|------|
| Phase 1 | 项目初始化：POM、application.yml、数据源配置、MyBatis-Plus 配置 | — |
| Phase 2 | fitness-common 基础：Entity、枚举、统一返回体、全局异常 | Phase 1 |
| Phase 3 | 认证模块：Spring Security + JWT + Redis | Phase 2 |
| Phase 4 | 用户端核心：用户信息、课程、训练记录 API | Phase 3 |
| Phase 5 | 教练端功能：课程管理、学员管理、训练计划 | Phase 4 |
| Phase 6 | 管理端功能：用户管理、课程审核、统计 | Phase 4 |
| Phase 7 | AI 助手：LangChain4j + RAG + Function Calling | Phase 4 |
| Phase 8 | 前端页面开发 | Phase 4-7 |
| Phase 9 | 联调测试、部署 | Phase 8 |
