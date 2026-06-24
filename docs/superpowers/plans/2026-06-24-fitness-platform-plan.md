# 线上健身平台 — 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 从零搭建综合健身管理平台，包含用户端、教练端、管理端 + AI 健身助手。

**架构：** Spring Boot 3 四模块后端（common/web/admin/ai） + Vue 3 双前端（web/admin），MyBatis-Plus + MySQL + MongoDB + Redis + Pinecone，JWT 认证，LangChain4j AI。

**技术栈：** Spring Boot 3.x, MyBatis-Plus, Spring Security, JWT, Redis, MySQL 8.0, MongoDB, Pinecone, LangChain4j, DeepSeek, Vue 3, Element Plus, Pinia, Axios

---

## 文件结构

### 后端 fitness-java/

```
fitness-java/
├── pom.xml                                    # 父 POM
├── fitness-common/src/main/java/com/fitness/
│   ├── entity/       (User, Course, UserCourse, TrainingRecord, DietRecord, BodyMetrics, TrainingPlan, BaseEntity)
│   ├── dto/request/  (LoginRequest, RegisterRequest, CourseQueryRequest 等)
│   ├── dto/response/ (UserVO, CourseVO, LoginResponse 等)
│   ├── dto/ApiResponse.java
│   ├── enums/        (UserRole, CourseCategory, DifficultyLevel, MealType, CourseStatus)
│   ├── exception/    (BusinessException, GlobalExceptionHandler, ErrorCode)
│   ├── utils/        (JwtUtils, RedisUtils)
│   └── config/       (MyBatisPlusConfig, RedisConfig, WebMvcConfig)
├── fitness-web/src/main/java/com/fitness/
│   ├── FitnessWebApplication.java
│   ├── controller/   (AuthController, UserController, CourseController, TrainingController, DietController,
│   │                  BodyMetricsController, CoachController, TrainingPlanController)
│   ├── service/      (对应 Service 接口)
│   ├── service/impl/ (对应 ServiceImpl)
│   ├── mapper/       (UserMapper, CourseMapper 等 MyBatis-Plus Mapper)
│   └── security/     (JwtAuthFilter, WebSecurityConfig, UserDetailsServiceImpl)
├── fitness-admin/src/main/java/com/fitness/
│   ├── FitnessAdminApplication.java
│   ├── controller/   (AdminUserController, AdminCourseController, DashboardController)
│   ├── service/      (对应 Service 接口)
│   ├── service/impl/ (对应 ServiceImpl)
│   └── mapper/       (对应统计查询 Mapper)
└── fitness-ai/src/main/java/com/fitness/
    ├── FitnessAiApplication.java
    ├── controller/AiChatController.java       # SSE 流式对话
    ├── service/AiChatService.java             # 编排层
    ├── tool/ (TrainingTool, BodyMetricsTool, DietTool, CourseTool, PlanTool)
    ├── rag/   (EmbeddingService, PineconeStore)
    ├── memory/ (ChatHistoryRepository, ChatMemoryProvider)
    └── config/ (LLMConfig, PineconeConfig)
```

### 前端 fitness-front/

```
fitness-front/
├── fitness-front-web/src/
│   ├── api/           (auth.js, user.js, course.js, training.js, diet.js, metrics.js, ai.js)
│   ├── views/         (Login, Register, Home, CourseList, CourseDetail, Training,
│   │                   Diet, BodyMetrics, AiChat, Profile, CoachDashboard 等)
│   ├── components/    (NavBar, VideoPlayer, CalorieChart, MetricChart 等)
│   ├── router/index.js
│   ├── stores/        (user.js, course.js, training.js 等 Pinia stores)
│   └── utils/         (request.js axios 实例 + 拦截器)
├── fitness-front-admin/src/
│   ├── api/           (admin.js)
│   ├── views/         (Login, Dashboard, UserManage, CourseReview, Statistics)
│   ├── components/    (AdminLayout, SideMenu, StatCard)
│   ├── router/index.js
│   ├── stores/        (admin.js)
│   └── utils/request.js
```

---

## Phase 1: 项目基础搭建

### 任务 1.1：创建父 POM 和模块结构

**文件：**
- 创建: `fitness-java/pom.xml`
- 创建: `fitness-java/fitness-common/pom.xml`
- 创建: `fitness-java/fitness-web/pom.xml`
- 创建: `fitness-java/fitness-admin/pom.xml`
- 创建: `fitness-java/fitness-ai/pom.xml`

