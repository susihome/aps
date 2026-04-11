# APS 排产调度系统

中小制造业高级计划排程系统，基于 Timefold Solver 的智能排产平台。

## 快速开始

### 启动依赖服务

```bash
docker-compose up -d
```

### 构建项目

```bash
./mvnw clean install
```

### 运行应用

```bash
./mvnw spring-boot:run -pl aps-api
```

## 技术栈

- Spring Boot 3.4 + JDK 21
- Timefold Solver 1.15
- PostgreSQL 18
- Redis 7
- RabbitMQ 3

详见 [CLAUDE.md](CLAUDE.md)
