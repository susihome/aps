# APS 实施进度

**更新时间**: 2026-04-13
**当前分支**: `master`

## 当前阶段

当前在推进“多实例部署改造”的第一批实现，采用 `TDD` 方式执行，并要求每批完成后做一次 review。

本轮目标是先落地最小可闭环的“多实例认证 / 会话管理”能力，暂不进入 RabbitMQ 异步排产和对象存储改造。

## 本轮已完成

### 1. 多实例认证核心实现

- 新增 `AuthSession` 实体
- 新增 `AuthUserSessionState` 实体
- 新增 `AuthSessionStatus` 枚举
- 新增 `AuthSessionRepository`
- 新增 `AuthUserSessionStateRepository`
- 新增 `AuthSessionService`
- 新增 `TokenBlacklistService`

### 2. JWT 扩展为会话感知令牌

已改造 `JwtTokenProvider`，新增或使用以下 claims / 能力：

- `jti`
- `sessionId`
- `sessionVersion`
- `userId` 改为显式字符串写入 claim
- 读取 token 过期时间
- 计算 token 剩余有效期

### 3. 登录 / 刷新 / 登出闭环

已改造 `AuthService`：

- 登录成功后创建 `sessionVersion`
- 为每次登录生成独立 `sessionId`
- 生成带会话信息的 `access token` / `refresh token`
- 将 refresh 会话状态写入数据库和 Redis
- `refreshToken()` 增加会话有效性校验
- 新增 `logout(accessToken, refreshToken)`
- 新增 `logoutAll(userId, accessToken)`

### 4. API 与安全链路

已改造 `AuthController`：

- `POST /api/auth/logout` 现在会真正撤销会话
- 新增 `POST /api/auth/logout-all`
- logout / logout-all 均会清理 cookie
- `refreshToken` cookie 过期时间改为读取配置，不再写死 7 天

已改造安全校验：

- `JwtAuthenticationFilter` 增加黑名单校验
- `JwtAuthenticationFilter` 增加 `sessionVersion` 校验
- `WebSocketAuthInterceptor` 增加黑名单与会话版本校验

### 5. 数据库迁移

新增 Flyway 脚本：

- `aps-api/src/main/resources/db/migration/V31__Create_Auth_Session.sql`
- `aps-api/src/main/resources/db/migration/V32__Create_Auth_User_Session_State.sql`

### 6. 排产任务化入口第一批

已完成“异步排产入口任务化”的最小闭环，但当前仍是单机内存求解驱动，尚未切到 RabbitMQ worker。

新增：

- `ScheduleSolverTask` 实体
- `SolverTaskStatus` 枚举
- `SolverTaskType` 枚举
- `TriggerSource` 枚举
- `ScheduleSolverTaskRepository`
- `V33__Create_Schedule_Solver_Task.sql`

已改造：

- `ScheduleService`
  - 新增 `submitSolveTask(scheduleId, triggeredBy, triggerSource)`
  - 求解开始时写入任务记录
  - 求解完成后回写 `SUCCESS`
  - 求解失败后回写 `FAILED`
  - 新增 `getLatestSolverTask(scheduleId)`
- `ScheduleController`
  - `POST /api/schedules/{id}/solve` 现在返回任务信息，不再只返回空成功
  - 新增 `GET /api/schedules/{id}/solver-tasks/latest`

### 7. 排产方案级锁第一批

已新增：

- `ScheduleLockService`

已改造：

- `ScheduleService.submitSolveTask()` 现在会先尝试获取 `scheduleId` 锁
- 获取不到锁时，直接拒绝重复提交
- 求解成功、失败、手动停止时都会释放锁

当前实现说明：

- 锁当前基于 Redis `setIfAbsent + TTL`
- key 形式：`lock:schedule:solve:{scheduleId}`
- 这是多实例防重的第一版，后续仍建议补锁续期和 worker 侧接管

### 8. RabbitMQ 排产任务链第一批

已完成从 API/Service 侧“提交任务”到 worker 侧“消费任务”的第一版拆分。

新增：

- `ScheduleSolveTaskMessage`
- `ScheduleTaskDispatcher`
- `RabbitMqScheduleTaskDispatcher`
- `ScheduleTaskRabbitConfig`
- `aps-mq-consumer` 启动类 `ApsMqConsumerApplication`
- `ScheduleTaskConsumer`

已改造：

- `ScheduleService.submitSolveTask()` 现在只负责：
  - 校验排产方案存在
  - 抢占 `scheduleId` 锁
  - 创建 `PENDING` 任务记录
  - 发送 MQ 消息
- `ScheduleService.executeSolveTask(taskId)` 负责：
  - 加载任务和排产方案
  - 将任务置为 `RUNNING`
  - 调用 `SolverManager`
  - 在完成/失败时回写任务状态并释放锁
- `aps-mq-consumer` 的 `ScheduleTaskConsumer` 消费 MQ 后调用 `scheduleService.executeSolveTask(taskId)`

当前实现说明：

- 队列名当前为 `schedule.task.plan`
- 当前使用的是最小可行版本，尚未引入 exchange / routing key 细分
- 当前还没有失败重试、死信队列、幂等消费记录
- 当前 worker 仍直接回调本地异步完成监听，后续可继续拆为更明确的 worker orchestration

### 9. MQ 幂等与重试骨架第一批

已新增：

- `MqConsumeRecord`
- `MqConsumeRecordRepository`
- `MqConsumeRecordService`
- `V34__Create_Mq_Consume_Record.sql`

已改造：

- `ScheduleTaskConsumer` 现在在消费前检查是否已消费
- 同一个 `taskId + consumerName` 重复投递时会直接跳过
- `ScheduleTaskRabbitConfig` 已升级为：
  - 主队列
  - 重试队列
  - DLQ
  - exchange + routing key

当前队列结构：

- 主 exchange: `schedule.task.exchange`
- 主队列: `schedule.task.plan`
- 重试队列: `schedule.task.plan.retry`
- 死信队列: `schedule.task.plan.dlq`

当前实现说明：

