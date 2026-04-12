# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

中小制造业APS（高级计划排程）系统 - 基于约束求解的智能排产调度平台。

**核心能力：**
- 多目标优化排产（交期、产能、成本平衡）
- 实时动态调整（响应MES报工、设备异常）
- 可视化交互式甘特图（拖拽调整、约束校验）

## Technology Stack

### Backend
- **Spring Boot 3.4+ / JDK 21** - 虚拟线程处理高并发
- **Timefold Solver** - 约束求解引擎（核心）
- **PostgreSQL 18+** - 主数据存储（工单、工艺、资源、排产结果）
- **Redis 7+** - 缓存 + Pub/Sub
- **RabbitMQ 3.x** - MES事件异步处理
- **JPA (Hibernate 6.x)** - ORM层

### Frontend
- **Vue 3 + TypeScript** - 前端框架
- **frappe-gantt** - 甘特图核心组件
- **Element Plus / Ant Design Vue** - UI组件库
- **Pinia** - 状态管理

## Architecture

### Core Modules

```
aps-system/
├── aps-domain/          # 领域模型（工单、工艺、资源、排产方案）
├── aps-solver/          # Timefold求解引擎封装
├── aps-service/         # 业务服务层
├── aps-api/             # REST API + WebSocket
├── aps-mq-consumer/     # RabbitMQ消费者（MES事件）
└── aps-web/             # Vue前端
```

### Domain Model Hierarchy

**核心实体关系：**
- `Order` (工单) → `Process` (工艺路线) → `Operation` (工序)
- `Resource` (资源) → `Equipment` (设备) / `WorkCenter` (设备)
- `Schedule` (排产方案) → `Assignment` (作业分配)

**Timefold Planning Entities：**
- `@PlanningEntity`: `Assignment` (可移动的作业)
- `@PlanningVariable`: `startTime`, `assignedResource`
- `@PlanningScore`: 硬约束 + 软约束评分

### Constraint Rules

**硬约束（Hard Constraints）：**
- 设备能力匹配（工序只能分配到合格设备）
- 工艺路线顺序（前置工序完成后才能开始后续工序）
- 资源时间窗口（设备班次、维护时间）
- 物料齐套性（BOM物料必须可用）

**软约束（Soft Constraints）：**
- 最小化交期延迟（优先级加权）
- 最大化设备利用率
- 最小化换模次数（同类产品连续生产）
- 负载均衡（避免设备过载）

## Development Commands

### Backend

```bash
# 构建项目
./mvnw clean install

# 运行Spring Boot应用（启用虚拟线程）
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="--enable-preview"

# 运行单个测试
./mvnw test -Dtest=ScheduleSolverTest

# 运行Timefold求解器测试
./mvnw test -Dtest=*SolverTest

# 生成JPA实体DDL
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=create"
```

### Frontend

```bash
cd aps-web

# 安装依赖
npm install

# 开发模式（热重载）
npm run dev

# 构建生产版本
npm run build

# 类型检查
npm run type-check

# 运行单元测试
npm run test:unit
```

### Database

```bash
# 启动PostgreSQL（Docker）
docker run -d --name aps-postgres \
  -e POSTGRES_DB=aps \
  -e POSTGRES_USER=aps_user \
  -e POSTGRES_PASSWORD=aps_pass \
  -p 5432:5432 postgres:18

# 连接数据库
psql -h localhost -U aps_user -d aps

# 执行迁移脚本
./mvnw flyway:migrate
```

### Redis & RabbitMQ

```bash
# 启动Redis
docker run -d --name aps-redis -p 6379:6379 redis:7

# 启动RabbitMQ
docker run -d --name aps-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management

# RabbitMQ管理界面
# http://localhost:15672 (guest/guest)
```

### Docker Compose（推荐）

```bash
# 启动所有依赖服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## Timefold Solver Configuration

### Solver Config Location
`src/main/resources/solverConfig.xml`

### Key Parameters

```xml
<solver>
  <solutionClass>com.aps.domain.Schedule</solutionClass>
  <entityClass>com.aps.domain.Assignment</entityClass>
  
  <!-- 求解时间限制 -->
  <termination>
    <secondsSpentLimit>30</secondsSpentLimit>
  </termination>
  
  <!-- 构造启发式：快速生成初始解 -->
  <constructionHeuristic>
    <constructionHeuristicType>FIRST_FIT_DECREASING</constructionHeuristicType>
  </constructionHeuristic>
  
  <!-- 局部搜索：优化解 -->
  <localSearch>
    <acceptor>
      <entityTabuSize>7</entityTabuSize>
    </acceptor>
    <forager>
      <acceptedCountLimit>1000</acceptedCountLimit>
    </forager>
  </localSearch>
