<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Delete, Edit, Key, Plus, RefreshRight, Search } from '@element-plus/icons-vue'
import UserEditorDialog from '@/components/users/UserEditorDialog.vue'
import { useUserManagement } from '@/composables/useUserManagement'
import { useAuthStore } from '@/stores/auth'
import type { User } from '@/types/auth'

const authStore = useAuthStore()
const {
  loading,
  submitting,
  users,
  roleOptions,
  dialogVisible,
  dialogMode,
  filters,
  pagination,
  formInitialValue,
  initialize,
  handleSearch,
  handleResetFilters,
  handlePageChange,
  handlePageSizeChange,
  openCreateDialog,
  openEditDialog,
  closeDialog,
  submitUser,
  deleteUser,
  resetPassword,
} = useUserManagement()

const canCreateUser = computed(() => authStore.hasPermission('system:user:add'))
const canEditUser = computed(() => authStore.hasPermission('system:user:edit'))
const canDeleteUser = computed(() => authStore.hasPermission('system:user:delete'))
const canResetPassword = computed(() => authStore.hasPermission('system:user:reset_password'))

onMounted(() => {
  initialize()
})

function getRoleLabel(roleName: string): string {
  const roleMap: Record<string, string> = {
    ADMIN: '管理员',
    PLANNER: '计划员',
    SUPERVISOR: '主管',
  }
  return roleMap[roleName] || roleName
}

function formatDate(dateStr?: string): string {
  if (!dateStr) {
    return '-'
  }
  return new Date(dateStr).toLocaleString('zh-CN')
}

function getRoleNames(user: User): string[] {
  return user.roles
    .map((role) => typeof role === 'string' ? role : role.name)
    .filter((roleName): roleName is string => Boolean(roleName))
}
</script>

<template>
  <div class="users-page">
    <section class="hero-card">
      <div>
        <p class="hero-card__eyebrow">System Control</p>
        <h2 class="hero-card__title">用户管理</h2>
        <p class="hero-card__desc">统一管理系统账号、角色归属、启停状态和密码重置。</p>
      </div>
      <el-button
        v-if="canCreateUser"
        type="primary"
        class="hero-card__action"
        @click="openCreateDialog"
      >
        <el-icon><Plus /></el-icon>
        <span>新建用户</span>
      </el-button>
    </section>

    <el-card shadow="hover" class="content-card">
      <div class="filters">
        <el-input
          v-model="filters.keyword"
          class="filters__keyword"
          placeholder="搜索用户名或邮箱"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="filters.enabled" clearable placeholder="状态筛选" class="filters__status">
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
        <div class="filters__actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            <span>查询</span>
          </el-button>
          <el-button @click="handleResetFilters">
            <el-icon><RefreshRight /></el-icon>
            <span>重置</span>
          </el-button>
        </div>
      </div>

      <el-table
        :data="users"
        v-loading="loading"
        class="users-table"
        row-key="id"
        empty-text="暂无用户数据"
      >
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column label="角色" min-width="220">
          <template #default="{ row }">
            <div class="role-tags">
              <el-tag
                v-for="roleName in getRoleNames(row)"
                :key="`${row.id}-${roleName}`"
                size="small"
                effect="plain"
              >
                {{ getRoleLabel(roleName) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small" effect="dark">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近登录" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.lastLoginAt) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="260" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button v-if="canEditUser" text type="primary" size="small" @click="openEditDialog(row)">
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button v-if="canResetPassword" text type="warning" size="small" @click="resetPassword(row)">
                <el-icon><Key /></el-icon>
                <span>重置密码</span>
              </el-button>
              <el-button v-if="canDeleteUser" text type="danger" size="small" @click="deleteUser(row)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <div class="pagination-bar__summary">共 {{ pagination.total }} 条用户记录</div>
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </el-card>

    <UserEditorDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :loading="submitting"
      :roles="roleOptions"
      :initial-value="formInitialValue"
      @submit="submitUser"
      @update:model-value="(value) => { if (!value) closeDialog() }"
    />
  </div>
</template>

<style scoped>
.users-page {
  display: grid;
  gap: 20px;
  animation: users-enter 320ms ease-out;
}

.hero-card {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 28px;
  border-radius: 20px;
  background:
    radial-gradient(circle at top right, rgba(249, 115, 22, 0.32), transparent 28%),
    linear-gradient(135deg, #fff7ed 0%, #ffffff 48%, #f8fafc 100%);
  border: 1px solid rgba(249, 115, 22, 0.18);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.hero-card__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #c2410c;
}

.hero-card__title {
  margin: 0;
  font-size: 30px;
  line-height: 1.1;
  color: #0f172a;
}

.hero-card__desc {
  margin: 10px 0 0;
  color: #475569;
}

.hero-card__action {
  min-height: 44px;
  border: none;
  border-radius: 999px;
  padding: 0 18px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  box-shadow: 0 10px 24px rgba(249, 115, 22, 0.24);
}

.content-card {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
}

.filters {
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) minmax(160px, 0.7fr) auto;
  gap: 12px;
  margin-bottom: 18px;
}

.filters__keyword,
.filters__status {
  width: 100%;
}

.filters__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.users-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #0f172a;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
}

.pagination-bar__summary {
  color: #64748b;
  font-size: 13px;
}

@keyframes users-enter {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 960px) {
  .hero-card {
    flex-direction: column;
    align-items: stretch;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .filters__actions,
  .pagination-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