- [ ] **步骤 1：创建父 POM**

父 POM 管理 Spring Boot 3.x、MyBatis-Plus、Spring Security、JWT、Redis、MongoDB、LangChain4j 等所有依赖版本，以及四个子模块声明。

- [ ] **步骤 2：创建各子模块 POM**

每个子模块引入所需依赖：
- `fitness-common`: mybatis-plus-spring-boot3-starter, lombok, mysql-connector
- `fitness-web`: 依赖 common + spring-boot-starter-security, jjwt, spring-boot-starter-data-redis
- `fitness-admin`: 依赖 common + spring-boot-starter-security
- `fitness-ai`: 依赖 web（复用 UserDetails）+ langchain4j-spring-boot-starter, langchain4j-deepseek, langchain4j-pinecone, spring-boot-starter-data-mongodb

- [ ] **步骤 3：验证 Maven 构建**

```bash
cd "g:\Vibe Coding\Ai-coding-projects\fitness\fitness-java"
mvn compile
```

预期：BUILD SUCCESS（虽有未使用的 import 警告，但无错误）

### 任务 1.2：创建 application.yml 配置文件

**文件：** 每个子模块的 `src/main/resources/application.yml`

- [ ] **步骤 1：fitness-web application.yml**

```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fitness?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
jwt:
  secret: ${JWT_SECRET:your-secret-key-min-32-chars-long}
  access-token-expire: 30  # minutes
  refresh-token-expire: 7  # days
```

- [ ] **步骤 2：fitness-admin application.yml**（端口 8081，其余同上）
- [ ] **步骤 3：fitness-ai application.yml**（端口 8082，额外配置 MongoDB、Pinecone、DeepSeek）
- [ ] **步骤 4：创建 fitness-web 启动类并验证启动**

```java
// FitnessWebApplication.java
@SpringBootApplication
@MapperScan("com.fitness.mapper")
public class FitnessWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessWebApplication.class, args);
    }
}
```

---

## Phase 2: fitness-common 公共模块

### 任务 2.1：创建 BaseEntity 和所有实体类

**文件：**
- 创建: `fitness-common/src/main/java/com/fitness/entity/BaseEntity.java`
- 创建: `fitness-common/src/main/java/com/fitness/entity/User.java`
- 创建: `fitness-common/src/main/java/com/fitness/entity/Course.java`
- 创建: `fitness-common/src/main/java/com/fitness/entity/UserCourse.java`
- 创建: `fitness-common/src/main/java/com/fitness/entity/TrainingRecord.java`
- 创建: `fitness-common/src/main/java/com/fitness/entity/DietRecord.java`
- 创建: `fitness-common/src/main/java/com/fitness/entity/BodyMetrics.java`
- 创建: `fitness-common/src/main/java/com/fitness/entity/TrainingPlan.java`

- [ ] **步骤 1：创建 BaseEntity**

```java
@Data
public class BaseEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **步骤 2：创建 User 实体**

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private UserRole role;     // USER, COACH, ADMIN
    private Integer status;    // 0 禁用 1 正常
}
```

- [ ] **步骤 3：创建 Course、UserCourse、TrainingRecord、DietRecord、BodyMetrics、TrainingPlan 实体**

遵循设计规格中的表结构定义，使用 `@TableName`、`@TableField`、MyBatis-Plus 枚举映射注解。

- [ ] **步骤 4：创建自动填充处理器**