- 幂等当前基于数据库消费记录表
- 重试 / DLQ 当前只完成 RabbitMQ 拓扑骨架
- 还没有补“最大重试次数”和“失败消息转人工处理”的策略
- 还没有补 listener 侧的显式异常分类与 nack 策略

### 10. 跨进程排产通知到 WebSocket 第一批

已完成 worker -> RabbitMQ -> API -> WebSocket 的第一版通知桥接。

新增：

- `ScheduleNotificationMessage`
- `ScheduleNotificationDispatcher`
- `RabbitMqScheduleNotificationDispatcher`
- `ScheduleNotificationRabbitConfig`
- `ScheduleNotificationConsumer`

已改造：

- `ScheduleService.executeSolveTask()` 在开始、完成、失败、停止时都会发送排产通知消息
- `aps-api` 增加 AMQP 依赖
- API 实例通过 `ScheduleNotificationConsumer` 订阅通知 exchange
- API 收到通知后调用 `ScheduleProgressPublisher` 推送到 `/topic/schedule/{scheduleId}`

当前实现说明：

- 通知 exchange 使用 `fanout`
- API 侧使用 `AnonymousQueue`，每个实例都有自己的临时订阅队列
- 这意味着同一条排产通知可以被所有 API 实例接收，然后各自推送给本机 WebSocket 连接

当前已打通的通知类型：

- `STARTED`
- `COMPLETED`
- `FAILED`
- `STOPPED`

### 11. 失败任务补偿入口第一批

已完成最小可用的失败任务重试链路。

新增/改造：

- `ScheduleService.retryFailedTask(taskId)`
- `ScheduleController`
  - 新增 `POST /api/schedules/solver-tasks/{taskId}/retry`
- `ScheduleLockService`
  - 新增 `renewLock(scheduleId)` 基础方法

当前行为：

- 只有 `FAILED` 状态任务允许重试
- 重试时会重新创建一条新的 `PENDING` 任务记录
- 新任务会重新走 `scheduleId` 锁校验
- 重试后仍然复用现有 MQ 投递链路

当前说明：

- `renewLock(scheduleId)` 目前只是基础能力，尚未接入真正的周期续期调度
- 也就是说“锁续期机制”还没有完整完成，只是把接口和实现基础补上了

### 12. 排产锁续期第一批

已完成“长时间求解期间 Redis 锁续期”的最小闭环。

新增：

- `ScheduleLockRenewalService`
- `ScheduleAsyncConfig`
- `ScheduleLockRenewalServiceTest`

已改造：

- `ScheduleService.executeSolveTask()`
  - 求解开始后启动锁续期任务
  - 求解成功后停止锁续期任务
  - 求解失败后停止锁续期任务
- `ScheduleService`
  - 异步收尾逻辑改为走显式 `scheduleTaskExecutor`
  - 不再依赖默认公共线程池

当前行为：

- 锁续期任务按固定频率刷新 `lock:schedule:solve:{scheduleId}` 的 TTL
- 每个 `scheduleId` 同时只会存在一个续期任务
- 求解结束后会取消对应续期任务，避免空转

当前说明：

- 这次完成的是“worker 执行期间”的锁续期
- “任务在 MQ 中排队过久导致提交阶段锁过期”的问题仍未处理
- 锁值当前仍然是固定 `"1"`，还没有升级成带 owner token 的安全释放模型

### 13. 排产锁 owner token 第一批

已完成“排产锁按 owner token 加锁 / 解锁 / 续期”的第一版语义升级。

新增：

- `V35__Add_Lock_Owner_Token_To_Schedule_Solver_Task.sql`
- `ScheduleLockServiceTest`

已改造：

- `ScheduleSolverTask`
  - 新增 `lockOwnerToken`
- `ScheduleSolverTaskRepository`
  - 新增按 `scheduleId + status` 查询最近任务的方法
- `ScheduleLockService`
  - `tryLock(scheduleId, ownerToken)`
  - `unlock(scheduleId, ownerToken)`
  - `renewLock(scheduleId, ownerToken)`
- `ScheduleLockRenewalService`
  - 续期任务改为携带 `ownerToken`
- `ScheduleService`
  - 提交任务时生成 `ownerToken`
  - 任务记录持久化 `ownerToken`
  - 完成、失败、停止时按 `ownerToken` 解锁

当前行为：

- Redis 锁 value 不再是固定 `"1"`，而是每次任务独立 owner token
- 解锁和续期都带 owner token 校验
- 不再存在“其他实例误删当前锁”的直接路径

当前说明：

- 这次只完成了 owner token 化
- 还没有做“worker 消费时校验锁是否仍归自己所有”
- 也还没有做“锁丢失后的 fail-fast 或补偿策略”

### 14. worker 启动前锁归属校验第一批

已完成“worker 启动求解前校验锁仍归自己所有”的 fail-fast 保护。

已改造：

- `ScheduleLockService`
  - 新增 `isOwnedBy(scheduleId, ownerToken)`
- `ScheduleService.executeSolveTask()`
  - worker 启动前先校验锁归属
  - 若锁已失效或 owner token 不匹配，则直接终止执行
  - 会将任务标记为 `FAILED`
  - 会发送失败通知而不是继续调用求解器

当前行为：

- MQ 消费到任务后，不会盲目开始求解
- 如果锁已经过期、被覆盖或不属于当前任务，worker 会 fail-fast
- 这避免了“锁已丢失但仍继续排产”的错误路径

当前说明：

- 这次只做了启动前一次性校验
- 运行中的锁丢失仍然只会依赖下一次续期失败日志暴露
- 还没有做“锁丢失时主动终止求解”的更强保护

### 15. PENDING 任务锁保活第一批

已完成“提交到消费之间”的待执行任务锁保活第一版。

新增：

- `PendingTaskLockMaintenanceService`
- `PendingTaskLockMaintenanceScheduler`
- `PendingTaskLockMaintenanceServiceTest`

已改造：

- `ScheduleSolverTaskRepository`
  - 新增按 `PENDING` 状态批量查询方法

