# APS 排产调度系统

中小制造业高级计划排程系统，基于 Timefold Solver 的智能排产平台。

## 开发说明

仓库内的代理协作规范、代码约束和模块约定统一以 [AGENTS.md](AGENTS.md) 为准。

### 日常开发速查

- **新功能**：先做方案，再实现；默认按 TDD 推进，完成后做一次 code review
- **修 Bug**：优先按“失败测试 -> 修复 -> 验证”的顺序处理
- **Java / Spring Boot**：默认遵循 `spring-boot-engineer` + `springboot-tdd`；涉及安全时补充 `springboot-security`
- **Vue 3**：默认遵循 `vue-best-practices`；路由权限相关改动补充 `vue-router-best-practices`
- **代码清理**：清理死代码、重复代码、文件结构时按 `clean-code` 方式处理
- **接口联动**：后端字段、枚举、接口变更时必须同步更新前端类型和 API 封装
- **提交前最小验证**：后端至少完成编译/测试，前端至少完成 `npm run type-check` 和构建验证
- **更多详细规则**：查看 [AGENTS.md](AGENTS.md) 中的 `Recommended Skills` 和 `Additional Project Rules`

## 快速开始

### 启动依赖服务

```bash
docker-compose up -d
```

### 构建项目

```bash
./mvnw clean install
```

### 运行后端

```bash
./mvnw spring-boot:run -pl aps-api
```

### 运行前端

```bash
cd aps-web
npm install
npm run dev
```

## 技术栈

- Spring Boot 3.4 + JDK 21
- Timefold Solver 1.15
- PostgreSQL 18
- Redis 7
- RabbitMQ 3
- Vue 3 + TypeScript