```java
@Component
public class MetaObjectHandler implements com.baomidou.mybatisplus.core.handlers.MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 任务 2.2：创建枚举类

**文件：** 创建 `enums/` 下 5 个枚举类

- [ ] **步骤 1：UserRole 枚举**

```java
@Getter
public enum UserRole implements IEnum<String> {
    USER("USER", "普通用户"),
    COACH("COACH", "教练"),
    ADMIN("ADMIN", "管理员");
    private final String value;
    private final String desc;
    UserRole(String value, String desc) { this.value = value; this.desc = desc; }
}
```

- [ ] **步骤 2：CourseCategory、DifficultyLevel、MealType、CourseStatus 枚举**

类似结构，DifficultyLevel: BEGINNER/INTERMEDIATE/ADVANCED，MealType: BREAKFAST/LUNCH/DINNER/SNACK，CourseStatus: DRAFT/PENDING/APPROVED/REJECTED。

### 任务 2.3：创建统一返回体和全局异常处理

**文件：**
- 创建: `dto/ApiResponse.java`
- 创建: `exception/ErrorCode.java`
- 创建: `exception/BusinessException.java`
- 创建: `exception/GlobalExceptionHandler.java`

- [ ] **步骤 1：ApiResponse 统一返回体**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

- [ ] **步骤 2：ErrorCode 错误码枚举**

```java
@Getter
public enum ErrorCode {
    SUCCESS(200, "成功"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),
    USERNAME_EXISTS(1001, "用户名已存在"),
    LOGIN_FAILED(1002, "用户名或密码错误"),
    TOKEN_EXPIRED(1003, "Token 已过期"),
    COURSE_NOT_FOUND(2001, "课程不存在"),
    USER_DISABLED(3001, "用户已被禁用");
    private final int code;
    private final String message;
}
```

- [ ] **步骤 3：BusinessException**

```java
public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
```

- [ ] **步骤 4：GlobalExceptionHandler**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return ApiResponse.error(400, msg);
    }
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

### 任务 2.4：创建 DTO 基础类

**文件：** 创建 `dto/request/` 和 `dto/response/` 下的基础 DTO

- [ ] **步骤 1：认证相关 DTO**

```java
// LoginRequest.java
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}

// RegisterRequest.java
@Data
public class RegisterRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String username;
    @NotBlank @Size(min = 6, max = 100)
    private String password;
    @NotBlank
    private String nickname;
    private String phone;
}

// LoginResponse.java
@Data @AllArgsConstructor @NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserVO user;
}
```

- [ ] **步骤 2：UserVO**

```java
@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}
```

### 任务 2.5：创建工具类和共享配置

**文件：**
- 创建: `utils/JwtUtils.java`
- 创建: `utils/RedisUtils.java`
- 创建: `config/MyBatisPlusConfig.java`
- 创建: `config/RedisConfig.java`

- [ ] **步骤 1：JwtUtils**

```java
@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access-token-expire}")
    private int accessTokenExpire;

    public String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("role", role)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpire * 60 * 1000L))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build().parseClaimsJws(token).getBody();
    }
}
```

- [ ] **步骤 2：RedisUtils** — 封装 `RedisTemplate` 常用操作（set/get/delete/hasKey/expire）
- [ ] **步骤 3：MyBatisPlusConfig** — 配置分页插件、MetaObjectHandler 注册
- [ ] **步骤 4：RedisConfig** — 配置 Jackson2JsonRedisSerializer 序列化

---

## Phase 3: 认证模块（fitness-web）

### 任务 3.1：创建 Mapper 层

**文件：**
- 创建: `fitness-web/src/main/java/com/fitness/mapper/UserMapper.java`

- [ ] **步骤 1：UserMapper**

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus BaseMapper 提供基础 CRUD，无需额外方法
}
```

### 任务 3.2：实现认证 Service

**文件：**
- 创建: `fitness-web/src/main/java/com/fitness/service/AuthService.java`
- 创建: `fitness-web/src/main/java/com/fitness/service/impl/AuthServiceImpl.java`

- [ ] **步骤 1：AuthService 接口**

```java
public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserVO register(RegisterRequest request);
    LoginResponse refresh(String refreshToken);
    void logout(String accessToken, Long userId);
}
```

- [ ] **步骤 2：AuthServiceImpl 实现**

核心逻辑：
- `login`: 查 UserMapper 验证用户名密码（BCrypt），生成 accessToken + refreshToken，refreshToken 存 Redis（`refresh:<userId>:<uuid>`），返回 LoginResponse
- `register`: 检查用户名唯一性，BCrypt 加密密码，插入 user 表，返回 UserVO
- `refresh`: 验证 Redis 中的 refreshToken，签发新 accessToken
- `logout`: 将 accessToken 的 jti 加入 Redis 黑名单（TTL=剩余有效期），删除 refreshToken

### 任务 3.3：实现 Spring Security 配置

**文件：**
- 创建: `fitness-web/src/main/java/com/fitness/security/JwtAuthFilter.java`
- 创建: `fitness-web/src/main/java/com/fitness/security/UserDetailsServiceImpl.java`
- 创建: `fitness-web/src/main/java/com/fitness/security/WebSecurityConfig.java`