当前行为：

- 定时扫描最多 100 条 `PENDING` 任务
- 对仍带 `ownerToken` 的待执行任务续 Redis 锁
- 这样在 MQ 排队期间，锁不会因为 TTL 到期直接失效

当前说明：

- 当前只做了简单批量续期，没有加入分页游标或分片处理
- 也没有为“长时间积压的 PENDING 任务”增加自动失败策略
- 续期调度当前依赖启用 `@EnableScheduling` 的应用实例执行

### 16. PENDING 超时自动失败第一批

已完成“长时间积压的待执行任务自动失败”第一版。

已改造：

- `PendingTaskLockMaintenanceService`
  - 增加超时 `PENDING` 任务扫描
  - 超时任务自动标记为 `FAILED`
  - 自动释放对应 Redis 锁
  - 自动发送失败通知
- `ScheduleSolverTaskRepository`
  - 新增按 `status + createTimeBefore` 查询方法

当前行为：

- `PENDING` 超过 15 分钟会被自动判定为超时
- 超时后不会继续无限保活
- 任务状态会收敛为 `FAILED`

当前说明：

- 当前阈值先固定为 15 分钟
- 还没有把超时阈值配置化
- 也还没有做“按任务类型区分超时策略”

### 17. 运行中锁丢失主动终止第一批

已完成“运行中的求解任务在锁丢失后主动终止”的第一版。

已改造：

- `ScheduleLockRenewalService`
  - 续期失败时不再只打日志
  - 会停止当前续期任务
  - 会回调上层终止逻辑
- `ScheduleService`
  - 新增运行中锁丢失处理逻辑
  - 会调用 `solverManager.terminateEarly(scheduleId)`
  - 会将任务标记为 `FAILED`
  - 会发送失败通知

当前行为：

- 运行中的排产任务如果续期失败，不会继续占用求解器资源
- 锁丢失会收敛成明确失败状态，而不是仅靠日志暴露

当前说明：

- 当前按“续期失败即终止”处理，策略偏保守
- 还没有做“短暂 Redis 抖动下的多次重试再终止”
- 也没有做“终止原因细分”字段

### 18. 共享文件基础设施第一批

已完成“共享文件管理”的最小可用主链路，并先将物料导入错误文件迁移到新文件服务。

新增：

- `FileObject`
- `FileObjectStatus`
- `FileVisibility`
- `StorageProvider`
- `V36__Create_Sys_File_Object.sql`
- `FileObjectRepository`
- `FileStorageService`
- `FileObjectService`
- `StorageProperties`
- `MinioStorageConfig`
- `MinioFileStorageService`
- `InMemoryFileStorageService`
- `FileObjectServiceTest`

已改造：

- `MaterialService`
  - 导入错误文件不再写 `material_import_error_files`
  - 改为写 `sys_file_object + FileStorageService`
  - 下载错误文件改为走 `FileObjectService`
  - 定时清理改为按业务类型清理共享文件

当前行为：

- 已有统一文件元数据表 `sys_file_object`
- 已有对象存储抽象 `FileStorageService`
- 生产路径支持 MinIO
- 未配置 MinIO 时，使用内存存储作为开发/测试回退
- 物料导入错误文件已经走新文件服务，不再依赖 `BYTEA`

当前说明：

- 这次先只迁了“物料导入错误文件”
- 导出文件、模板文件、附件等还没迁
- `material_import_error_files` 旧表和旧实体还没删除，当前处于兼容过渡期

### 19. 物料导出文件迁移第一批

已完成“物料导出文件”接入共享文件服务的第一版。

已改造：

- `MaterialService`
  - 新增 `exportMaterialsToFile(format)`
  - 导出内容可直接落到共享文件服务
  - 新增导出文件下载能力
- `MaterialController`
  - 新增 `POST /api/materials/export-files`
  - 新增 `GET /api/materials/exports/{token}`

当前行为：

- 物料导出现在支持“生成共享文件 + token 下载”
- 原有直流式 `/api/materials/export` 仍保留，兼容旧前端
- 新接口已经满足多实例下“生成在 A、下载落到 B”仍可工作的要求

当前说明：

- 这次只迁了“物料导出”
- 审计导出、其他业务导出、模板文件还没迁
- 旧的直流式导出接口还在，后续可视前端改造情况决定是否下线

### 20. 审计导出文件迁移第一批

已完成“审计导出文件”接入共享文件服务的第一版。

新增/改造：

- `AuditService`
  - 新增 `exportAuditLogsToFile(startTime, endTime)`
  - 新增导出文件下载能力
- `AuditLogController`
  - 新增 `POST /api/audit-logs/export-files`
  - 新增 `GET /api/audit-logs/exports/{token}`
- `AuditServiceTest`
  - 新增共享文件导出测试
- `AuditLogControllerTest`
  - 新增导出创建与下载接口测试

当前行为：

- 审计导出现在支持“生成共享文件 + token 下载”
- 审计文件正文不再依赖当前 API 实例内存直出
- 多实例下可以做到“创建在 A、下载落到 B”仍然可用

当前说明：

- 这次先迁了“审计导出”
- 模板文件和其他业务导出还没迁
- 审计导出的旧直流式接口仍保留，后续可按前端切换节奏决定是否收口

### 21. 物料模板文件迁移第一批

已完成“物料模板文件”接入共享文件服务的第一版，并同步切换前端下载方式。

新增/改造：

- `MaterialService`
  - 新增 `exportTemplateToFile(format)`
  - 新增模板文件下载能力
  - 过期清理增加 `MATERIAL_TEMPLATE`
- `MaterialController`
  - 新增 `POST /api/materials/template-files`
  - 新增 `GET /api/materials/templates/{token}`
- `aps-web/src/api/material.ts`
  - 新增模板文件创建与下载 API
- `aps-web/src/views/Material.vue`
  - 模板导出按钮切换为“先创建共享文件，再按 token 下载”

当前行为：

- 物料模板现在支持“生成共享文件 + token 下载”
- 前端不再依赖单实例直流式响应完成模板下载
- 多实例下可以做到“模板创建在 A、下载落到 B”仍然可用

