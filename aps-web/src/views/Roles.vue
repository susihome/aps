<template>
  <div class="roles-page">
    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 左侧：角色列表 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3><el-icon><UserFilled /></el-icon> 角色列表</h3>
          <el-button type="primary" size="small" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>

        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索角色..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />

        <!-- 角色卡片列表 -->
        <div v-loading="loading" class="role-list">
          <div
            v-for="role in filteredRoles"
            :key="role.id"
            :class="['role-card', { active: selectedRole?.id === role.id }]"
            @click="selectRole(role)"
          >
            <div :class="['role-icon', getRoleIconClass(role.name)]">
              <el-icon><component :is="getRoleIcon(role.name)" /></el-icon>
            </div>
            <div class="role-info">
              <div class="role-name">{{ getRoleDisplayName(role.name) }}</div>
              <div class="role-tags">
                <el-tag :type="getRoleTagType(role.name)" size="small">{{ role.name }}</el-tag>
                <el-tag type="success" size="small">
                  <el-icon><CircleCheckFilled /></el-icon>
                  启用
                </el-tag>
              </div>
            </div>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, role)">
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <el-empty v-if="filteredRoles.length === 0" description="暂无角色" />
        </div>
      </div>

      <!-- 右侧：权限配置 -->
      <div class="right-panel">
        <div v-if="selectedRole" class="permission-panel">
          <div class="panel-header">
            <div class="header-left">
              <el-icon><Lock /></el-icon>
              <span>权限配置 - {{ selectedRole.name }}</span>
            </div>
            <el-button type="primary" size="small" @click="handleSavePermissions">
              <el-icon><Check /></el-icon>
              保存
            </el-button>
          </div>

          <!-- 权限操作栏 -->
          <div class="permission-actions">
            <el-button text size="small" @click="selectAllPermissions">
              <el-icon><Select /></el-icon>
              全选
            </el-button>
            <el-button text size="small" @click="clearAllPermissions">
              <el-icon><Close /></el-icon>
              清空
            </el-button>
            <div class="permission-count">
              已选择 {{ selectedPermissionIds.length }} 项权限
            </div>
          </div>

          <!-- 权限树 -->
          <div class="permission-tree-wrapper">
            <el-tree
              ref="permissionTreeRef"
              :data="permissionTree"
              :props="{ children: 'children', label: 'name' }"
              node-key="id"
              :default-checked-keys="selectedPermissionIds"
              :default-expand-all="false"
              show-checkbox
              @check="handlePermissionCheck"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon v-if="data.type === 'CATALOG'" class="node-icon"><Folder /></el-icon>
                  <el-icon v-else-if="data.type === 'MENU'" class="node-icon"><Document /></el-icon>
                  <el-icon v-else class="node-icon"><Operation /></el-icon>
                  <span class="node-label">{{ data.name }}</span>
                  <span class="node-code">{{ data.code }}</span>
                  <span class="node-count" v-if="data.children && data.children.length > 0">
                    {{ getCheckedCount(data) }}/{{ getTotalCount(data) }}
                  </span>
                </div>
              </template>
            </el-tree>
          </div>
        </div>

        <el-empty v-else description="请选择左侧角色节点查看详情" />
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="editingRole ? '编辑角色' : '新增角色'"
      width="500px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input
            v-model="formData.name"
            :disabled="!!editingRole"
            placeholder="请输入角色名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { msgSuccess, msgError, confirmDanger, extractErrorMsg } from '@/utils/message'
import {
  Plus,
  Search,
  UserFilled,
  Avatar,
  MoreFilled,
  Lock,
  Check,
  Select,
  Close,
  Folder,
  Document,
  Operation,
  Edit,
  Delete,
  CircleCheckFilled
} from '@element-plus/icons-vue'
import { useRoleApi, type Role, type RoleForm } from '@/api/role'
import { usePermissionApi, type Permission } from '@/api/permission'
import { dictionaryApi, type DictItem } from '@/api/dictionary'