- [ ] **步骤 1：JwtAuthFilter** — OncePerRequestFilter，从 Header 提取 Bearer token，校验 JWT + 黑名单，设置 SecurityContext
- [ ] **步骤 2：UserDetailsServiceImpl** — 通过 UserMapper 查用户，包装为 Spring Security UserDetails
- [ ] **步骤 3：WebSecurityConfig** — 配置 SecurityFilterChain：放行 `/api/auth/**`，其他接口需要认证；添加 JwtAuthFilter；配置无状态 Session

### 任务 3.4：创建 AuthController

**文件：** 创建 `fitness-web/src/main/java/com/fitness/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request);
    @PostMapping("/register")
    public ApiResponse<UserVO> register(@Valid @RequestBody RegisterRequest request);
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Authorization") String refreshToken);
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token);
}
```

- [ ] **步骤：启动应用，用 curl 测试登录/注册接口**

```bash
# 测试注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","nickname":"测试用户"}'
# 预期返回：200 + UserVO

# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'
# 预期返回：200 + LoginResponse (accessToken + refreshToken + UserVO)
```

---

## Phase 4: 用户端核心 API（fitness-web）

### 任务 4.1：用户信息管理

**文件：** 创建 `fitness-web/src/main/java/com/fitness/controller/UserController.java` 及对应 Service/Mapper

- [ ] **步骤 1：GET `/api/user/profile`** — 获取当前登录用户信息
- [ ] **步骤 2：PUT `/api/user/profile`** — 更新昵称、头像、手机号
- [ ] **步骤 3：PUT `/api/user/password`** — 修改密码（需验证旧密码）

### 任务 4.2：课程浏览 API

**文件：**
- 创建: `fitness-web/src/main/java/com/fitness/mapper/CourseMapper.java`
- 创建: `fitness-web/src/main/java/com/fitness/service/CourseService.java` + `impl/CourseServiceImpl.java`
- 创建: `fitness-web/src/main/java/com/fitness/controller/CourseController.java`

- [ ] **步骤 1：GET `/api/courses`** — 分页查询课程列表，支持 category / difficulty / keyword 筛选，按时间倒序
- [ ] **步骤 2：GET `/api/courses/{id}`** — 课程详情（含教练信息）
- [ ] **步骤 3：POST `/api/courses/{id}/subscribe`** — 用户订阅课程（免费直接订阅，付费预留）
- [ ] **步骤 4：GET `/api/user/courses`** — 我的课程列表（已订阅 + 学习进度）

### 任务 4.3：训练记录 API

**文件：** 创建 TrainingRecord 相关 Controller/Service/Mapper

- [ ] **步骤 1：POST `/api/training/records`** — 添加训练记录（跟课程或自由训练）
- [ ] **步骤 2：GET `/api/training/records`** — 查询近期训练记录列表（支持日期范围筛选）
- [ ] **步骤 3：GET `/api/training/records/{id}`** — 单条训练记录详情

### 任务 4.4：饮食记录 API

**文件：** 创建 DietRecord 相关 Controller/Service/Mapper

- [ ] **步骤 1：POST `/api/diet/records`** — 添加饮食记录
- [ ] **步骤 2：GET `/api/diet/records`** — 按日期查询饮食记录，汇总当天总热量/三大营养素
- [ ] **步骤 3：DELETE `/api/diet/records/{id}`** — 删除某条饮食记录

### 任务 4.5：身体数据 API

**文件：** 创建 BodyMetrics 相关 Controller/Service/Mapper

- [ ] **步骤 1：POST `/api/metrics`** — 记录身体数据，自动计算 BMI
- [ ] **步骤 2：GET `/api/metrics`** — 查询身体数据列表（支持日期范围），用于绘制趋势图
- [ ] **步骤 3：GET `/api/metrics/latest`** — 获取最新一条身体数据

---

## Phase 5: 教练端功能（fitness-web）

### 任务 5.1：教练课程管理 API

**文件：**
- 创建: `fitness-web/src/main/java/com/fitness/controller/CoachController.java`
- 创建: `fitness-web/src/main/java/com/fitness/service/CoachService.java` + impl

- [ ] **步骤 1：POST `/api/coach/courses`** — 创建课程（上传标题、描述、分类、难度、封面/视频 URL、价格）
- [ ] **步骤 2：PUT `/api/coach/courses/{id}`** — 编辑课程信息
- [ ] **步骤 3：GET `/api/coach/courses`** — 教练查看自己的课程列表（含审核状态）
- [ ] **步骤 4：DELETE `/api/coach/courses/{id}`** — 下架课程
- [ ] **步骤 5：实现 `@PreAuthorize("hasRole('COACH')")` 权限校验**