当前说明：

- 这次迁的是“物料模板”
- 旧的 `/api/materials/export?format=xlsx` 仍保留兼容
- 其他业务模板和剩余导出文件还没迁完

### 22. 审计导出前端切换第一批

已完成“审计导出”前端从直流式下载切到共享文件 token 下载的第一版。

新增/改造：

- `aps-web/src/api/audit.ts`
  - 新增审计导出文件创建接口
  - 新增审计导出文件下载接口
- `aps-web/src/views/AuditLog.vue`
  - 导出按钮改为“先创建共享文件，再按 token 下载”
  - 增加导出 loading 状态

当前行为：

- 审计日志页面不再依赖旧的 `/api/audit-logs/export` 直流式接口完成下载
- 前端已切换到共享文件主链路
- 审计导出在多实例下可稳定工作

当前说明：

- 后端旧直流式接口仍保留兼容
- 审计导出这条链路现在已经完成前后端双侧迁移

### 23. 前端旧下载 API 清理第一批

已完成“前端已无引用的旧直流式下载 API”清理，避免后续误走旧链路。

新增/改造：

- `aps-web/src/api/audit.ts`
  - 删除未再使用的 `exportAuditLogs()`
- `aps-web/src/api/material.ts`
  - 删除未再使用的 `exportFile()`

当前行为：

- 审计导出前端只能走共享文件 token 下载链路
- 物料模板前端只能走共享文件 token 下载链路
- 前端误回退到旧直流式下载接口的风险已降低

当前说明：

- 后端兼容接口仍保留
- 这次是前端 API 面的清尾，不涉及后端行为变化

### 24. 会话列表与单设备下线第一批

已完成“当前用户会话列表”和“撤销单设备会话”的第一版。

新增/改造：

- `AuthSessionService`
  - 新增活动会话列表查询
- `AuthService`
  - 新增 `listSessions(userId, accessToken)`
  - 新增 `revokeSession(userId, sessionId)`
  - 新增 `isCurrentSession(accessToken, sessionId)`
- `AuthController`
  - 新增 `GET /api/auth/sessions`
  - 新增 `DELETE /api/auth/sessions/{sessionId}`
  - 撤销当前会话时会清理认证 cookie

当前行为：

- 已登录用户可以查询自己当前所有活动会话
- 可以撤销指定会话
- 如果撤销的是当前会话，前端会被动退出登录状态

当前说明：

- 这次先做了最小闭环
- 还没有做前端“设备管理”页面
- 还没有补 `lastAccessAt` 更新链路和过期会话清理任务

### 25. 过期会话清理任务第一批

已完成“过期认证会话自动清理”的第一版。

新增/改造：

- `AuthSessionRepository`
  - 新增过期活动会话批量查询方法
- `AuthSessionService`
  - 新增 `cleanupExpiredSessions()`
  - 清理时会将过期 `ACTIVE` 会话标记为 `EXPIRED`
  - 同步删除 Redis 会话 key 与用户会话集合索引
- `AuthSessionCleanupScheduler`
  - 新增定时清理任务
- `AuthSessionServiceTest`
  - 新增过期会话清理测试

当前行为：

- 系统会定时扫描并清理已过期的活动会话
- 过期会话不会继续残留在 Redis 会话索引中
- 会话列表查询不会长期看到失效垃圾数据

当前说明：

- 这次先做了基础清理
- 还没有接分布式锁
- 还没有补 `lastAccessAt` 更新链路

### 26. 会话最近访问时间更新第一批

已完成“请求鉴权成功后刷新会话最近访问时间”的第一版。

新增/改造：

- `AuthSessionService`
  - 新增 `touchSession(sessionId)`
- `JwtAuthenticationFilter`
  - 访问令牌校验通过后会刷新当前会话的 `lastAccessAt`
- `AuthSessionServiceTest`
  - 新增最近访问时间更新测试
- `JwtAuthenticationFilterTest`
  - 新增鉴权成功后刷新会话时间测试

当前行为：

- 用户访问受保护接口时，会刷新当前会话的最近访问时间
- 会话列表中的 `lastAccessAt` 现在具备实际业务意义

当前说明：

- 这次先做了最小同步更新
- 还没有做节流更新策略
- 还没有做前端“设备管理”页面

### 27. 前端设备管理页面第一批

已完成“设备管理”前端页面的第一版，并接入用户菜单入口。

新增/改造：

- `aps-web/src/api/auth.ts`
  - 新增会话列表、撤销单设备、退出全部设备 API
- `aps-web/src/composables/useSessionDevices.ts`
  - 新增设备管理逻辑 composable
- `aps-web/src/views/SessionDevices.vue`
  - 新增设备管理页面
- `aps-web/src/router/index.ts`
  - 新增隐藏路由 `/session-devices`
- `aps-web/src/views/Layout.vue`
  - 菜单构建逻辑支持 `hideInMenu`
- `aps-web/src/components/UserMenu.vue`
  - 新增“设备管理”“退出全部设备”入口

当前行为：

- 用户可从右上角用户菜单进入设备管理页
- 可查看当前账号在线设备
- 可移除指定设备
- 可退出全部设备

当前说明：

- 这次先完成页面最小闭环
- 还没有做设备类型图标映射或更细的客户端识别
- 后端兼容接口仍然保留

### 28. 会话清理任务分布式锁第一批

已完成“过期会话清理任务”的分布式互斥第一版。

新增/改造：

- `ScheduledTaskLockService`
  - 新增通用定时任务 Redis 锁服务
- `AuthSessionCleanupScheduler`
  - 接入分布式锁后再执行清理
- `ScheduledTaskLockServiceTest`
  - 新增定时任务锁测试
- `AuthSessionCleanupSchedulerTest`
  - 新增清理任务互斥测试

当前行为：

- 多实例下只有拿到锁的节点会执行过期会话清理
- 未拿到锁的节点会跳过本轮执行
- 清理完成后会安全释放锁

当前说明：

