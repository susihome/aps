# 物料-模具绑定关系 - 实施进度

**日期**: 2026-04-12
**分支**: master

## 已完成的工作

### 1. 核心实体与数据库
- `V26__Material_Mold_Bindings.sql` - 关系表迁移脚本
- `MaterialMoldBinding.java` - 关联实体（priority, cycleTime, setupTime, changeoverTime, isDefault, isPreferred, enabled, validFrom, validTo）
- `Mold.java` - 扩展了注塑属性（requiredTonnage, maxShotWeight, cavityCount, status, maintenanceState）
- `Material.java` - 扩展了模具绑定关系
- `AuditAction.java` - 新增关系维护审计动作

### 2. 服务层
- `MaterialMoldBindingRepository.java` - JPA仓储
- `MaterialMoldBindingService.java` - 业务服务（CRUD + 关系查询）
- `MaterialService.java` - 更新（关联关系支持）
- `MoldService.java` - 更新（关联关系支持）

### 3. API层
- `MaterialMoldBindingDto.java` - 请求/响应DTO
- `MaterialMoldBindingController.java` - REST API
- `MoldController.java` / `MoldDto.java` - 更新模具属性

### 4. 求解器集成
- `OperationPlanningFact.java` - 新增排程事实字段（requiredMaterialId, lockedMoldId, eligibleResources, candidateMolds, preferredMold, preferredMoldPriority, preferredChangeoverTimeMinutes）
- `AssignmentPlanningModel.java` - 新增规划实体字段（eligibleResources, candidateMolds, preferredMold等）
- `ScheduleModelConverter.java` - 实现物料→模具→设备候选资格推导逻辑
- `ApsConstraintProvider.java` - 新增三个约束：
  - `resourceEligibility` (硬约束) - 设备必须满足物料-模具路径推导的可行资格
  - `preferPreferredMold` (软约束) - 优先使用默认/高优先级模具
  - `minimizeChangeover` (软约束) - 同机台相邻作业减少换模
- `AssignmentDifficultyComparator.java` - 更新难度比较逻辑

### 5. 测试
- `ApsConstraintProviderTest.java` - 4个约束测试（已修复，全部通过）
- `MaterialMoldBindingServiceTest.java` - 服务层测试
- `MaterialMoldBindingControllerTest.java` - API层测试
- `MaterialServiceTest.java` / `MoldServiceTest.java` - 更新

## 已修复的问题

### Solver测试修复
- **问题**: `ConstraintVerifier.given()` 不提供值范围导致 planning variable 未初始化（-1init分数）
- **修复**: 改用 `givenSolution()` 提供完整的 `SchedulePlanningModel`
- **问题**: `minimizeChangeover` 约束使用 `Joiners.lessThan(assignmentId)` 依赖UUID随机大小关系，导致join结果不确定
- **修复**: 改为 `Joiners.filtering()` 基于时间相邻关系过滤（`previous.endTime == current.startTime`）

### ScheduleTimeParameterServiceTest 修复
- **问题**: `create_DuplicateResource_ShouldThrow` 测试中不必要的stub导致 `UnnecessaryStubbingException`
- **修复**: 移除了 `when(resourceRepository.findById(resourceId))` 这行不必要的stub

## 当前问题 - aps-api 模块 22个测试错误

需要检查 `aps-api/target/surefire-reports/` 下的错误详情。

可能原因：新增的 `MaterialMoldBindingControllerTest` 或 `MoldControllerTest` 中的测试有依赖问题。

### 需要做的事情

1. **[紧急]** 修复 aps-api 模块的 22 个测试错误
2. **[重要]** 运行全量测试确认 `mvn test` 全部通过
3. **[建议]** 考虑清理 `.claude/worktrees/` 下的大量残留目录
4. **[建议]** 代码审查：检查 `ScheduleModelConverter` 中 `lockedMold` 的逻辑是否正确（当前当有 lockedMold 时只保留 lockedMold 对应的 binding，这排除了其他可行模具）
5. **[建议]** 二期规划：将模具升级为真正的受限排产资源（Assignment 显式记录 selectedMold，增加模具占用冲突约束）

## 关键文件清单

| 文件 | 说明 |
|------|------|
| `aps-api/src/main/resources/db/migration/V26__Material_Mold_Bindings.sql` | 关系表迁移 |
| `aps-domain/src/main/java/com/aps/domain/entity/MaterialMoldBinding.java` | 关联实体 |
| `aps-domain/src/main/java/com/aps/domain/entity/Mold.java` | 模具实体（含扩展属性） |
| `aps-domain/src/main/java/com/aps/domain/entity/Material.java` | 物料实体 |
| `aps-solver/src/main/java/com/aps/solver/ApsConstraintProvider.java` | 约束规则 |
| `aps-solver/src/main/java/com/aps/solver/model/ScheduleModelConverter.java` | 模型转换器 |
| `aps-solver/src/main/java/com/aps/solver/model/AssignmentPlanningModel.java` | 规划实体 |
| `aps-solver/src/main/java/com/aps/solver/model/OperationPlanningFact.java` | 工序事实 |
| `aps-service/src/main/java/com/aps/service/MaterialMoldBindingService.java` | 关系服务 |
| `aps-api/src/main/java/com/aps/api/controller/MaterialMoldBindingController.java` | 关系API |