### 任务 5.2：学员管理 API

- [ ] **步骤 1：GET `/api/coach/students`** — 教练查看订阅了自己课程的学员列表
- [ ] **步骤 2：GET `/api/coach/students/{userId}/training`** — 查看某个学员的训练记录
- [ ] **步骤 3：GET `/api/coach/students/{userId}/metrics`** — 查看某个学员的身体数据

### 任务 5.3：训练计划管理 API

- [ ] **步骤 1：POST `/api/coach/plans`** — 为学员创建训练计划（标题、内容 JSON、起止日期）
- [ ] **步骤 2：GET `/api/coach/plans`** — 查看自己下发的所有训练计划
- [ ] **步骤 3：GET `/api/user/plans`** — 学员查看教练下发的训练计划

---

## Phase 6: 管理端 API（fitness-admin）

### 任务 6.1：管理端认证与启动

**文件：** 创建 fitness-admin 启动类和 Security 配置

- [ ] **步骤 1：创建 FitnessAdminApplication**（端口 8081，复用 fitness-common 模块）
- [ ] **步骤 2：配置 Spring Security，仅 ADMIN 角色可访问管理端接口**
- [ ] **步骤 3：验收：用普通用户 token 访问管理端接口返回 403**

### 任务 6.2：用户管理 API

**文件：** 创建 AdminUserController + AdminUserService

- [ ] **步骤 1：GET `/api/admin/users`** — 分页查询所有用户（支持角色/状态筛选）
- [ ] **步骤 2：PUT `/api/admin/users/{id}/status`** — 启用/禁用用户
- [ ] **步骤 3：GET `/api/admin/users/{id}`** — 查看用户详情

### 任务 6.3：课程审核 API

**文件：** 创建 AdminCourseController + AdminCourseService

- [ ] **步骤 1：GET `/api/admin/courses`** — 查询待审核课程列表
- [ ] **步骤 2：PUT `/api/admin/courses/{id}/approve`** — 课程审核通过
- [ ] **步骤 3：PUT `/api/admin/courses/{id}/reject`** — 课程驳回（需填写驳回原因）

### 任务 6.4：数据统计 API

**文件：** 创建 DashboardController + DashboardService

- [ ] **步骤 1：GET `/api/admin/dashboard`** — 返回总用户数、总课程数、今日活跃用户数、近 7 天新增用户趋势

---

## Phase 7: AI 助手模块（fitness-ai）

### 任务 7.1：fitness-ai 基础配置

**文件：**
- 创建: `fitness-ai/src/main/java/com/fitness/FitnessAiApplication.java`
- 创建: `fitness-ai/src/main/java/com/fitness/config/LLMConfig.java`
- 创建: `fitness-ai/src/main/java/com/fitness/config/PineconeConfig.java`
- 创建: `fitness-ai/src/main/resources/application.yml`

- [ ] **步骤 1：启动类 + application.yml**（配置 DeepSeek API key、Pinecone API key + index、MongoDB 连接）
- [ ] **步骤 2：LLMConfig** — 配置 ChatLanguageModel（DeepSeek）、EmbeddingModel（DeepSeek Embedding）
- [ ] **步骤 3：PineconeConfig** — 配置 Pinecone 连接（host、apiKey、indexName）

### 任务 7.2：对话记忆模块（MongoDB）

**文件：**
- 创建: `fitness-ai/src/main/java/com/fitness/memory/ChatHistory.java`
- 创建: `fitness-ai/src/main/java/com/fitness/memory/ChatHistoryRepository.java`
- 创建: `fitness-ai/src/main/java/com/fitness/memory/ChatMemoryProvider.java`

- [ ] **步骤 1：ChatHistory 文档实体**

```java
@Document(collection = "chat_history")
@Data
public class ChatHistory {
    @Id
    private String id;
    private Long userId;
    private String sessionId;
    private List<ChatMessage> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
public class ChatMessage {
    private String role;     // USER / ASSISTANT
    private String content;
    private List<ToolCall> toolCalls;
    private LocalDateTime timestamp;
}
```

- [ ] **步骤 2：ChatHistoryRepository**

```java
public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {
    Optional<ChatHistory> findByUserIdAndSessionId(Long userId, String sessionId);
    List<ChatHistory> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
```