const roleApi = useRoleApi()
const permissionApi = usePermissionApi()

const loading = ref(false)
const roles = ref<Role[]>([])
const selectedRole = ref<Role | null>(null)
const permissionTree = ref<Permission[]>([])
const searchKeyword = ref('')
const roleTypeItems = ref<DictItem[]>([])
const roleTypeLabelMap = ref<Record<string, string>>({})

const showDialog = ref(false)
const editingRole = ref<Role | null>(null)
const selectedPermissionIds = ref<string[]>([])

const permissionTreeRef = ref()
const formRef = ref()

const formData = ref<RoleForm>({
  name: '',
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  description: [{ max: 255, message: '描述长度不能超过 255 个字符', trigger: 'blur' }]
}

const filteredRoles = computed(() => {
  if (!searchKeyword.value) return roles.value
  return roles.value.filter((r: Role) =>
    r.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    r.description?.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

onMounted(async () => {
  await Promise.all([loadRoles(), loadPermissionTree(), loadRoleTypeDict()])
})

async function loadRoleTypeDict() {
  try {
    roleTypeItems.value = await dictionaryApi.getEnabledItemsByTypeCode('ROLE_TYPE')
    roleTypeLabelMap.value = Object.fromEntries(
      roleTypeItems.value.map(item => [item.itemCode, item.itemName])
    )
  } catch {
    roleTypeItems.value = []
  }
}

async function loadRoles() {
  loading.value = true
  try {
    const result = await roleApi.getRoles(0, 100)
    roles.value = result.content
    if (roles.value.length > 0 && !selectedRole.value) {
      await selectRole(roles.value[0])
    }
  } catch (error: any) {
    msgError(error.message || '加载角色失败')
  } finally {
    loading.value = false
  }
}

async function loadPermissionTree() {
  try {
    permissionTree.value = await permissionApi.getPermissionTree()
  } catch (error: any) {
    msgError(error.message || '加载权限树失败')
  }
}

async function selectRole(role: Role) {
  selectedRole.value = role
  try {
    const permissionIds = await roleApi.getRolePermissions(role.id)
    selectedPermissionIds.value = permissionIds
    await nextTick()
    if (permissionTreeRef.value) {
      permissionTreeRef.value.setCheckedKeys(permissionIds)
    }
  } catch (error: any) {
    msgError(error.message || '加载角色权限失败')
  }
}

function handleCreate() {
  editingRole.value = null
  formData.value = { name: '', description: '' }
  showDialog.value = true
}

function handleCommand(command: string, role: Role) {
  if (command === 'edit') {
    editingRole.value = role
    formData.value = {
      name: role.name,
      description: role.description || ''
    }
    showDialog.value = true
  } else if (command === 'delete') {
    handleDelete(role)
  }
}

async function handleSave() {
  if (!formRef.value) return
  formData.value.name = formData.value.name.trim()
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (editingRole.value) {
        await roleApi.updateRole(editingRole.value.id, formData.value)
        msgSuccess('角色更新成功')
      } else {
        await roleApi.createRole(formData.value)
        msgSuccess('角色创建成功')
      }
      showDialog.value = false
      await loadRoles()
    } catch (error: any) {
      msgError(error.message || '保存失败')
    }
  })
}

async function handleDelete(role: Role) {
  try {
    await confirmDanger(`确定删除角色 "${role.name}" 吗？`)
    await roleApi.deleteRole(role.id)
    msgSuccess('删除成功')
    if (selectedRole.value?.id === role.id) {
      selectedRole.value = null
    }
    await loadRoles()
  } catch (error: any) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除失败'))
    }
  }
}

function handlePermissionCheck() {
  if (permissionTreeRef.value) {
    selectedPermissionIds.value = permissionTreeRef.value.getCheckedKeys()
  }
}

async function handleSavePermissions() {
  if (!selectedRole.value) return
  try {
    await roleApi.assignPermissions(selectedRole.value.id, selectedPermissionIds.value)
    msgSuccess('权限保存成功')
    await loadRoles()
  } catch (error: any) {
    msgError(error.message || '保存失败')
  }
}