</solver>
```

### Incremental Solving

对于动态调整场景（MES报工、设备故障），使用增量求解：

```java
// 锁定已开始的作业
@PlanningPin
private boolean pinned;

// 仅重排未开始的作业
solver.solve(schedule); // 自动跳过pinned=true的作业
```

## WebSocket Real-time Push

### Endpoint
`ws://localhost:8080/ws/schedule-progress`

### Message Format

```json
{
  "type": "PROGRESS",
  "scheduleId": "SCH-20260402-001",
  "progress": 65,
  "currentScore": "-150hard/-2300soft",
  "timestamp": "2026-04-02T01:15:30Z"
}
```

```json
{
  "type": "CONFLICT",
  "conflictType": "RESOURCE_OVERLOAD",
  "resourceId": "EQ-001",
  "affectedAssignments": ["ASG-123", "ASG-456"],
  "message": "设备EQ-001在时间段 10:00-12:00 存在冲突"
}
```

## MES Integration

### RabbitMQ Queue
- `mes.workorder.report` - 工序报工事件
- `mes.equipment.fault` - 设备故障事件
- `mes.material.shortage` - 物料短缺事件

### Event Schema

```json
{
  "eventType": "OPERATION_COMPLETED",
  "orderId": "WO-20260402-001",
  "operationId": "OP-001",
  "equipmentId": "EQ-001",
  "completedTime": "2026-04-02T10:30:00Z",
  "actualDuration": 120,
  "qualityStatus": "PASS"
}
```

### Auto Re-schedule Trigger

当接收到以下事件时，自动触发重排产：
- 工序实际完工时间与计划偏差 > 30分钟
- 设备故障预计停机 > 2小时
- 紧急插单（优先级 = URGENT）

## Frontend Gantt Chart

### Data Structure

```typescript
interface GanttTask {
  id: string;
  name: string;
  start: Date;
  end: Date;
  progress: number;
  dependencies: string[];
  custom_class: string; // 'conflict' | 'locked' | 'normal'
  resourceId: string;
}
```

### Drag & Drop Validation

拖拽调整后，前端需校验：
1. 是否违反工艺路线顺序
2. 是否超出设备可用时间窗口
3. 是否与其他作业时间冲突

校验失败则回滚拖拽，显示错误提示。

## Performance Targets

- **求解速度**: 100工单 × 5工序 × 10设备 < 30秒
- **WebSocket延迟**: < 500ms
- **甘特图渲染**: 1000+作业无卡顿（虚拟滚动）
- **并发用户**: 支持50+用户同时查看

## Testing Strategy

### Unit Tests
- 约束规则测试（每个硬/软约束独立测试）
- 领域模型测试（工单、工艺、资源）
- 服务层测试（Mock Repository）

### Integration Tests
- Timefold求解器集成测试（小规模数据集）
- PostgreSQL数据持久化测试
- RabbitMQ消息消费测试

### E2E Tests
- 完整排产流程（创建工单 → 求解 → 查看甘特图）
- 动态调整流程（MES事件 → 重排产 → WebSocket推送）

## Common Pitfalls

### Timefold Solver
- **避免在约束规则中执行I/O操作**（会严重拖慢求解速度）
- **使用Incremental Score Calculation**（大规模问题必须）
- **合理设置终止条件**（时间限制 vs 分数阈值）

### Virtual Threads
- **不要在虚拟线程中使用synchronized**（会固定到平台线程）
- **使用ReentrantLock替代synchronized**
- **数据库连接池需配置为支持虚拟线程**

### WebSocket
- **心跳机制**（每30秒ping/pong，避免连接超时）
- **断线重连**（前端需实现指数退避重连）
- **消息幂等性**（重复消息需去重）

## Security

- **认证**: Spring Security + JWT
- **角色权限**:
  - `PLANNER`: 创建/修改排产方案
  - `SUPERVISOR`: 查看+手动调整
  - `ADMIN`: 系统配置
- **操作审计**: 记录所有排产变更（AuditLog表）

## Monitoring