- 这次先只给会话清理任务接了互斥
- 其他定时任务还没统一迁到同一套调度锁服务

### 29. 排产阶段进度通知第一批

已完成“排产运行中阶段进度消息”的第一版。

新增/改造：

- `ScheduleNotificationDispatcher`
  - 新增 `publishProgress(...)`
- `RabbitMqScheduleNotificationDispatcher`
  - 新增进度通知消息发送
- `ScheduleService`
  - 在构建模型、提交求解、保存结果三个阶段发送进度通知
  - 同步更新任务进度
- `ScheduleNotificationConsumer`
  - 新增 `PROGRESS` 通知消费
- `ScheduleServiceTest`
  - 补阶段进度通知断言
- `ScheduleNotificationConsumerTest`
  - 新增 `PROGRESS` 通知测试

当前行为：

- 排产不再只有开始和结束两个节点
- API/WebSocket 现在能收到更细粒度的阶段进度
- 任务表中的 `progress` 会随阶段推进更新

当前说明：

- 这次先做了阶段型进度，不是 Timefold 实时 score 流
- 后续还可以继续补更细粒度百分比与 score 推送

## 本轮测试

先写测试，再做实现。

新增测试：

- `aps-service/src/test/java/com/aps/service/AuthServiceTest.java`
- `aps-api/src/test/java/com/aps/api/controller/AuthControllerTest.java`

已通过的命令：

```bash
mvn test -pl aps-api,aps-service -am "-Dtest=AuthControllerTest,AuthServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service,aps-mq-consumer -am "-Dtest=ScheduleServiceTest,ScheduleTaskConsumerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service,aps-mq-consumer -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest,ScheduleTaskConsumerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service,aps-mq-consumer -am "-Dtest=ScheduleServiceTest,ScheduleTaskConsumerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn clean test -pl aps-api,aps-service -am "-Dtest=ScheduleNotificationConsumerTest,ScheduleProgressPublisherTest,SolveEventListenerTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=ScheduleLockRenewalServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest,ScheduleLockRenewalServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=ScheduleLockServiceTest,ScheduleLockRenewalServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleLockServiceTest,ScheduleLockRenewalServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=ScheduleLockServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleLockServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=PendingTaskLockMaintenanceServiceTest,ScheduleLockServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,PendingTaskLockMaintenanceServiceTest,ScheduleLockServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=PendingTaskLockMaintenanceServiceTest,ScheduleLockServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,PendingTaskLockMaintenanceServiceTest,ScheduleLockServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=ScheduleLockRenewalServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleLockRenewalServiceTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service -am "-Dtest=MaterialServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service,aps-api -am "-Dtest=FileObjectServiceTest,MaterialServiceTest,MaterialControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service,aps-api -am "-Dtest=MaterialServiceTest,MaterialControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service,aps-api -am "-Dtest=AuditServiceTest,AuditLogControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-service,aps-api -am "-Dtest=MaterialServiceTest,MaterialControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
npm run type-check
npm run type-check
mvn test -pl aps-api,aps-service -am "-Dtest=AuthControllerTest,AuthServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=AuthSessionServiceTest,AuthControllerTest,AuthServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=JwtAuthenticationFilterTest,AuthSessionServiceTest,AuthControllerTest,AuthServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
npm run type-check
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduledTaskLockServiceTest,AuthSessionCleanupSchedulerTest,JwtAuthenticationFilterTest,AuthSessionServiceTest,AuthControllerTest,AuthServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleServiceTest,ScheduleNotificationConsumerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

说明：

- 中途遇到一次 `aps-api` test class 增量产物异常，`clean test` 后恢复正常
- 最终当前这批目标测试已全部通过

## 本轮 review 结论

未发现阻塞性问题。

已在 review 阶段顺手修复：

- `refreshToken` cookie 过期时间此前仍写死为 7 天，现已改为读取 `jwt.refresh-expiration`
- logout 在未认证上下文下也会先清 cookie，避免前端残留脏状态

## 当前未做

以下内容仍未开始，后续必须继续推进：

### 1. 认证 / 会话后续

- 会话列表接口
- 单设备踢下线接口
- 过期 `auth_session` 清理任务
- Redis / 数据库一致性补充测试
- Redis 集成测试或 Testcontainers 测试

### 2. 文件共享

- `sys_file_object` 表
- `FileStorageService`
- MinIO / S3 接入
- 导入错误文件从数据库字节存储迁移到对象存储

### 3. 排产异步化

- `schedule_solver_task` 表
- RabbitMQ 排产任务投递
- solver worker 消费执行
- `scheduleId` 分布式锁
- 进度广播

说明：

- 当前已完成“任务记录入库 + 查询最近任务”
- 当前已完成“scheduleId Redis 锁第一版”
- 当前已完成“MQ dispatch + worker consume 第一版”
- 当前 `ScheduleService` 已不在提交入口直接调用求解器，而是由 worker 消费后执行
- 当前已完成“消费幂等 + 重试/DLQ 拓扑第一版”
- 当前已完成“跨进程开始/完成/失败/停止通知 -> WebSocket”
- 当前已完成“失败任务最小补偿入口”
- 当前已完成“worker 执行期锁续期第一版”
- 当前已完成“owner token 化的安全解锁第一版”
- 当前已完成“worker 启动前锁归属校验第一版”
- 当前已完成“提交到消费之间的锁保活第一版”
- 当前已完成“长时间积压任务治理第一版”
- 当前已完成“运行中锁丢失后的主动终止第一版”
- 当前已完成“共享文件基础设施第一批”
- 当前已完成“物料导出文件迁移第一版”
- 当前已完成“审计导出文件迁移第一版”
- 当前已完成“物料模板文件迁移第一版”
- 当前已完成“审计导出前端切换第一版”
- 当前已完成“前端旧下载 API 清理第一版”
- 当前已完成“会话列表与单设备下线第一版”
- 当前已完成“过期会话清理任务第一版”
- 当前已完成“会话最近访问时间更新第一版”
- 当前已完成“前端设备管理页面第一版”
- 当前已完成“会话清理任务分布式锁第一版”
- 当前已完成“排产阶段进度通知第一版”
- 当前已完成“MQ 重试次数治理与异常分级第一版”
- 当前已完成“MQ 重试次数与回退延迟配置化第一版”
- 当前已完成“排产任务详情查询第一版”
- 当前已完成“排产进度文案与 score 透传第一版”
- 当前已完成“排产任务历史列表第一版”
- 当前已完成“旧直流式导出接口清理第一版”
- 当前已完成“旧导入错误文件实体清理第一版”
- 当前已完成“续期失败重试策略第一版”
- 当前还未完成“更细粒度进度广播”

### 4. WebSocket 多实例广播

- 当前只做了 token 会话有效性校验
- 还没有接 RabbitMQ 跨实例广播
- 还没有做连接注册表和按 `scheduleId` 路由

## 下一步建议顺序

建议按下面顺序继续，避免改造交叉过多：

1. `schedule_solver_task + RabbitMQ` 异步排产入口
2. `scheduleId` 分布式锁
3. WebSocket 跨实例广播
4. `sys_file_object + MinIO` 文件共享
5. 会话列表 / 踢设备 / 清理任务

当前状态更新：

- 第 1 步已完成一半：任务入口和任务表已经落地
- 第 1 步现在已完成主要骨架
- 下一步应继续补：
  - 更细粒度的求解进度百分比 / 分数推送

## 30. MQ 重试次数治理与异常分级第一批

本轮目标：

- 把排产任务消费从“有 retry / DLQ 拓扑”推进到“可控地重试”
- 避免业务异常和资源不存在异常被无限重试

已完成：

- `ScheduleTaskConsumer` 增加 `Message` 入参，开始读取 AMQP `x-death`
- 增加最大重试次数控制，当前阈值为 `3`
- `BusinessException` 统一视为不可重试异常，直接投递到 DLQ
- 普通异常在未超阈值时抛 `AmqpRejectAndDontRequeueException`，交给 RabbitMQ 进入重试链路
- 超过最大重试次数后不再继续重试，直接投递到 DLQ
- 补齐 `ScheduleTaskConsumerTest`：
  - 正常消费
  - 重复消费跳过
  - 可重试异常进入重试链路
  - 业务异常直接进 DLQ
  - 超过最大重试次数直接进 DLQ

已通过测试：

```bash
mvn test -pl aps-mq-consumer,aps-service -am "-Dtest=ScheduleTaskConsumerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service,aps-mq-consumer -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest,ScheduleTaskConsumerTest,ScheduleNotificationConsumerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- 这批把“无限重试风险”压下来了，但还没到生产级闭环
- 下一步更值得继续补：
  - 失败任务人工补偿入口
  - 重试次数、回退策略配置化