function selectAllPermissions() {
  const allIds = getAllPermissionIds(permissionTree.value)
  selectedPermissionIds.value = allIds
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys(allIds)
  }
}

function clearAllPermissions() {
  selectedPermissionIds.value = []
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys([])
  }
}

function getAllPermissionIds(nodes: Permission[]): string[] {
  let ids: string[] = []
  for (const node of nodes) {
    ids.push(node.id)
    if (node.children && node.children.length > 0) {
      ids = ids.concat(getAllPermissionIds(node.children))
    }
  }
  return ids
}

function getCheckedCount(node: Permission): number {
  let count = 0
  if (selectedPermissionIds.value.includes(node.id)) count++
  if (node.children) {
    for (const child of node.children) {
      count += getCheckedCount(child)
    }
  }
  return count
}

function getTotalCount(node: Permission): number {
  let count = 1
  if (node.children) {
    for (const child of node.children) {
      count += getTotalCount(child)
    }
  }
  return count
}

// 获取角色图标
function getRoleIcon(roleName: string) {
  const iconMap: Record<string, any> = {
    'ADMIN': UserFilled,
    'PLANNER': Document,
    'SUPERVISOR': Lock
  }
  return iconMap[roleName] || Avatar
}

// 获取角色图标样式类
function getRoleIconClass(roleName: string): string {
  const classMap: Record<string, string> = {
    'ADMIN': 'icon-admin',
    'PLANNER': 'icon-planner',
    'SUPERVISOR': 'icon-supervisor'
  }
  return classMap[roleName] || ''
}

// 获取角色标签类型
function getRoleTagType(roleName: string): string {
  const typeMap: Record<string, string> = {
    'ADMIN': 'danger',
    'PLANNER': 'warning',
    'SUPERVISOR': 'info'
  }
  return typeMap[roleName] || 'info'
}

// 获取角色显示名称
function getRoleDisplayName(roleName: string): string {
  return roleTypeLabelMap.value[roleName] || roleName
}
</script>

<style scoped>
.roles-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.content-wrapper {
  display: flex;
  gap: 20px;
  height: calc(100vh - 200px);
}

.left-panel {
  width: 350px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.right-panel {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 20px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.panel-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.search-input {
  margin-bottom: 16px;
}

.role-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 8px;
}

.role-list::-webkit-scrollbar {
  width: 6px;
}

.role-list::-webkit-scrollbar-track {
  background: transparent;
}

.role-list::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.role-list::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

.role-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.role-card:hover {
  border-color: #409eff;
  background: #f0f9ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.role-card.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.role-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.role-icon.icon-admin {
  background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
}

.role-icon.icon-planner {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.role-icon.icon-supervisor {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.role-info {
  flex: 1;
  min-width: 0;
}

.role-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #303133;
}

.role-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.more-icon {
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.3s;
}

.more-icon:hover {
  color: #409eff;
}

.permission-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.permission-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.permission-count {
  margin-left: auto;
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

.permission-tree-wrapper {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
}

.permission-tree-wrapper::-webkit-scrollbar {
  width: 6px;
}

.permission-tree-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.permission-tree-wrapper::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.permission-tree-wrapper::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  font-size: 14px;
}

.node-icon {
  color: #909399;
  font-size: 16px;
  flex-shrink: 0;
}

.node-label {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.node-code {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.node-count {
  margin-left: auto;
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 10px;
  flex-shrink: 0;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-tree) {
  background: transparent;
}

:deep(.el-tree-node) {
  padding: 4px 0;
}

:deep(.el-tree-node__content) {
  height: 32px;
  padding: 0 4px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f0f9ff;
}

:deep(.el-checkbox) {
  margin-right: 8px;
}

:deep(.el-tree-node.is-checked > .el-tree-node__content) {
  background-color: #ecf5ff;
}
</style>