- **Prometheus Metrics**:
  - `aps_solver_duration_seconds` - 求解耗时
  - `aps_schedule_score` - 排产方案分数
  - `aps_websocket_connections` - WebSocket连接数
  
- **Grafana Dashboard**: `http://localhost:3000`

## Development Phases

**Phase 1**: 基础数据模型 + 单设备单工序排产  
**Phase 2**: 多工序工艺路线 + 约束规则引擎  
**Phase 3**: 实时事件处理 + 动态重排产  
**Phase 4**: 前端甘特图交互 + WebSocket推送  
**Phase 5**: 高级优化（换模、物料齐套、并行排产）

## Key Files

- `ScheduleSolver.java` - Timefold求解器封装
- `ConstraintProvider.java` - 约束规则定义
- `ScheduleService.java` - 排产业务逻辑
- `MesEventConsumer.java` - MES事件消费者
- `ScheduleWebSocketHandler.java` - WebSocket推送
- `GanttChart.vue` - 甘特图组件
- `application.yml` - Spring Boot配置
- `solverConfig.xml` - Timefold求解器配置

## Spring Boot Engineer Quick Reference

### MUST DO

| 规则 | 正确写法 |
|------|---------|
| 构造器注入 | `public MyService(Dep dep) { this.dep = dep; }` |
| 验证 API 输入 | `@Valid @RequestBody MyRequest req`（每个写操作端点） |
| 类型安全配置绑定 | `@ConfigurationProperties(prefix = "app")` 绑定到 record/class |
| 正确组件注解 | `@Service` 业务逻辑，`@Repository` 数据层，`@RestController` HTTP层 |
| 事务范围 | 多步写操作加 `@Transactional`；只读查询加 `@Transactional(readOnly = true)` |
| 隐藏内部异常 | 在 `@RestControllerAdvice` 中捕获领域异常，返回 problem details，不暴露堆栈 |
| 外部化密钥 | 使用环境变量或 Spring Cloud Config，禁止放入 `application.properties` |

### MUST NOT DO

- 使用字段注入（`@Autowired` 加在字段上）
- 跳过 API 端点的输入验证
- 在应使用 `@Service`/`@Repository`/`@Controller` 时用 `@Component`
- 在响应式链中混用阻塞代码（如 WebFlux 链中调用 `.block()`）
- 在配置文件中存储 Secret 或凭证
- 硬编码 URL、凭证或环境相关值
- 使用 Spring Boot 2.x 废弃模式（如 `WebSecurityConfigurerAdapter`）

### Minimal Working Structure（最小完整示例）

```java
// Entity
@Entity @Table(name = "...")
public class MyEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // fields...
}

// Service（构造器注入）
@Service
public class MyService {
    private final MyRepository repo;
    public MyService(MyRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<MyEntity> findAll() { return repo.findAll(); }

    @Transactional
    public MyEntity create(MyRequest req) { ... }
}

// Controller
@RestController @RequestMapping("/api/v1/...") @Validated
public class MyController {
    private final MyService service;
    public MyController(MyService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MyResponse create(@Valid @RequestBody MyRequest req) {
        return service.create(req);
    }
}
```

## Vue 3 Best Practices Quick Reference

### 组件拆分触发条件（满足任意一条即需拆分）

1. 组件同时承担数据编排/状态管理 + 多个独立 UI 区块
2. 模板中有 3+ 个独立 UI 区块（表单、筛选、列表、操作栏等）
3. 某个模板块被重复使用或可复用

### 路由视图组件（View）规则

- 保持 View 组件轻量：只做 app shell/layout、provider wiring 和 feature composition
- CRUD/列表功能至少拆分为：容器组件、表单组件、列表/项目组件、操作栏/状态组件

### Composable 规则

- 逻辑被复用、有状态、或有副作用时提取为 composable
- composable API 保持小巧、有类型、可预期
- 职责：feature logic 放 composable，UI 展示放 component

### 自查 Checklist（提交前）

- [ ] 核心功能已实现且符合需求
- [ ] 使用了 Composition API + `<script setup lang="ts">`
- [ ] reactivity 模型最小化（`ref`/`reactive` + `computed` 派生）
- [ ] SFC 结构和模板规则已遵循
- [ ] 组件职责单一，已按规则拆分
- [ ] 数据流契约显式声明（`defineProps`、`defineEmits` 带类型）
- [ ] 复用/复杂逻辑已提取为 composable
- [ ] 性能优化在功能完成后才做