## 31. MQ 重试次数与回退延迟配置化第一版

本轮目标：

- 去掉排产任务消费和 RabbitMQ 配置里的硬编码重试策略
- 让重试次数和回退延迟具备可运维调参能力

已完成：

- 新增 `ScheduleTaskProperties`
  - `app.schedule.task.max-retry-count`
  - `app.schedule.task.retry-delay-ms`
- `ScheduleTaskConsumer` 改为读取 `ScheduleTaskProperties.maxRetryCount`
- `ScheduleTaskRabbitConfig` 改为读取 `ScheduleTaskProperties.retryDelayMs`
- `application.yml` 增加默认值和环境变量覆盖：
  - `APP_SCHEDULE_TASK_MAX_RETRY_COUNT`
  - `APP_SCHEDULE_TASK_RETRY_DELAY_MS`
- 新增 `ScheduleTaskRabbitConfigTest`
- 更新 `ScheduleTaskConsumerTest`，覆盖配置化后的消费行为

已通过测试：

```bash
mvn test -pl aps-mq-consumer,aps-service -am "-Dtest=ScheduleTaskConsumerTest,ScheduleTaskRabbitConfigTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service,aps-mq-consumer -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest,ScheduleTaskConsumerTest,ScheduleNotificationConsumerTest,ScheduleTaskRabbitConfigTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- `MQ` 重试策略已经从“写死在代码里”推进到“可配置”
- 剩余更值得继续补的内容：
  - 失败任务人工补偿入口增强
  - 更细粒度的求解进度 / 分数推送
  - 续期失败后的重试与告警策略

## 32. 排产任务详情查询第一版

本轮目标：

- 把失败任务补偿入口从“只知道 taskId 就重试”推进到“可先查看失败详情再决定是否补偿”

已完成：

- `ScheduleService` 新增 `getSolverTask(taskId)`
- `ScheduleController` 新增 `GET /api/schedules/solver-tasks/{taskId}`
- `SolverTaskResponse` 扩展：
  - `errorMessage`
  - `startedAt`
  - `finishedAt`
- 现有返回任务信息的接口统一带出这些字段
- 补齐测试：
  - `ScheduleServiceTest`
  - `ScheduleControllerTest`

已通过测试：

```bash
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- 现在人工补偿前已经能查询失败原因和执行时间
- 下一步更值得继续补：
  - 失败任务补偿入口增强
  - 更细粒度的求解进度 / 分数推送

## 33. 排产进度文案与 score 透传第一版

本轮目标：

- 让 WebSocket `PROGRESS` 消息不只带百分比，还能带阶段文案和当前 score

已完成：

- `ScheduleProgressPublisher.publishProgress(...)` 扩展为同时发送：
  - `progress`
  - `message`
  - `currentScore`
- `ScheduleNotificationConsumer` 现在会把 RabbitMQ 通知中的：
  - `message`
  - `score`
  原样透传到 WebSocket
- `SolveEventListener` 兼容新签名，保持旧本地事件路径可编译
- 补齐测试：
  - `ScheduleProgressPublisherTest`
  - `ScheduleNotificationConsumerTest`

已通过测试：

