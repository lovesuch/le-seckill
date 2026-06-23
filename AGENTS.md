# AGENTS.md - 团队 AI 开发规则

> 此文件由 TeamCop 自动生成，用于约束 AI 在工作空间中的所有行为。
> 工作空间: 秒杀活动

## 工作空间信息
- ID: f356af6e-377f-4911-852e-9e0449cc2327
- 涉及服务: ["user-service", "order-service"]
- 当前阶段: null
- 进度: 0%

## 团队知识库摘要

## 服务规范: REST API 设计规范
1. 统一使用小写短横线命名: /api/v1/user-profiles
2. 资源用名词复数: /users, /workspaces, /pipelines
3. HTTP 方法语义: GET(查), POST(增), PUT(全量改), PATCH(部分改), DELETE(删)
4. 状态码: 200成功, 201创建, 204无内容, 400参数错误, 401未认证, 403无权限, 404不存在, 500服务异常
5. 分页统一返回: {items:[], total:0, page:1, size:20}
6. 错误响应格式: {code:"ERR_CODE", message:"用户可读描述", details:[]}

## 服务规范: 统一异常处理规范
1. 所有 Controller 层异常由 GlobalExceptionHandler 统一捕获
2. 业务异常继承 BizException(ErrorCode code)
3. ErrorCode 枚举集中管理: AUTH_001~AUTH_999, BIZ_001~BIZ_999, SYS_001~SYS_999
4. 异常日志: BizException 记录 WARN, SystemException 记录 ERROR
5. 响应中不暴露堆栈信息，仅返回 code + message
6. 第三方调用异常统一包装为 ExternalServiceException

## 服务规范: MyBatis Plus 开发规范
1. Entity 必须使用 @TableName 注解，字段使用 @TableField
2. 查询优先使用 LambdaQueryWrapper，避免硬编码列名
3. 分页查询必须使用 Page<T>，配合 PaginationInnerInterceptor
4. 逻辑删除统一使用 @TableLogic，字段名 deleted
5. 自动填充: created_at, updated_at 使用 MetaObjectHandler
6. 批量操作使用 saveBatch/updateBatchById，单次不超过 500 条
7. 删除使用 removeById（3.5.9+），deleteById 已废弃

## 业务规则: AI 编程命令体系
TeamCop 提供 50+ Slash 命令，覆盖需求→方案→开发→测试→上线全链路:

需求阶段: /init(初始化), /prd(生成PRD)
方案阶段: /design(技术方案), /api-design(API设计)
开发阶段: /code(代码生成), /review(代码审查), /refactor(重构)
测试阶段: /test(单元测试), /integration-test(集成测试)
运维阶段: /deploy(部署), /health(健康检查), /rollback(回滚)
知识阶段: /learn(学习纠错), /sync(知识同步)

所有命令通过 CommandDispatcher 统一分发，支持知识注入和上下文增强。

## 业务规则: 工作空间协同模型
工作空间是 TeamCop 的核心协同单元，遵循"一个需求一个空间"原则:

1. 生命周期: created → analyzing → designing → developing → testing → deploying → completed
2. 每个空间绑定: PRD文档、技术方案、代码变更、测试报告
3. 成员角色: owner(创建者), member(协作者), viewer(观察者)
4. 服务拆分: 通过 services JSON 字段记录涉及的服务列表
5. 分支策略: branches JSON 字段记录各服务的工作分支
6. 文件版本: total_file_versions 跟踪文件变更次数

## 业务规则: 三层知识库架构
TeamCop 知识库采用三层架构:

Layer 1 - 服务规范:
REST API 规范、数据库规范、异常处理规范、编码规范等通用技术标准。

Layer 2 - 业务域知识:
业务领域模型、业务流程、命令体系、工作空间模型等业务相关知识。

Layer 3 - 系统架构:
微服务分层、技术选型、部署架构、安全模型等架构决策。

知识条目支持版本管理(knowledge_version)、语义搜索(Qdrant向量)、标签分类。
命令执行时通过 CommandDispatcher 自动注入相关知识上下文。

## 系统架构: Spring Boot 微服务分层架构
TeamCop 后端采用经典分层架构:

Controller 层: REST API 入口，参数校验，响应封装
Service 层: 业务逻辑编排，事务管理
Mapper 层: MyBatis Plus 数据访问
Entity 层: 数据库实体映射
DTO 层: 数据传输对象，入参/出参分离

横切关注点:
- 统一认证: JwtAuthFilter + @CurrentUserId
- 统一审计: AuditAspect 自动记录操作日志
- 统一限流: RateLimitInterceptor
- 统一异常: GlobalExceptionHandler

## 系统架构: SSE 实时事件流架构
TeamCop 使用 Server-Sent Events (SSE) 实现实时通知:

1. 连接管理: SseEmitterManager 统一管理所有活跃连接
2. 认证: SseAuthFilter 在 SSE 连接建立时验证 JWT
3. 事件类型: notification, pipeline_step, task_complete, command_progress
4. 断线重连: 前端 sseService 自动重连，指数退避策略
5. 广播: 支持全局广播(系统通知)和用户定向推送(个人通知)
6. 心跳: 每 30 秒发送心跳保持连接存活
7. Token 续期: 前端在 token 过期前 60 秒自动刷新

## 系统架构: 数据库选型与规范
数据库: MySQL 8.0+
ORM: MyBatis Plus 3.5.9
迁移: Flyway

核心规范:
1. 表名小写下划线: knowledge_item, execution_task
2. 主键: BIGINT AUTO_INCREMENT
3. 时间字段: created_at/updated_at, DATETIME 类型
4. 逻辑删除: deleted TINYINT DEFAULT 0
5. 索引命名: idx_前缀, 唯一键 uk_前缀
6. 迁移幂等: CREATE TABLE IF NOT EXISTS, INSERT ... ON DUPLICATE KEY UPDATE
7. 禁止在迁移中使用 DELIMITER 和存储过程(Flyway 9.22+ 限制)

## 系统架构: 安全与认证体系
TeamCop 安全体系包含:

认证:
- JWT Token: 有效期 2h, Refresh Token 有效期 7d
- SSO: 支持 OAuth2/OIDC (Google, GitHub, Azure AD)
- 主动续期: 前端在 token 过期前自动刷新

授权:
- RBAC 三角色: admin(全权限), developer(开发权限), viewer(只读)
- PermissionService + Redis 缓存(10分钟TTL)
- 接口级: @PreAuthorize 注解

安全加固:
- CORS: 生产环境收紧允许域名
- XSS: 输入过滤 + Content-Security-Policy
- 审计: AuditAspect 记录所有敏感操作
- API 限流: RateLimitInterceptor 防止滥用


## 开发规范
1. 所有代码必须遵循团队知识库中定义的编码规范
2. 新增 API 必须遵循 RESTful 设计规范
3. 数据库变更必须通过 Flyway 迁移脚本
4. 所有公共方法必须添加单元测试
5. 发现错误时，必须通过 /learn 命令记录纠错经验

## 约束规则
- 不得删除现有文件，除非明确要求
- Git 提交必须使用规范的 commit message
- 跨服务修改必须同时更新所有相关服务
- 生成的代码必须通过 /review 命令审查

## 复杂需求处理 (SubAgent)
- 对于涉及超过 3 个服务的复杂需求，采用主从 Agent 模式
- 主 Agent 负责整体架构设计与验收，子 Agent 负责具体模块实现
- 确保各子任务上下文隔离，避免信息过载