- [ ] **步骤 3：ChatMemoryProvider** — 实现 LangChain4j `ChatMemoryStore`，读写 MongoDB

### 任务 7.3：RAG 知识库

**文件：**
- 创建: `fitness-ai/src/main/java/com/fitness/rag/EmbeddingService.java`
- 创建: `fitness-ai/src/main/java/com/fitness/rag/PineconeStoreService.java`
- 创建: `fitness-ai/src/main/resources/fitness-knowledge/init-knowledge.md`

- [ ] **步骤 1：EmbeddingService** — 调用 DeepSeek Embedding API 做文本向量化
- [ ] **步骤 2：PineconeStoreService** — 初始化知识库：读取 `init-knowledge.md`，分段 → Embedding → 写入 Pinecone
- [ ] **步骤 3：实现 `search(String query, int topK)`** — 查询向量化 → Pinecone 相似度检索 → 返回 Top-K 文本片段

### 任务 7.4：Function Calling Tools

**文件：** 创建 `tool/` 下 5 个 Tool 类

- [ ] **步骤 1：TrainingTool**

```java
@Component
public class TrainingTool {
    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

    @Tool("查询用户近期训练记录")
    public List<TrainingSummary> queryTrainingRecords(
        @P("用户ID") Long userId,
        @P("查询天数，默认7") int days) {
        // 查询近 N 天训练记录，汇总次数、总时长、总消耗
    }
}
```

- [ ] **步骤 2：BodyMetricsTool** — 查询身体数据趋势，返回体重/体脂变化序列
- [ ] **步骤 3：DietTool** — 查询某日期饮食记录，汇总热量+三大营养素
- [ ] **步骤 4：CourseTool** — 搜索课程库（按关键词/分类/难度）
- [ ] **步骤 5：PlanTool** — `generateTrainingPlan(userId, goal, weeks)`，基于用户当前数据 + 目标输出结构化训练计划

### 任务 7.5：AiChatService 编排层

**文件：**
- 创建: `fitness-ai/src/main/java/com/fitness/service/AiChatService.java`

- [ ] **步骤 1：实现完整对话编排流程**

```java
@Service
public class AiChatService {
    public Flux<String> chat(Long userId, String sessionId, String userMessage) {
        // 1. 从 MongoDB 加载历史对话 → ChatMemory
        // 2. 用 AiServices.builder() 创建 Assistant
        //    - 注册 5 个 @Tool
        //    - 设置 RAG 检索器（ContentRetriever）
        //    - 设置 ChatMemory
        // 3. 流式调用 assistant.chat(userMessage)
        // 4. 将本轮对话 append 到 ChatHistory → 保存 MongoDB
        // 5. 返回 Flux<String>（SSE 流式输出）
    }
}
```

### 任务 7.6：AiChatController（SSE 流式对话）

**文件：** 创建 `fitness-ai/src/main/java/com/fitness/controller/AiChatController.java`

- [ ] **步骤 1：SSE 流式接口**

```java
@RestController
@RequestMapping("/api/ai")
public class AiChatController {
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
        @AuthenticationPrincipal Long userId,
        @RequestParam String message,
        @RequestParam(defaultValue = "default") String sessionId) {
        return aiChatService.chat(userId, sessionId, message);
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatHistory>> getHistory(
        @AuthenticationPrincipal Long userId) {
        // 返回用户的对话历史列表
    }
}
```

- [ ] **步骤 2：验证流式对话** — 用 curl 测试 SSE 流式返回

---

## Phase 8: 前端开发

### 任务 8.1：前端公共基础设施

**文件：** 修改 `fitness-front-web/src/` 下现有文件

