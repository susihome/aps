<template>
  <el-dropdown @command="handleCommand" trigger="click">
    <div class="user-menu" role="button" tabindex="0" aria-label="用户菜单">
      <el-avatar :size="32" class="user-avatar" :aria-label="`用户 ${username}`">
        {{ userInitial }}
      </el-avatar>
      <span class="username">{{ username }}</span>
      <el-icon class="el-icon--right" aria-hidden="true"><ArrowDown /></el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item disabled>
          <div class="user-info">
            <div class="user-name">{{ username }}</div>
            <div class="user-role">{{ userRole }}</div>
          </div>
        </el-dropdown-item>
        <el-dropdown-item divided command="logout" aria-label="退出登录">
          <el-icon aria-hidden="true"><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { msgSuccess, confirmAction } from '@/utils/message'
import { ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = computed(() => authStore.user?.username || '')
const userInitial = computed(() => username.value.charAt(0).toUpperCase())
const userRole = computed(() => {
  const roles = authStore.user?.roles || []
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'PLANNER': '计划员',
    'SUPERVISOR': '主管'
  }
  return roles.map(r => {
    const name = typeof r === 'string' ? r : r.name
    return roleMap[name] || name
  }).join(', ')
})

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await confirmAction('确定要退出登录吗？')

      await authStore.logout()
      msgSuccess('已退出登录')
      router.push('/login')
    } catch (error) {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.user-menu {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 10px;
  transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 44px;
  border: 1px solid transparent;
}

.user-menu:hover {
  background: #f3f4f6;
  border-color: #e5e7eb;
}

.user-menu:focus-visible {
  outline: 2px solid #667eea;
  outline-offset: 2px;
}

.user-avatar {
  background: #2563EB;
  color: white;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.25);
}

.username {
  margin: 0 12px;
  color: #1E293B;
  font-size: 14px;
  line-height: 1.5;
  font-weight: 600;
}

.user-info {
  padding: 12px 0;
  min-width: 140px;
}

.user-name {
  font-size: 14px;
  color: #1E293B;
  font-weight: 600;
  line-height: 1.5;
}

.user-role {
  font-size: 12px;
  color: #64748B;
  margin-top: 4px;
  line-height: 1.5;
  font-weight: 500;
}

/* 下拉菜单动画 */
:deep(.el-dropdown-menu) {
  animation: slideDown 250ms cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 8px;
}

:deep(.el-dropdown-menu__item) {
  border-radius: 6px;
  padding: 10px 16px;
  transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f3f4f6;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .username {
    display: none;
  }

  .user-menu {
    padding: 8px;
  }
}

/* 支持 prefers-reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .user-menu,
  :deep(.el-dropdown-menu),
  :deep(.el-dropdown-menu__item) {
    transition: none;
    animation: none;
  }
}
</style>
