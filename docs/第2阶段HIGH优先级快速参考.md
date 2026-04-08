# APS 系统优化 - 第 2 阶段 (HIGH 优先级) 快速参考

## 🎯 第 2 阶段目标

完成 **5 个 HIGH 优先级优化**，预计工作量 **20 小时**。

---

## 📋 HIGH 优先级优化清单

### 1. WebSocket 重连逻辑
**文件**: `aps-web/src/components/GanttChart.vue:65-94`

**问题**: WebSocket 连接丢失无法自动重连

**修复方案**:
```typescript
// 添加重连机制
const reconnectAttempts = ref(0);
const maxReconnectAttempts = 5;
const reconnectDelay = ref(1000);

function connectWebSocket() {
  try {
    ws.value = new WebSocket(wsUrl);
    ws.value.onopen = () => {
      reconnectAttempts.value = 0;
      reconnectDelay.value = 1000;
      ElMessage.success('WebSocket 已连接');
    };
    ws.value.onerror = () => {
      if (reconnectAttempts.value < maxReconnectAttempts) {
        reconnectAttempts.value++;
        const delay = Math.min(reconnectDelay.value * 2, 30000);
        setTimeout(connectWebSocket, delay);
        reconnectDelay.value = delay;
      }
    };
  } catch (error) {
    log.error('WebSocket 连接失败', error);
  }
}
```

**预期收益**: 连接丢失自动恢复，提升可用性至 99.9%

---

### 2. 类型安全修复
**文件**: `aps-web/src/stores/schedule.ts:7`

**问题**: `currentScheduleId` 类型错误（number vs UUID string）

**修复方案**:
```typescript
// ❌ 修复前
currentScheduleId: number | null = null

// ✅ 修复后
currentScheduleId: string | null = null  // UUID 格式
```

**预期收益**: 消除类型错误，提升代码安全性

---

### 3. 异常处理改进
**文件**: 多个文件
- `AuditAspect.java:61`
- `JwtAuthenticationFilter.java:62`
- `AuditService.java:42`

**问题**: 过于宽泛的异常捕获 `catch (Exception e)`

**修复方案**:
```java
// ❌ 修复前
catch (Exception e) {
    log.error("Error", e);
}

// ✅ 修复后
catch (IOException e) {
    log.error("IO 错误", e);
} catch (JsonProcessingException e) {
    log.error("JSON 处理错误", e);
} catch (Exception e) {
    log.error("未预期的错误", e);
}
```

**预期收益**: 更好的错误诊断，便于调试

---

### 4. DTO 验证
**文件**: `OrderController.java:25` 及其他控制器

**问题**: `@RequestBody` 缺少 `@Valid` 注解

**修复方案**:
```java
// ❌ 修复前
@PostMapping
public AjaxResult<Order> createOrder(@RequestBody Order order) { ... }

// ✅ 修复后
@PostMapping
public AjaxResult<Order> createOrder(@Valid @RequestBody Order order) { ... }
```

**预期收益**: 防止无效数据持久化

---

### 5. 权限查询性能优化
**文件**: `PermissionService.java:114-123`

**问题**: `isAncestor()` 方法 O(n) 递归遍历

**修复方案**:
```java
// ✅ 修复后 - 使用数据库查询
@Query(value = """
    WITH RECURSIVE ancestors AS (
        SELECT id, parent_id FROM permissions WHERE id = :childId
        UNION ALL
        SELECT p.id, p.parent_id FROM permissions p
        INNER JOIN ancestors a ON p.id = a.parent_id
    )
    SELECT COUNT(*) > 0 FROM ancestors WHERE id = :ancestorId
    """, nativeQuery = true)
boolean isAncestor(@Param("childId") UUID childId, @Param("ancestorId") UUID ancestorId);
```

**预期收益**: 权限查询性能提升 10-100x

---

## 📊 优化优先级

| # | 优化项 | 难度 | 工作量 | 优先级 |
|---|--------|------|--------|--------|
| 1 | WebSocket 重连 | 中 | 6h | 🔴 高 |
| 2 | 类型安全修复 | 低 | 4h | 🔴 高 |
| 3 | 异常处理改进 | 中 | 6h | 🔴 高 |
| 4 | DTO 验证 | 低 | 4h | 🔴 高 |
| 5 | 权限查询优化 | 高 | 6h | 🔴 高 |

---

## 🚀 实施步骤

### 第 1 天 - 类型安全 + DTO 验证 (8 小时)
1. 修复 Store 中的类型定义
2. 添加 @Valid 到所有控制器
3. 编译验证

### 第 2 天 - 异常处理 (6 小时)
1. 改进 AuditAspect 异常处理
2. 改进 JwtAuthenticationFilter 异常处理
3. 改进 AuditService 异常处理
4. 编译验证

### 第 3 天 - WebSocket 重连 (6 小时)
1. 实现重连机制
2. 添加连接状态管理
3. 测试验证

### 第 4 天 - 权限查询优化 (6 小时)
1. 添加递归 CTE 查询
2. 更新 PermissionService
3. 编译验证

---

## ✅ 验收标准

- [ ] 所有 5 个优化完成
- [ ] 编译通过 (BUILD SUCCESS)
- [ ] 无新增警告
- [ ] 功能测试通过
- [ ] 性能测试通过

---

## 📈 预期收益

| 方面 | 改进 |
|------|------|
| 可用性 | 99.9% (WebSocket 重连) |
| 类型安全 | 100% (类型修复) |
| 错误诊断 | 提升 50% (异常处理) |
| 数据完整性 | 100% (DTO 验证) |
| 查询性能 | 10-100x (权限优化) |

---

## 📚 相关文档

- `APS系统全面代码审查与优化计划.md` - 完整计划
- `CRITICAL优化完成总结.md` - 第 1 阶段总结
- `系统审查优化完整总结.md` - 审查总结

---

## 💡 建议

1. **按优先级顺序实施**: 类型安全 → DTO 验证 → 异常处理 → WebSocket → 权限优化
2. **每个优化后编译验证**: 确保无编译错误
3. **保持代码风格一致**: 参考现有代码规范
4. **添加详细注释**: 说明优化的原因和方法
5. **记录性能改进**: 对比优化前后的性能指标

---

## 🎯 下一步

准备好开始第 2 阶段优化了吗？

**建议**: 从类型安全修复开始，这是最简单的优化，可以快速获得成就感！
