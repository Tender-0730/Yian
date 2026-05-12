# 项目编码规范（CLAUDE.md）

> 基于《阿里巴巴 Java 开发手册》+ Spring Boot 社区最佳实践
> AI 编码时必须严格遵守本文件所有规范，不得自行发挥。

## 所有回复必须使用中文

---

## 一、技术栈

| 层面 | 选型 | 版本 | 说明 |
|------|------|------|------|
| 语言 | Java | 21 | Virtual Threads 已启用 |
| 框架 | Spring Boot | 3.5.x | |
| ORM | MyBatis-Plus | 3.5.9 | 增强版 MyBatis，内置分页、逻辑删除 |
| 数据库 | MySQL | 8.x | InnoDB，utf8mb4 |
| 连接池 | HikariCP | | Spring Boot 默认 |
| 安全 | Spring Security + JWT | jjwt 0.12.6 | 无状态认证 |
| 密码加密 | BCrypt | | |
| API 文档 | Knife4j (SpringDoc) | 4.5.0 | OpenAPI 3.0 标准 |
| 工具库 | Hutool + Lombok | hutool 5.8.x | |
| 参数校验 | Jakarta Validation | | @Valid + @Validated |
| 构建工具 | Maven | 3.8+ | |
| 代码规范 | 阿里巴巴 Java 开发手册（P3C） | | |

---

## 二、项目目录结构

```
backend/src/main/java/com/yian/
├── controller/        # HTTP 路由入口，只做收发
├── service/           # 业务逻辑接口
│   └── impl/          # 业务逻辑实现（XxxServiceImpl）
├── mapper/            # 数据访问层，继承 BaseMapper<Entity>
├── entity/            # 数据库表映射（MyBatis-Plus 注解）
├── dto/               # 接口入参 DTO（XxxRequest / XxxQuery）
├── vo/                # 接口出参 VO（XxxVO）
├── exception/         # 自定义异常 + GlobalExceptionHandler
├── common/            # Result、PageResult、ResultCode、BusinessException 等
├── config/            # 配置类 @Configuration
├── security/          # JwtUtils、JwtAuthenticationFilter、LoginUser
├── enums/             # 枚举类
└── constant/          # 常量类

backend/src/main/resources/
├── application.yml          # 公共配置（profiles、datasource、mybatis-plus、jwt 等）
├── application-dev.yml      # 开发环境（MySQL 本地）
├── application-prod.yml     # 生产环境（MySQL，敏感信息用环境变量，不提交 git）
├── mapper/                  # MyBatis XML（复杂 SQL 才需要）
└── db/
    ├── schema.sql           # 建表 DDL
    └── data.sql             # 初始化数据
```

---

## 三、分层职责规范（禁止越界）

### Controller 层
- 只负责：接收请求、参数校验、调用 Service、返回响应
- 禁止：写任何业务逻辑、直接调用 Mapper、写 SQL
- 返回值：统一用 `Result<T>` 包装，分页用 `PageResult<T>`
- 注解：`@RestController` + `@RequestMapping` + `@RequiredArgsConstructor`
- 参数校验：DTO 中使用 Jakarta Validation，Controller 参数加 `@Valid`

### Service 层
- 必须：先定义 interface，再写 XxxServiceImpl 实现
- 只负责：核心业务逻辑、事务管理、Entity 与 VO/DTO 的转换
- 禁止：直接处理 HTTP 相关（HttpServletRequest 等）
- 事务：写操作加 `@Transactional`，查询加 `@Transactional(readOnly = true)`
- 复杂分页/条件查询优先用 MyBatis-Plus Wrapper，其次写 XML

### Mapper 层
- 继承 `BaseMapper<Entity>`，加 `@Mapper` 注解
- 简单 CRUD 用 MyBatis-Plus 内置方法，无需写 SQL
- 复杂查询写在 Mapper 接口（`@Select` / XML）
- 禁止：写任何业务逻辑

### Entity 层
- 注解：`@TableName("表名")` + `@Data`
- 主键：`@TableId(type = IdType.AUTO)`
- 时间字段：`@TableField(fill = FieldFill.INSERT / INSERT_UPDATE)` 配合自动填充
- 逻辑删除：`@TableLogic`，字段名 `is_deleted`
- 禁止：直接作为 Controller 的返回值（必须转为 VO）
- 禁止：使用 `@ManyToOne` / `@OneToMany` 等关联（用逻辑外键）

### DTO（入参）
- 完整命名：`XxxRequest`（新增/编辑）、`XxxQuery`（分页查询）
- 所有校验注解写在这里（`@NotBlank`、`@NotNull` 等）
- 使用 `@Data`

### VO（出参）
- 完整命名：`XxxVO`
- 只包含需要返回的字段，绝对不含 password 等敏感字段
- 使用 `@Data` + `@Builder`

---

## 四、命名规范（阿里规范）

### 类命名
| 类型 | 规则 | 示例 |
|------|------|------|
| Controller | 业务名 + Controller | `ResidentController` |
| Service 接口 | 业务名 + Service | `ResidentService` |
| Service 实现 | 业务名 + ServiceImpl | `ResidentServiceImpl` |
| Mapper | 实体名 + Mapper | `ResidentMapper` |
| Entity | 业务名（单数名词）| `Resident`、`Room` |
| 入参 DTO | 业务名 + Request / Query | `ResidentSaveRequest`、`ResidentPageQuery` |
| 出参 VO | 业务名 + VO | `ResidentVO`、`ResidentDetailVO` |
| 异常类 | 业务名 + Exception | `UserNotFoundException` |
| 枚举类 | 业务名 + 后缀 | `GenderEnum`、`ResidentStatusEnum` |

