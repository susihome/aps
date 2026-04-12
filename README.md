# APS 排产调度系统

中小制造业高级计划排程系统，基于 Timefold Solver 的智能排产平台。

## 开发说明

仓库内的代理协作规范、代码约束和模块约定统一以 [AGENTS.md](AGENTS.md) 为准。

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
