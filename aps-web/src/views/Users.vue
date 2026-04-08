<template>
  <div class="users-page">
    <h2>用户管理</h2>

    <el-card shadow="hover">
      <div class="toolbar">
        <el-button v-if="canCreateUser" type="primary" @click="showCreateDialog" aria-label="新建用户">
          <el-icon><Plus /></el-icon>
          <span>新建用户</span>
        </el-button>
      </div>

      <el-table :data="users" style="width: 100%" aria-label="用户列表">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 5px">
              {{ getRoleLabel(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button v-if="canEditUser" text type="primary" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button v-if="canDeleteUser" text type="danger" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
              <el-button v-if="canResetPassword" text type="warning" size="small" @click="handleResetPassword(row)">
                <el-icon><Key /></el-icon>
                <span>重置密码</span>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="新建用户"
      width="90%"
      :style="{ maxWidth: '500px' }"
      :close-on-click-modal="false"
      aria-labelledby="dialog-title"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            autocomplete="off"
            aria-label="用户名"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            autocomplete="new-password"
            aria-label="密码"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            autocomplete="email"
            aria-label="邮箱"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" aria-label="取消">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="loading" aria-label="创建用户">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Key } from '@element-plus/icons-vue'
import { userApi } from '../api'
import type { User } from '../types/auth'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const users = ref<User[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const canCreateUser = computed(() => authStore.hasPermission('system:user:add'))
const canEditUser = computed(() => authStore.hasPermission('system:user:edit'))
const canDeleteUser = computed(() => authStore.hasPermission('system:user:delete'))
const canResetPassword = computed(() => authStore.hasPermission('system:user:reset_password'))

const form = ref({
  username: '',
  password: '',
  email: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  try {
    users.value = await userApi.list()
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

function showCreateDialog() {
  form.value = {
    username: '',
    password: '',
    email: ''
  }
  dialogVisible.value = true
}

function handleEdit(_user: User) {
  ElMessage.info('编辑用户功能待接入后端接口')
}

function handleDelete(_user: User) {
  ElMessage.info('删除用户功能待接入后端接口')
}

function handleResetPassword(_user: User) {
  ElMessage.info('重置密码功能待接入后端接口')
}

async function handleCreate() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userApi.create({
        ...form.value,
        roleIds: []
      })
      ElMessage.success('用户创建成功')
      dialogVisible.value = false
      loadUsers()
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : '创建用户失败'
      ElMessage.error(message)
    } finally {
      loading.value = false
    }
  })
}

function getRoleLabel(role: { name: string }): string {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'PLANNER': '计划员',
    'SUPERVISOR': '主管'
  }
  return roleMap[role.name] || role.name
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
.users-page {
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

.users-page h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.3;
  letter-spacing: -0.02em;
  font-family: 'Fira Sans', sans-serif;
}

.users-page :deep(.el-card) {
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.toolbar {
  margin-bottom: 20px;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar .el-button {
  min-height: 44px;
  transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  font-weight: 600;
  background: #F97316;
  border: none;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.25);
  cursor: pointer;
}

.toolbar .el-button:hover {
  background: #EA580C;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.35);
}

.toolbar .el-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}

.toolbar .el-button:focus-visible {
  outline: 3px solid rgba(249, 115, 22, 0.3);
  outline-offset: 2px;
}

:deep(.el-table) {
  font-size: 14px;
  border-radius: 8px;
}

:deep(.el-table th) {
  font-weight: 600;
  background: #F8FAFC;
  color: #1E293B;
}

:deep(.el-table tr:hover) {
  background: #F8FAFC;
}

:deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

:deep(.el-dialog__header) {
  padding: 20px 20px 16px;
  border-bottom: 1px solid #E2E8F0;
  font-weight: 600;
  font-size: 16px;
  color: #1E293B;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 16px 20px 20px;
  border-top: 1px solid #E2E8F0;
}

/* 响应式 */
@media (max-width: 768px) {
  .users-page h2 {
    font-size: 24px;
  }

  :deep(.el-table) {
    font-size: 13px;
  }

  :deep(.el-table-column) {
    min-width: auto !important;
  }
}

/* 支持 prefers-reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .users-page,
  .toolbar .el-button {
    animation: none;
    transition: none;
  }

  .toolbar .el-button:hover,
  .toolbar .el-button:active {
    transform: none;
  }
}
</style>