### 方法命名
| 场景 | 规则 | 示例 |
|------|------|------|
| 查询单个 | getXxxById | `getResidentById` |
| 查询列表 | listXxx | `listResidents` |
| 分页查询 | pageXxx | `pageResidents` |
| 新增 | createXxx / saveXxx | `createResident` |
| 修改 | updateXxx | `updateResident` |
| 删除 | deleteXxx | `deleteResident` |
| 判断存在 | existsXxx | `existsByIdCard` |

### 变量 / 字段命名
- 小驼峰：`userId`、`createdAt`、`residentStatus`
- 常量：全大写下划线，`MAX_RETRY_COUNT`、`DEFAULT_PAGE_SIZE`
- 布尔字段：不加 is 前缀（数据库字段 `is_deleted`，实体字段 `deleted`）
- 集合变量：用复数或加 List/Map 后缀，`residentList`、`roomMap`

### 数据库命名
- 表名：小写下划线，`resident`、`health_record`、`meal_record`
- 字段名：小写下划线，`user_id`、`created_at`、`is_deleted`
- 主键：统一命名 `id`，类型 `BIGINT`，自增
- 时间字段：`created_at`、`updated_at`，类型 `DATETIME`
- 所有表/字段必须加 `COMMENT`
- 索引命名：`idx_字段名`（普通）、`uk_字段名`（唯一）
- 字符集：`utf8mb4`，排序规则：`utf8mb4_general_ci`
- 引擎：`InnoDB`

---

## 五、统一响应规范

### ApiResponse（已实现为 Result.java）

```json
// 成功
{ "code": 200, "message": "success", "data": { ... } }

// 分页
{ "code": 200, "message": "success", "data": { "records": [...], "total": 100, "page": 1, "size": 10 } }

// 业务异常
{ "code": 400, "message": "原密码错误", "data": null }

// 认证失败
{ "code": 401, "message": "token已过期，请重新登录", "data": null }

// 权限不足
{ "code": 403, "message": "无操作权限", "data": null }

// 系统异常
{ "code": 500, "message": "系统繁忙，请稍后重试", "data": null }
```

### ResultCode 枚举
- `SUCCESS(200)` — 操作成功
- `BAD_REQUEST(400)` — 参数错误
- `UNAUTHORIZED(401)` — 未认证
- `FORBIDDEN(403)` — 无权限
- `NOT_FOUND(404)` — 资源不存在
- `INTERNAL_ERROR(500)` — 系统异常

### 全局异常处理
- `@RestControllerAdvice` + `@ExceptionHandler`
- `BusinessException`（业务异常）→ 返回对应 ResultCode
- `MethodArgumentNotValidException`（参数校验）→ 400
- `AccessDeniedException`（权限不足）→ 403
- `Exception`（兜底）→ 500，不暴露异常详情

---

## 六、JWT 认证规范

### Token 策略
| 属性 | 值 |
|------|------|
| Token 类型 | JWT (HMAC-SHA256) |
| accessToken 有效期 | 2 小时 |
| refreshToken 有效期 | 7 天 |
| 密钥 | 环境变量 `JWT_SECRET`，开发环境默认值 32+ 字符 |
| 存储 | 前端 localStorage，请求头 `Authorization: Bearer <token>` |

### 安全配置
- Spring Security 过滤器链：`JwtAuthenticationFilter`（继承 `OncePerRequestFilter`）
- 放行路径：`/api/auth/login`、`/api/auth/register`、`/api/auth/refresh`、Knife4j 文档路径
- 密码编码器：`BCryptPasswordEncoder`
- SecurityContext 用户主体：`LoginUser`（实现 `UserDetails`）

---

## 七、安全规范

- 生产环境密码、密钥等**必须**用环境变量，不得硬编码
- `application-prod.yml` 不提交 git（加入 `.gitignore`）
- 密码存储必须用 BCrypt 加密，禁止明文
- VO 中禁止出现 `password`、`secret` 等敏感字段
- 禁止直接拼接 SQL（MyBatis-Plus 参数化查询天然防注入）

---

## 八、禁止事项清单

以下行为 AI 生成代码时绝对禁止：

- [ ] 禁止 `System.out.println`，用 `@Slf4j` + `log.info/warn/error`
- [ ] 禁止字段注入 `@Autowired`，用构造注入（`@RequiredArgsConstructor`）
- [ ] 禁止 Entity 直接作为接口返回值，必须转为 VO
- [ ] 禁止在 Controller 写业务逻辑
- [ ] 禁止在 Mapper 写业务逻辑
- [ ] 禁止捕获异常后 `return null` 或静默吞掉
- [ ] 禁止 password 等敏感字段出现在 VO
- [ ] 禁止硬编码数据库密码、密钥等敏感配置
- [ ] 禁止在循环内做数据库查询（N+1 问题）
- [ ] 禁止方法超过 80 行，超过必须拆分
- [ ] 禁止一个类超过 500 行，超过必须拆分
- [ ] 禁止魔法值直接出现在代码里，用常量或枚举替代
- [ ] 禁止数据库使用外键约束，用逻辑外键
- [ ] 禁止不带 `COMMENT` 的建表语句