```bash
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleProgressPublisherTest,ScheduleNotificationConsumerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- 现在前端已经能拿到“当前做到哪一步”和“当前 score”
- 剩余更值得继续补的内容：
  - 更细粒度的实时 score 推送
  - 失败任务补偿入口增强

## 34. 排产任务历史列表第一版

本轮目标：

- 把人工补偿入口从“只知道某个 taskId 才能看详情”推进到“先看最近任务历史，再选目标任务”

已完成：

- `ScheduleSolverTaskRepository` 新增：
  - `findTop10ByScheduleIdOrderByCreateTimeDesc(scheduleId)`
- `ScheduleService` 新增：
  - `listSolverTasks(scheduleId)`
- `ScheduleController` 新增：
  - `GET /api/schedules/{id}/solver-tasks`
- 返回值沿用现有 `SolverTaskResponse`，已包含：
  - `status`
  - `progress`
  - `score`
  - `errorMessage`
  - `startedAt`
  - `finishedAt`
- 补齐测试：
  - `ScheduleServiceTest`
  - `ScheduleControllerTest`

已通过测试：

```bash
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- 到这里人工补偿主链已经具备：
  - 最近任务列表
  - 单任务详情
  - 单任务重试
- 剩余更值得继续补的内容：
  - 更细粒度的实时 score 推送
  - 续期失败重试策略

## 35. 旧直流式导出接口与遗留实体清理第一版

本轮目标：

- 收掉前端已不再使用的旧直流式导出接口
- 移除已经脱离主链、未再被引用的旧导入错误文件实体与仓储

已完成：

- 删除旧接口：
  - `GET /api/materials/export`
  - `GET /api/audit-logs/export`
- 删除遗留类：
  - `MaterialImportErrorFile`
  - `MaterialImportErrorFileRepository`
- 同步收敛控制器测试，保留共享文件新链路测试：
  - `export-files`
  - `template-files`
  - `exports/{token}`
  - `import-errors/{token}`
- 修正受影响测试：
  - `MaterialControllerTest`
  - `SolveEventListenerTest`

已通过测试：

```bash
mvn test -pl aps-api,aps-service -am "-Dtest=MaterialControllerTest,AuditLogControllerTest,MaterialServiceTest,AuditServiceTest,SolveEventListenerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- 共享文件主线已经不再依赖旧的直流式导出接口
- 主任务剩余高优先级内容已经收敛到：
  - 更细粒度的实时 score 推送

## 36. 续期失败重试策略第一版

本轮目标：

- 避免 Redis / 网络瞬时异常导致排产锁第一次续期失败就立刻终止求解

已完成：

- 新增 `ScheduleLockProperties`
  - `app.schedule.lock.renew-failure-retry-count`
- `ScheduleLockRenewalService` 现在区分两类情况：
  - `owner token` 不匹配：立即停止并回调
  - 续期抛异常：累计失败次数，达到阈值后才停止并回调
- 续期成功后会清空失败计数，避免历史瞬时失败影响后续判断
- `application.yml` 增加默认配置：
  - `APP_SCHEDULE_LOCK_RENEW_FAILURE_RETRY_COUNT`
- 补齐测试：
  - 瞬时异常达到阈值前不触发回调
  - 连续异常达到阈值后停止并回调
  - 原有 owner token 不匹配路径保持立即失败

已通过测试：

```bash
mvn test -pl aps-service -am "-Dtest=ScheduleLockRenewalServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleServiceTest,ScheduleLockRenewalServiceTest,ScheduleControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- 排产锁续期从“单次异常即中断”推进到了“容忍瞬时故障后再判定中断”
- 主任务剩余高优先级内容已经主要收敛到：
  - 更细粒度的实时 score 推送

## 37. 更细粒度实时 score 推送第一版

本轮目标：

- 把排产进度从“阶段性文案 + 少量 score”推进到“求解过程中 best solution 变化就能推送当前 score”

已完成：

- `ScheduleService.executeSolveTask()` 不再使用 `solverManager.solve(...)`
- 改为使用 `solverManager.solveAndListen(scheduleId, planningModel, bestSolutionConsumer)`
- 新增 `handleIntermediateBestSolution(...)`
  - 在 Timefold 发现更优解时提取当前 `score`
  - 推进任务进度，当前按 `40 -> 50 -> 60 -> 70 -> 75` 收敛
  - 发布 `SolveProgressEvent`
  - 通过 `ScheduleNotificationDispatcher.publishProgress(...)` 广播到 RabbitMQ
- 现有 API 侧 `ScheduleNotificationConsumer -> ScheduleProgressPublisher -> WebSocket` 链路无需改协议即可接收中间 score
- 补齐测试：
  - `ScheduleServiceTest`
    - 新增“中间 best solution score 会推送”覆盖
    - 原有 `solve()` 相关 mock 收敛为 `solveAndListen()`

已通过测试：

