<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h1>欢迎，{{ username }}！</h1>
      <p class="welcome-text">您的角色：{{ userRole }}</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover" role="region" aria-label="工单统计">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF" aria-hidden="true"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value" aria-label="工单总数">--</div>
              <div class="stat-label">工单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover" role="region" aria-label="排产计划统计">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A" aria-hidden="true"><Calendar /></el-icon>
            <div class="stat-info">
              <div class="stat-value" aria-label="排产计划数">--</div>
              <div class="stat-label">排产计划</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover" role="region" aria-label="设备资源统计">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C" aria-hidden="true"><Setting /></el-icon>
            <div class="stat-info">
              <div class="stat-value" aria-label="设备资源数">--</div>
              <div class="stat-label">设备资源</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>快速开始</span>
      </template>
      <div class="quick-actions">
        <el-button
          type="primary"
          size="large"
          @click="goToOrders"
          v-if="canAccessOrders"
          aria-label="进入工单管理"
        >
          <el-icon><Document /></el-icon>
          <span>工单管理</span>
        </el-button>
        <el-button
          type="success"
          size="large"
          @click="goToSchedule"
          v-if="canAccessSchedule"
          aria-label="进入排产计划"
        >
          <el-icon><Calendar /></el-icon>
          <span>排产计划</span>
        </el-button>
        <el-button
          type="warning"
          size="large"
          @click="goToUsers"
          v-if="canAccessUsers"
          aria-label="进入用户管理"
        >
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Calendar, Setting, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = computed(() => authStore.user?.username || '')
const userRole = computed(() => {
  const roles = authStore.user?.roles || []
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'PLANNER': '计划员',
    'SUPERVISOR': '主管'
  }
  return roles.map(r => roleMap[r] || r).join(', ')
})

const canAccessOrders = computed(() => {
  return authStore.hasRole('ADMIN') || authStore.hasRole('PLANNER')
})

const canAccessSchedule = computed(() => {
  return authStore.hasRole('ADMIN') || authStore.hasRole('PLANNER')
})

const canAccessUsers = computed(() => {
  return authStore.hasRole('ADMIN')
})

const goToOrders = () => router.push('/orders')
const goToSchedule = () => router.push('/schedule')
const goToUsers = () => router.push('/users')
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  animation: fadeIn 400ms cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-section {
  margin-bottom: 24px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #2563EB 0%, #1E40AF 100%);
  border-radius: var(--radius-lg);
  color: white;
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
}

h1 {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 6px 0;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.welcome-text {
  color: rgba(255, 255, 255, 0.95);
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
  font-weight: 400;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  cursor: default;
  transition: all 200ms ease;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
  background: white;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.stat-icon {
  font-size: 48px;
  margin-right: 16px;
  flex-shrink: 0;
  opacity: 0.9;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 6px;
  line-height: 1.5;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-card {
  margin-top: 24px;
  transition: all 250ms ease;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: white;
}

.info-card:hover {
  box-shadow: var(--shadow-md);
}

.info-card :deep(.el-card__header) {
  background: var(--color-surface-light);
  border-bottom: 1px solid var(--color-border);
  font-weight: 700;
  font-size: 16px;
  color: var(--color-text);
}

.quick-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding: 8px 0;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 160px;
  height: 56px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  transition: all 200ms ease;
  border: 2px solid transparent;
}

.quick-actions .el-button--primary {
  background: var(--color-primary);
  border: none;
  box-shadow: var(--shadow-sm);
}

.quick-actions .el-button--primary:hover {
  background: var(--color-primary-dark);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-actions .el-button--success {
  background: var(--color-success);
  border: none;
  box-shadow: var(--shadow-sm);
}

.quick-actions .el-button--success:hover {
  background: #15803D;
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-actions .el-button--warning {
  background: var(--color-warning);
  border: none;
  box-shadow: var(--shadow-sm);
}

.quick-actions .el-button--warning:hover {
  background: #D97706;
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-actions .el-button:active {
  transform: translateY(0);
}

.quick-actions .el-button:focus-visible {
  outline: 3px solid rgba(37, 99, 235, 0.3);
  outline-offset: 2px;
}

/* 响应式 */
@media (max-width: 768px) {
  .welcome-section {
    padding: 20px;
  }

  h1 {
    font-size: 24px;
  }

  .welcome-text {
    font-size: 13px;
  }

  .stat-icon {
    font-size: 42px;
    margin-right: 14px;
  }

  .stat-value {
    font-size: 28px;
  }

  .stat-label {
    font-size: 12px;
  }

  .quick-actions {
    flex-direction: column;
  }

  .quick-actions .el-button {
    width: 100%;
    min-width: 0;
  }
}

/* 支持 prefers-reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .dashboard,
  .stat-card,
  .info-card,
  .quick-actions .el-button {
    animation: none;
    transition: none;
  }

  .stat-card:hover,
  .quick-actions .el-button:hover,
  .quick-actions .el-button:active {
    transform: none;
  }
}
</style>
