<template>
  <div class="permissions-page">
    <div class="permissions-layout">
      <el-card class="tree-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <div class="panel-title-wrap">
              <el-icon class="panel-title-icon"><Lock /></el-icon>
              <span class="panel-title">权限列表</span>
            </div>
            <el-button type="primary" size="small" @click="openCreateDialog()">
              <el-icon><Plus /></el-icon>
              <span>新增</span>
            </el-button>
          </div>
        </template>

        <div class="tree-toolbar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索权限..."
            :prefix-icon="Search"
            clearable
            class="tree-search"
          />
          <div class="tree-actions">
            <el-button text size="small" @click="expandAll">全部展开</el-button>
            <el-button text size="small" @click="collapseAll">全部收起</el-button>
            <el-button text size="small" @click="resetTree">重置</el-button>
            <el-button text size="small" :loading="loading" @click="loadPermissions">刷新</el-button>
          </div>
        </div>

        <el-tree
          ref="treeRef"
          class="permission-tree"
          node-key="id"
          :data="filteredPermissionTree"
          :props="treeProps"
          :expand-on-click-node="false"
          :highlight-current="true"
          :default-expanded-keys="expandedKeys"
          draggable
          @node-click="handleNodeClick"
          @node-drop="handleNodeDrop"
        >
          <template #default="{ data }">
            <div class="tree-node" :class="{ active: selectedPermission?.id === data.id }">
              <div class="tree-node-main">
                <div class="tree-node-icon" :class="data.type">
                  <el-icon><component :is="iconMap[data.icon] || Menu" /></el-icon>
                </div>
                <div class="tree-node-text">
                  <div class="tree-node-title">{{ data.name }}</div>
                  <div class="tree-node-code">{{ data.code }}</div>
                </div>
              </div>
              <el-tag size="small" round :type="getTagType(data.type)">{{ getTypeLabel(data.type) }}</el-tag>
            </div>
          </template>
        </el-tree>
      </el-card>

      <el-card class="detail-panel" shadow="never">
        <template #header>
          <div class="detail-header">
            <div class="detail-title-wrap">
              <div class="detail-icon">
                <el-icon><component :is="iconMap[selectedPermission?.icon || 'Menu'] || Menu" /></el-icon>
              </div>
              <div>
                <div class="detail-title">{{ selectedPermission?.name || '请选择权限' }}</div>
                <div class="detail-subtitle">{{ selectedPermission?.code || '点击左侧权限节点查看详情' }}</div>
              </div>
            </div>
            <div class="detail-actions" v-if="selectedPermission">
              <el-switch
                :model-value="selectedPermission.enabled"
                inline-prompt
                active-text="启用"
                inactive-text="停用"
                :loading="toggleLoading"
                @change="handleTogglePermission"
              />
              <el-button type="primary" plain @click="openEditDialog(selectedPermission)">
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button type="danger" plain @click="handleDeletePermission(selectedPermission.id)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
          </div>
        </template>

        <template v-if="selectedPermission">
          <section class="detail-section">
            <div class="section-title">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">权限编码</span>
                <span class="info-value">{{ selectedPermission.code }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">权限名称</span>
                <span class="info-value">{{ selectedPermission.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">权限类型</span>
                <span class="info-value">{{ typeLabelMap[selectedPermission.type] }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">路由路径</span>
                <span class="info-value">{{ selectedPermission.routePath || '无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">图标</span>
                <span class="info-value">{{ selectedPermission.icon }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">排序号</span>
                <span class="info-value">{{ selectedPermission.sort }}</span>
              </div>
            </div>
            <div v-if="selectedPermission.description" class="description-box">
              <span class="info-label">描述</span>
              <p class="description-text">{{ selectedPermission.description }}</p>
            </div>
          </section>

          <section class="detail-section">
            <div class="section-title">
              <el-icon><Opportunity /></el-icon>
              <span>状态信息</span>
            </div>
            <div class="status-grid">
              <div class="status-item">
                <span class="info-label">启用状态</span>
                <el-tag :type="selectedPermission.enabled ? 'success' : 'danger'" round>
                  {{ selectedPermission.enabled ? '已启用' : '已停用' }}
                </el-tag>
              </div>
              <div class="status-item">
                <span class="info-label">是否可见</span>
                <span class="info-value">{{ selectedPermission.visible ? '可见' : '隐藏' }}</span>
              </div>
            </div>
          </section>

          <section class="detail-section" v-if="selectedPermission.children?.length">
            <div class="section-title">
              <el-icon><Lightning /></el-icon>
              <span>子权限 ({{ selectedPermission.children.length }})</span>
            </div>
            <div class="child-permissions">
              <div v-for="child in selectedPermission.children" :key="child.id" class="child-permission-card">
                <div class="child-main" @click="selectPermissionById(child.id)">
                  <div class="child-icon">
                    <el-icon><component :is="iconMap[child.icon] || Key" /></el-icon>
                  </div>
                  <div class="child-text">
                    <div class="child-title">{{ child.name }}</div>
                    <div class="child-code">{{ child.code }}</div>
                  </div>
                </div>
                <div class="child-actions">
                  <el-button text size="small" @click.stop="openEditDialog(child)">编辑</el-button>
                  <el-button text size="small" type="danger" @click.stop="handleDeletePermission(child.id)">删除</el-button>
                </div>
              </div>
            </div>
          </section>
        </template>

        <el-empty v-else description="请选择左侧权限节点" />
      </el-card>
    </div>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑权限' : '新增权限'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="权限编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="如: system:user:add"
            :disabled="isEditMode"
          />
        </el-form-item>

        <el-form-item label="权限名称" prop="name">
          <el-input v-model="formData.name" placeholder="如: 新增用户" />
        </el-form-item>

        <el-form-item label="权限类型" prop="type">
          <el-select v-model="formData.type" placeholder="选择权限类型">
            <el-option
              v-for="item in permissionTypeItems"
              :key="item.itemCode"
              :label="item.itemName"
              :value="item.itemCode.toLowerCase()"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="父权限" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="permissionTree"
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            placeholder="选择父权限（可选）"
            clearable
            check-strictly
          />
        </el-form-item>

        <el-form-item label="路由路径" prop="routePath">
          <el-input v-model="formData.routePath" placeholder="如: /users" />
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-select v-model="formData.icon" placeholder="选择图标">
            <el-option label="Menu" value="Menu" />
            <el-option label="Setting" value="Setting" />
            <el-option label="User" value="User" />
            <el-option label="Lock" value="Lock" />
            <el-option label="Key" value="Key" />
            <el-option label="Plus" value="Plus" />
            <el-option label="Edit" value="Edit" />
            <el-option label="Delete" value="Delete" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            rows="3"
            placeholder="权限描述（可选）"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="formData.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>

        <el-form-item label="可见性">
          <el-switch v-model="formData.visible" active-text="可见" inactive-text="隐藏" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import type { Component } from 'vue'
import type { ElTree, FormInstance } from 'element-plus'
import { msgSuccess, msgError, confirmDanger } from '@/utils/message'
import {
  Plus,
  Search,
  Menu,
  Setting,
  User,
  Key,
  Edit,
  Delete,
  InfoFilled,
  Opportunity,
  Lightning,
  Lock
} from '@element-plus/icons-vue'
import { usePermissionApi, type Permission, type PermissionForm } from '../api/permission'
import { dictionaryApi, type DictItem } from '@/api/dictionary'

type PermissionType = 'catalog' | 'menu' | 'button'

const permissionApi = usePermissionApi()
const treeRef = ref<InstanceType<typeof ElTree>>()
const formRef = ref<FormInstance>()

const searchKeyword = ref('')
const expandedKeys = ref<string[]>([])
const loading = ref(false)
const toggleLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEditMode = ref(false)

const iconMap: Record<string, Component> = {
  Plus,
  Menu,
  Setting,
  User,
  Key,
  Edit,
  Delete,
  Lock
}

const permissionTypeItems = ref<DictItem[]>([])
const typeLabelMap = ref<Record<string, string>>({
  catalog: '目录',
  menu: '菜单',
  button: '按钮'
})

const tagTypeMap: Record<PermissionType, 'info' | 'primary' | 'success'> = {
  catalog: 'info',
  menu: 'primary',
  button: 'success'
}

const permissionTree = ref<Permission[]>([])
const selectedPermissionId = ref('')

const formData = reactive<PermissionForm>({
  code: '',
  name: '',
  description: '',
  type: 'menu',
  routePath: '',
  icon: 'Menu',
  sort: 0,
  enabled: true,
  visible: true,
  parentId: undefined
})

const formRules = {
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9:_-]+$/, message: '权限编码只能包含字母、数字、冒号、下划线和连字符', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
  icon: [{ required: true, message: '请选择图标', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
}

const treeProps = {
  label: 'name',
  children: 'children'
}

function getTypeLabel(type: PermissionType): string {
  return typeLabelMap.value[type] ?? type
}

function getTagType(type: PermissionType): 'info' | 'primary' | 'success' {
  return tagTypeMap[type]
}

function flattenPermissions(nodes: Permission[]): Permission[] {
  return nodes.flatMap(node => [node, ...(node.children ? flattenPermissions(node.children) : [])])
}

const flatPermissions = computed(() => flattenPermissions(permissionTree.value))

const selectedPermission = computed(() => {
  return flatPermissions.value.find(item => item.id === selectedPermissionId.value) || null
})

function filterTree(nodes: Permission[], keyword: string): Permission[] {
  if (!keyword.trim()) return nodes
  const normalizedKeyword = keyword.trim().toLowerCase()
  const result: Permission[] = []

  nodes.forEach(node => {
    const filteredChildren = node.children ? filterTree(node.children, normalizedKeyword) : undefined
    const matched =
      node.name.toLowerCase().includes(normalizedKeyword) ||
      node.code.toLowerCase().includes(normalizedKeyword)

    if (matched || (filteredChildren && filteredChildren.length > 0)) {
      result.push({
        ...node,
        children: filteredChildren
      })
    }
  })

  return result
}

const filteredPermissionTree = computed(() => filterTree(permissionTree.value, searchKeyword.value))

async function loadPermissions() {
  loading.value = true
  try {
    const data = await permissionApi.getPermissionTree()
    permissionTree.value = data
    expandedKeys.value = collectExpandableKeys(data)
  } catch (error: any) {
    // 显示详细的错误信息
    const message = error.response?.data?.message || error.message || '加载权限失败'
    msgError(message)
    console.error('权限加载错误:', error)
  } finally {
    loading.value = false
  }
}

function collectExpandableKeys(nodes: Permission[]): string[] {
  return nodes.flatMap(node => {
    const own = node.children?.length ? [node.id] : []
    return [...own, ...(node.children ? collectExpandableKeys(node.children) : [])]
  })
}

function handleNodeClick(data: Permission) {
  selectedPermissionId.value = data.id
}

function selectPermissionById(id: string) {
  selectedPermissionId.value = id
}

function expandAll() {
  expandedKeys.value = collectExpandableKeys(permissionTree.value)
}

function collapseAll() {
  expandedKeys.value = []
}

function resetTree() {
  searchKeyword.value = ''
  expandedKeys.value = collectExpandableKeys(permissionTree.value)
}

function resetForm() {
  formRef.value?.clearValidate()
  formData.code = ''
  formData.name = ''
  formData.description = ''
  formData.type = 'menu'
  formData.routePath = ''
  formData.icon = 'Menu'
  formData.sort = 0
  formData.enabled = true
  formData.visible = true
  formData.parentId = undefined
  isEditMode.value = false
}

function openCreateDialog(parentId?: string) {
  resetForm()
  if (parentId) {
    formData.parentId = parentId
  }
  dialogVisible.value = true
}

function openEditDialog(permission: Permission) {
  resetForm()
  isEditMode.value = true
  formData.code = permission.code
  formData.name = permission.name
  formData.description = permission.description || ''
  formData.type = permission.type
  formData.routePath = permission.routePath || ''
  formData.icon = permission.icon
  formData.sort = permission.sort
  formData.enabled = permission.enabled
  formData.visible = permission.visible
  formData.parentId = permission.parentId
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEditMode.value && selectedPermission.value) {
        await permissionApi.updatePermission(selectedPermission.value.id, formData)
        msgSuccess('权限更新成功')
      } else {
        await permissionApi.createPermission(formData)
        msgSuccess('权限创建成功')
      }
      dialogVisible.value = false
      await loadPermissions()
    } catch (error) {
      msgError(isEditMode.value ? '权限更新失败' : '权限创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleTogglePermission(enabled: boolean) {
  if (!selectedPermission.value) return

  toggleLoading.value = true
  try {
    await permissionApi.togglePermission(selectedPermission.value.id, enabled)
    msgSuccess(enabled ? '权限已启用' : '权限已停用')
    await loadPermissions()
  } catch (error) {
    msgError('状态更新失败')
  } finally {
    toggleLoading.value = false
  }
}

async function handleDeletePermission(id: string) {
  try {
    await confirmDanger('确定删除该权限吗？删除后无法恢复。')

    await permissionApi.deletePermission(id)
    msgSuccess('权限删除成功')
    if (selectedPermissionId.value === id) {
      selectedPermissionId.value = ''
    }
    await loadPermissions()
  } catch (error: any) {
    // 确认框取消时抛出 'cancel' 字符串
    if (error !== 'cancel') {
      msgError('权限删除失败')
    }
  }
}

async function handleNodeDrop(draggingNode: any, dropNode: any, dropType: string, ev: DragEvent) {
  try {
    const updates = collectSortUpdates(permissionTree.value)
    if (updates.length > 0) {
      await permissionApi.updateSort(updates)
      msgSuccess('排序已更新')
    }
  } catch (error) {
    msgError('排序更新失败')
    await loadPermissions()
  }
}

function collectSortUpdates(nodes: Permission[], sort = 0): Array<{ id: string; sort: number }> {
  const updates: Array<{ id: string; sort: number }> = []
  nodes.forEach((node, index) => {
    const newSort = sort * 1000 + index
    if (node.sort !== newSort) {
      updates.push({ id: node.id, sort: newSort })
    }
    if (node.children?.length) {
      updates.push(...collectSortUpdates(node.children, newSort + 1))
    }
  })
  return updates
}

async function loadPermissionTypeDict() {
  try {
    permissionTypeItems.value = await dictionaryApi.getEnabledItemsByTypeCode('PERMISSION_TYPE')
    if (permissionTypeItems.value.length > 0) {
      typeLabelMap.value = Object.fromEntries(
        permissionTypeItems.value.map(item => [item.itemCode.toLowerCase(), item.itemName])
      )
    }
  } catch {
    permissionTypeItems.value = []
  }
}

onMounted(() => {
  loadPermissions()
  loadPermissionTypeDict()
})
</script>

<style scoped>
.permissions-page {
  min-height: 100%;
}

.permissions-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
  min-height: calc(100vh - 190px);
}

.tree-panel,
.detail-panel {
  border-radius: 16px;
  border: 1px solid var(--color-border);
}

.panel-header,
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-title-wrap,
.detail-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-title-icon,
.detail-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(37, 99, 235, 0.1);
  color: var(--color-primary);
}

.panel-title,
.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
}

.detail-subtitle {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.tree-toolbar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.tree-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.permission-tree {
  max-height: calc(100vh - 290px);
  overflow: auto;
}

.tree-node {
  width: 100%;
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px 8px 2px;
}

.tree-node-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.tree-node-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  background: rgba(37, 99, 235, 0.08);
  flex-shrink: 0;
}

.tree-node-icon.catalog {
  background: rgba(148, 163, 184, 0.16);
  color: #475569;
}

.tree-node-icon.menu {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
}

.tree-node-icon.button {
  background: rgba(34, 197, 94, 0.12);
  color: #16a34a;
}

.tree-node-text {
  min-width: 0;
}

.tree-node-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.tree-node-code {
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin-top: 2px;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-section + .detail-section {
  margin-top: 22px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 12px;
}

.info-grid,
.status-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.info-item,
.status-item {
  border-radius: 14px;
  background: #f8fbff;
  border: 1px solid #e8eef8;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.description-box {
  margin-top: 12px;
  padding: 12px;
  background: #f8fbff;
  border-radius: 8px;
  border: 1px solid #e8eef8;
}

.description-text {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.child-permissions {
  display: grid;
  gap: 10px;
}

.child-permission-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  background: #fff;
  transition: all 200ms;
}

.child-permission-card:hover {
  border-color: var(--color-primary);
  background: rgba(37, 99, 235, 0.02);
}

.child-main {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex: 1;
}

.child-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(37, 99, 235, 0.08);
  color: var(--color-primary);
  flex-shrink: 0;
}

.child-text {
  min-width: 0;
}

.child-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.child-code {
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin-top: 3px;
}

.child-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--color-border);
}

:deep(.el-tree-node__content) {
  min-height: 52px;
  border-radius: 12px;
}

:deep(.el-tree-node__content:hover) {
  background: rgba(37, 99, 235, 0.06);
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(37, 99, 235, 0.1);
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--color-text);
}

@media (max-width: 992px) {
  .permissions-layout {
    grid-template-columns: 1fr;
  }

  .permission-tree {
    max-height: 360px;
  }
}
</style>