```bash
mvn test -pl aps-service -am "-Dtest=ScheduleServiceTest" "-Dsurefire.failIfNoSpecifiedTests=false"
mvn test -pl aps-api,aps-service -am "-Dtest=ScheduleControllerTest,ScheduleServiceTest,ScheduleNotificationConsumerTest,ScheduleProgressPublisherTest,SolveEventListenerTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

review 结论：

- 本轮没有阻塞性问题
- 多实例排产链路现在已经支持“求解进行中实时透传当前更优分数”
- 主任务高优先级尾项已基本收口，后续更偏优化项而不是主链路缺口

## 38. 排产前端进度面板与动态订阅第一版

本轮目标：

- 让前端真正消费后端新增的 `message/currentScore`
- 去掉写死 `/topic/schedule/1` 的订阅方式
- 把排产页面 API 调用收敛回 `src/api`

已完成：

- `aps-web/src/api/schedule.ts`
  - 新增 `create()`
  - `solve()` 改为返回任务信息
  - 新增 `SolverTask` 类型
  - 新增 `getLatestSolverTask()`
- `aps-web/src/stores/schedule.ts`
  - 不再直接使用 `axios`
  - 新增：
    - `currentTaskId`
    - `currentTaskStatus`
    - `progressMessage`
    - `currentScore`
    - `isSolving`
  - `triggerSolve()` 改为走 `scheduleApi`
  - `updateProgress()` 改为接收结构化进度消息
- 新增 `aps-web/src/composables/useScheduleProgress.ts`
  - 统一管理 SockJS/STOMP 连接
  - 按当前 `scheduleId` 动态订阅
  - 复用现有 WebSocket 消息协议更新 store
- `aps-web/src/components/GanttChart.vue`
  - 删除写死的 `/topic/schedule/1` 订阅逻辑
  - 接入 `useScheduleProgress()`
  - 页面新增：
    - 当前状态
    - 当前 `scheduleId`
    - 阶段文案
    - 实时 `currentScore`
  - 求解按钮增加 `loading`

已通过测试：

```bash
npm run type-check
```

review 结论：

- 本轮没有阻塞性问题
- 后端新增的实时分数和阶段文案现在已经在前端真正落地，不再停留在协议层
- 主任务当前已经从“核心能力缺口”进入“剩余优化项整理”阶段

## 关键文件

### 本轮新增

- `aps-domain/src/main/java/com/aps/domain/entity/AuthSession.java`
- `aps-domain/src/main/java/com/aps/domain/entity/AuthUserSessionState.java`
- `aps-domain/src/main/java/com/aps/domain/enums/AuthSessionStatus.java`
- `aps-service/src/main/java/com/aps/service/AuthSessionService.java`
- `aps-service/src/main/java/com/aps/service/TokenBlacklistService.java`
- `aps-service/src/main/java/com/aps/service/repository/AuthSessionRepository.java`
- `aps-service/src/main/java/com/aps/service/repository/AuthUserSessionStateRepository.java`
- `aps-domain/src/main/java/com/aps/domain/entity/ScheduleSolverTask.java`
- `aps-domain/src/main/java/com/aps/domain/enums/SolverTaskStatus.java`
- `aps-domain/src/main/java/com/aps/domain/enums/SolverTaskType.java`
- `aps-domain/src/main/java/com/aps/domain/enums/TriggerSource.java`
- `aps-service/src/main/java/com/aps/service/repository/ScheduleSolverTaskRepository.java`
- `aps-service/src/main/java/com/aps/service/ScheduleLockService.java`
- `aps-service/src/main/java/com/aps/service/ScheduleLockRenewalService.java`
- `aps-service/src/main/java/com/aps/service/mq/ScheduleSolveTaskMessage.java`
- `aps-service/src/main/java/com/aps/service/mq/ScheduleTaskDispatcher.java`
- `aps-service/src/main/java/com/aps/service/mq/RabbitMqScheduleTaskDispatcher.java`
- `aps-service/src/main/java/com/aps/service/config/ScheduleTaskRabbitConfig.java`
- `aps-domain/src/main/java/com/aps/domain/entity/MqConsumeRecord.java`
- `aps-service/src/main/java/com/aps/service/MqConsumeRecordService.java`
- `aps-service/src/main/java/com/aps/service/repository/MqConsumeRecordRepository.java`
- `aps-service/src/main/java/com/aps/service/ScheduleLockService.java`
- `aps-service/src/main/java/com/aps/service/mq/ScheduleNotificationMessage.java`
- `aps-service/src/main/java/com/aps/service/mq/ScheduleNotificationDispatcher.java`
- `aps-service/src/main/java/com/aps/service/mq/RabbitMqScheduleNotificationDispatcher.java`
- `aps-service/src/main/java/com/aps/service/config/ScheduleNotificationRabbitConfig.java`
- `aps-api/src/main/resources/db/migration/V31__Create_Auth_Session.sql`
- `aps-api/src/main/resources/db/migration/V32__Create_Auth_User_Session_State.sql`
- `aps-api/src/main/resources/db/migration/V33__Create_Schedule_Solver_Task.sql`
- `aps-api/src/main/resources/db/migration/V34__Create_Mq_Consume_Record.sql`
- `aps-api/src/main/resources/db/migration/V35__Add_Lock_Owner_Token_To_Schedule_Solver_Task.sql`
- `aps-service/src/test/java/com/aps/service/AuthServiceTest.java`
- `aps-api/src/test/java/com/aps/api/controller/AuthControllerTest.java`
- `aps-service/src/test/java/com/aps/service/ScheduleServiceTest.java`
- `aps-service/src/test/java/com/aps/service/ScheduleLockServiceTest.java`
- `aps-service/src/test/java/com/aps/service/ScheduleLockRenewalServiceTest.java`
- `aps-api/src/test/java/com/aps/api/controller/ScheduleControllerTest.java`
- `aps-mq-consumer/src/main/java/com/aps/mq/ApsMqConsumerApplication.java`
- `aps-mq-consumer/src/main/java/com/aps/mq/consumer/ScheduleTaskConsumer.java`
- `aps-mq-consumer/src/test/java/com/aps/mq/consumer/ScheduleTaskConsumerTest.java`
- `aps-api/src/main/java/com/aps/api/websocket/ScheduleNotificationConsumer.java`
- `aps-api/src/test/java/com/aps/api/websocket/ScheduleNotificationConsumerTest.java`

### 本轮改造

- `aps-service/src/main/java/com/aps/service/AuthService.java`
- `aps-service/src/main/java/com/aps/service/security/JwtTokenProvider.java`
- `aps-api/src/main/java/com/aps/api/controller/AuthController.java`
- `aps-api/src/main/java/com/aps/api/security/JwtAuthenticationFilter.java`
- `aps-api/src/main/java/com/aps/api/security/WebSocketAuthInterceptor.java`
- `aps-service/src/main/java/com/aps/service/ScheduleService.java`
- `aps-service/src/main/java/com/aps/service/config/ScheduleAsyncConfig.java`
- `aps-api/src/main/java/com/aps/api/controller/ScheduleController.java`

## 执行规则

后续继续按以下规则推进：

- 先写测试，再写实现
- 每做完一批跑针对性测试
- 每批完成后做一次 review
- 每批完成后同步更新本文件，避免遗漏