- [ ] **步骤 1：配置 Axios 实例（utils/request.js）** — baseURL + 请求拦截器（注入 JWT）+ 响应拦截器（401 自动刷新 token / 跳转登录）
- [ ] **步骤 2：创建认证 store（stores/user.js）** — state: { user, token, refreshToken } + actions: { login, register, logout, refreshToken }
- [ ] **步骤 3：配置路由（router/index.js）** — 公开路由（/login, /register）+ 受保护路由（/home, /courses, /training, /diet, /metrics, /ai, /profile）+ 教练路由（/coach/*）+ 路由守卫

### 任务 8.2：用户端核心页面

**视图创建：**

- [ ] **Login.vue** — 用户名 + 密码表单，Element Plus 的 el-form，登录成功存储 token 跳转首页
- [ ] **Register.vue** — 用户名 + 密码 + 昵称 + 手机号（可选），注册成功跳转登录
- [ ] **Home.vue** — 首页：用户欢迎卡片（今日训练状态）+ 推荐课程列表 + 快捷入口（训练/饮食/身体数据/AI助手）
- [ ] **CourseList.vue** — 课程列表：分类 Tab + 搜索框 + 难度筛选 + 课程卡片网格（封面/标题/教练/难度/时长）
- [ ] **CourseDetail.vue** — 课程详情：视频播放器 + 课程信息 + 训练打卡按钮 + 进度条
- [ ] **Training.vue** — 训练记录页：日历视图 + 训练记录列表 + 添加记录抽屉（el-drawer）
- [ ] **Diet.vue** — 饮食记录页：日期选择 + 三餐表格 + 热量/营养素汇总 + 添加记录对话框
- [ ] **BodyMetrics.vue** — 身体数据页：最新数据卡片 + 添加记录表单 + 体重/体脂趋势折线图（使用 Element Plus 或 ECharts）
- [ ] **AiChat.vue** — AI 对话页：聊天界面（对话气泡 + 流式输出 + Markdown 渲染）+ 输入框 + 新建会话

### 任务 8.3：教练端页面

- [ ] **CoachDashboard.vue** — 教练工作台：学员数/课程数统计 + 快捷入口
- [ ] **CoachCourses.vue** — 课程管理：课程列表 Table + 新建/编辑课程对话框（视频上传）
- [ ] **CoachStudents.vue** — 学员管理：学员 Table + 点击查看训练数据/身体数据
- [ ] **CoachPlans.vue** — 训练计划：创建/编辑计划（el-form + 日期范围 + 富文本内容）

### 任务 8.4：管理端页面

**文件：** 修改 `fitness-front-admin/src/` 下现有文件

- [ ] **AdminLayout.vue** — 管理端布局：侧边菜单（用户管理/课程审核/统计）+ 顶部栏 + 主内容区
- [ ] **Login.vue** — 管理员登录（与用户端类似但跳转到管理端 Dashboard）
- [ ] **Dashboard.vue** — 统计面板：总用户数/课程数/活跃用户，7 天新增用户趋势图
- [ ] **UserManage.vue** — 用户管理 Table：分页 + 角色筛选 + 状态开关 + 查看详情
- [ ] **CourseReview.vue** — 课程审核：待审核课程列表 + 预览 + 通过/驳回操作

---

## Phase 9: 联调测试与部署

### 任务 9.1：端到端联调

- [ ] **步骤 1：启动所有后端服务**（fitness-web:8080、fitness-admin:8081、fitness-ai:8082）
- [ ] **步骤 2：启动两个前端项目**（fitness-front-web、fitness-front-admin）
- [ ] **步骤 3：走通完整用户流程**：注册 → 登录 → 浏览课程 → 订阅 → 打卡训练 → 记录饮食 → 记录身体数据 → 与 AI 对话 → 教练发布课程 → 管理端审核通过 → 教练管理学员 → 管理端查看统计
- [ ] **步骤 4：记录联调中发现的问题，逐一修复**

### 任务 9.2：数据库初始化脚本

**文件：** 创建 `fitness-java/docs/schema.sql`

- [ ] **步骤：编写完整建表 SQL**，基于 Phase 2 实体类生成，包含索引（user.username UNIQUE、user_course (user_id, course_id) UNIQUE 等）

### 任务 9.3：知识库初始化数据

**文件：** 创建 `fitness-java/fitness-ai/src/main/resources/fitness-knowledge/`

- [ ] **步骤：编写初始健身知识文档**（训练原理、营养学基础、动作库、常见问题），供 EmbeddingService 初始化 Pinecone

---

## 验证清单

- [ ] 父 POM 构建成功：`mvn clean compile`
- [ ] 三个 Spring Boot 服务正常启动无报错
- [ ] 注册/登录 API 返回正确的 JWT token
- [ ] 未登录访问受保护 API 返回 401
- [ ] 普通用户访问管理端 API 返回 403
- [ ] 课程 CRUD API 完整可用
- [ ] 训练/饮食/身体数据 CRUD 完整可用
- [ ] 教练只能管理自己的课程和学员
- [ ] AI 助手流式对话正常返回
- [ ] AI 助手能通过 Function Calling 查询用户训练数据
- [ ] 前端用户端基础页面可交互
- [ ] 前端管理端基础页面可交互
