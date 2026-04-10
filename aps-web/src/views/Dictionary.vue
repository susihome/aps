<template>
  <div class="dictionary-page">
    <el-empty v-if="!canAccessDictionaryPage" description="暂无编码管理权限" />
    <template v-else>
    <div class="page-header">
      <div>
        <h2>编码管理</h2>
        <p>统一维护字典类型和字典项</p>
      </div>
      <el-button v-if="canAddType" type="primary" @click="openTypeDialog()">
        <el-icon><Plus /></el-icon>
        新增类型
      </el-button>
    </div>

    <div class="content-wrapper">
      <div class="left-panel">
        <div class="panel-header">
          <span class="panel-title">字典类型</span>
          <el-input
            v-model="typeKeyword"
            placeholder="搜索类型编码/名称"
            clearable
            :disabled="!canListTypes"
            @keyup.enter="loadTypes"
          >
            <template #append>
              <el-button :disabled="!canListTypes" @click="loadTypes">查询</el-button>
            </template>
          </el-input>
        </div>

        <el-table
          v-loading="typeLoading"
          :data="typeList"
          height="460"
          highlight-current-row
          @current-change="handleTypeSelect"
        >
          <el-table-column prop="code" label="编码" min-width="120" />
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="canEditType || canRemoveType" label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canEditType" text type="primary" @click="openTypeDialog(row)">编辑</el-button>
              <el-button v-if="canRemoveType" text type="danger" @click="deleteType(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          v-model:current-page="typePageNo"
          v-model:page-size="typePageSize"
          :total="typeTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="loadTypes"
          @size-change="handleTypeSizeChange"
        />
      </div>

      <div class="right-panel">
        <div class="panel-header">
          <span class="panel-title">
            字典项
            <template v-if="selectedType">（{{ selectedType.name }}）</template>
          </span>
          <div class="item-actions">
            <el-input
              v-model="itemKeyword"
              placeholder="搜索项编码/名称/值"
              clearable
              :disabled="!canListItems"
              style="width: 240px"
              @keyup.enter="loadItems"
            />
            <el-button :disabled="!canListItems" @click="loadItems">查询</el-button>
            <el-button v-if="canAddItem" type="primary" :disabled="!selectedType" @click="openItemDialog()">
              <el-icon><Plus /></el-icon>
              新增项
            </el-button>
          </div>
        </div>

        <el-table v-loading="itemLoading" :data="itemList" height="460">
          <el-table-column prop="itemCode" label="项编码" min-width="110" />
          <el-table-column prop="itemName" label="项名称" min-width="120" />
          <el-table-column prop="itemValue" label="项值" min-width="120" />
          <el-table-column v-if="canEditItem" label="状态" width="120">
            <template #default="{ row }">
              <el-switch
                :model-value="row.enabled"
                inline-prompt
                active-text="启用"
                inactive-text="停用"
                @change="(value: boolean) => toggleItem(row, value)"
              />
            </template>
          </el-table-column>
          <el-table-column v-else label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="系统项" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.isSystem ? 'warning' : 'info'">
                {{ row.isSystem ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="canEditItem || canRemoveItem" label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canEditItem" text type="primary" @click="openItemDialog(row)">编辑</el-button>
              <el-button v-if="canRemoveItem" text type="danger" :disabled="row.isSystem" @click="deleteItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          v-model:current-page="itemPageNo"
          v-model:page-size="itemPageSize"
          :total="itemTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="loadItems"
          @size-change="handleItemSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="typeDialogVisible" :title="editingType ? '编辑字典类型' : '新增字典类型'" width="520px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="类型编码" prop="code">
          <el-input v-model="typeForm.code" :disabled="!!editingType" />
        </el-form-item>
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="typeForm.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="typeForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="typeForm.enabled" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="typeForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="editingItem ? '编辑字典项' : '新增字典项'" width="560px">
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="项编码" prop="itemCode">
          <el-input v-model="itemForm.itemCode" :disabled="!!editingItem" />
        </el-form-item>
        <el-form-item label="项名称" prop="itemName">
          <el-input v-model="itemForm.itemName" />
        </el-form-item>
        <el-form-item label="项值" prop="itemValue">
          <el-input v-model="itemForm.itemValue" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="itemForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="itemForm.enabled" />
        </el-form-item>
        <el-form-item label="系统项">
          <el-switch v-model="itemForm.isSystem" :disabled="!!editingItem && editingItem.isSystem" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="itemForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { dictionaryApi, type DictItem, type DictType } from '@/api/dictionary'
import { confirmDanger, extractErrorMsg, msgError, msgSuccess } from '@/utils/message'

const authStore = useAuthStore()

const canListTypes = authStore.hasPermission('system:dict:type:list')
const canListItems = authStore.hasPermission('system:dict:item:list')
const canQueryDictionary = authStore.hasPermission('system:dict:query')
const canAccessDictionaryPage = canListTypes || canListItems || canQueryDictionary
const canAddType = authStore.hasPermission('system:dict:type:add')
const canEditType = authStore.hasPermission('system:dict:type:edit')
const canRemoveType = authStore.hasPermission('system:dict:type:remove')
const canAddItem = authStore.hasPermission('system:dict:item:add')
const canEditItem = authStore.hasPermission('system:dict:item:edit')
const canRemoveItem = authStore.hasPermission('system:dict:item:remove')

const typeLoading = ref(false)
const itemLoading = ref(false)

const typeList = ref<DictType[]>([])
const itemList = ref<DictItem[]>([])

const selectedType = ref<DictType | null>(null)

const typeKeyword = ref('')
const itemKeyword = ref('')

const typePageNo = ref(1)
const typePageSize = ref(10)
const typeTotal = ref(0)

const itemPageNo = ref(1)
const itemPageSize = ref(10)
const itemTotal = ref(0)

const typeDialogVisible = ref(false)
const itemDialogVisible = ref(false)

const editingType = ref<DictType | null>(null)
const editingItem = ref<DictItem | null>(null)

const typeFormRef = ref<FormInstance>()
const itemFormRef = ref<FormInstance>()

const typeForm = ref({
  code: '',
  name: '',
  description: '',
  enabled: true,
  sortOrder: 0,
})

const itemForm = ref({
  itemCode: '',
  itemName: '',
  itemValue: '',
  description: '',
  enabled: true,
  sortOrder: 0,
  isSystem: false,
})

const typeRules: FormRules = {
  code: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
}

const itemRules: FormRules = {
  itemCode: [{ required: true, message: '请输入项编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入项名称', trigger: 'blur' }],
  itemValue: [{ required: true, message: '请输入项值', trigger: 'blur' }],
}

onMounted(async () => {
  await loadTypes()
})

async function loadTypes() {
  if (!canListTypes) {
    typeList.value = []
    typeTotal.value = 0
    selectedType.value = null
    itemList.value = []
    itemTotal.value = 0
    return
  }

  typeLoading.value = true
  try {
    const page = await dictionaryApi.getTypes({
      pageNo: typePageNo.value,
      pageSize: typePageSize.value,
      keyword: typeKeyword.value || undefined,
    })
    typeList.value = page.items
    typeTotal.value = page.total

    if (page.items.length === 0) {
      selectedType.value = null
      itemList.value = []
      itemTotal.value = 0
      return
    }

    const nextSelected = selectedType.value
      ? page.items.find(item => item.id === selectedType.value?.id) ?? page.items[0]
      : page.items[0]

    selectedType.value = nextSelected
    await loadItems()
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '加载字典类型失败'))
  } finally {
    typeLoading.value = false
  }
}

function handleTypeSelect(row: DictType | undefined) {
  if (!row) {
    return
  }
  selectedType.value = row
  itemPageNo.value = 1
  loadItems()
}

async function loadItems() {
  if (!canListItems || !selectedType.value) {
    itemList.value = []
    itemTotal.value = 0
    return
  }

  itemLoading.value = true
  try {
    const typeId = selectedType.value.id
    const page = await dictionaryApi.getItemsByType(typeId, {
      pageNo: itemPageNo.value,
      pageSize: itemPageSize.value,
      keyword: itemKeyword.value || undefined,
    })
    itemList.value = page.items
    itemTotal.value = page.total
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '加载字典项失败'))
  } finally {
    itemLoading.value = false
  }
}

function handleTypeSizeChange(size: number) {
  typePageSize.value = size
  typePageNo.value = 1
  loadTypes()
}

function handleItemSizeChange(size: number) {
  itemPageSize.value = size
  itemPageNo.value = 1
  loadItems()
}

function openTypeDialog(type?: DictType) {
  editingType.value = type ?? null
  typeForm.value = type
    ? {
        code: type.code,
        name: type.name,
        description: type.description ?? '',
        enabled: type.enabled,
        sortOrder: type.sortOrder,
      }
    : {
        code: '',
        name: '',
        description: '',
        enabled: true,
        sortOrder: 0,
      }
  typeDialogVisible.value = true
}

async function saveType() {
  if (!typeFormRef.value) {
    return
  }

  await typeFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      return
    }

    try {
      const payload = {
        ...typeForm.value,
        code: typeForm.value.code.trim().toUpperCase(),
        name: typeForm.value.name.trim(),
        description: typeForm.value.description?.trim(),
      }

      if (editingType.value) {
        await dictionaryApi.updateType(editingType.value.id, payload)
        msgSuccess('字典类型已更新')
      } else {
        await dictionaryApi.createType(payload)
        msgSuccess('字典类型已创建')
      }

      typeDialogVisible.value = false
      await loadTypes()
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '保存字典类型失败'))
    }
  })
}

async function deleteType(type: DictType) {
  try {
    await confirmDanger(`确定删除字典类型“${type.name}”吗？`)
    await dictionaryApi.deleteType(type.id)
    msgSuccess('字典类型已删除')
    if (selectedType.value?.id === type.id) {
      selectedType.value = null
    }
    await loadTypes()
  } catch (error: unknown) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除字典类型失败'))
    }
  }
}

function openItemDialog(item?: DictItem) {
  if (!selectedType.value) {
    msgError('请先选择字典类型')
    return
  }

  editingItem.value = item ?? null
  itemForm.value = item
    ? {
        itemCode: item.itemCode,
        itemName: item.itemName,
        itemValue: item.itemValue,
        description: item.description ?? '',
        enabled: item.enabled,
        sortOrder: item.sortOrder,
        isSystem: item.isSystem,
      }
    : {
        itemCode: '',
        itemName: '',
        itemValue: '',
        description: '',
        enabled: true,
        sortOrder: 0,
        isSystem: false,
      }
  itemDialogVisible.value = true
}

async function saveItem() {
  if (!itemFormRef.value || !selectedType.value) {
    return
  }

  await itemFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      return
    }

    try {
      const payload = {
        ...itemForm.value,
        itemCode: itemForm.value.itemCode.trim().toUpperCase(),
        itemName: itemForm.value.itemName.trim(),
        itemValue: itemForm.value.itemValue.trim(),
        description: itemForm.value.description?.trim(),
      }

      const selected = selectedType.value
      if (!selected) {
        msgError('请先选择字典类型')
        return
      }

      const typeId = selected.id
      if (editingItem.value) {
        await dictionaryApi.updateItem(editingItem.value.id, payload)
        msgSuccess('字典项已更新')
      } else {
        await dictionaryApi.createItem(typeId, payload)
        msgSuccess('字典项已创建')
      }

      itemDialogVisible.value = false
      await loadItems()
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '保存字典项失败'))
    }
  })
}

async function toggleItem(item: DictItem, enabled: boolean) {
  try {
    await dictionaryApi.updateItem(item.id, {
      itemCode: item.itemCode,
      itemName: item.itemName,
      itemValue: item.itemValue,
      description: item.description ?? '',
      enabled,
      sortOrder: item.sortOrder,
      isSystem: item.isSystem
    })
    item.enabled = enabled
    msgSuccess('字典项状态已更新')
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '更新字典项状态失败'))
    await loadItems()
  }
}

async function deleteItem(item: DictItem) {
  try {
    await confirmDanger(`确定删除字典项“${item.itemName}”吗？`)
    await dictionaryApi.deleteItem(item.id)
    msgSuccess('字典项已删除')
    await loadItems()
  } catch (error: unknown) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除字典项失败'))
    }
  }
}
</script>

<style scoped>
.dictionary-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
}

.page-header p {
  margin: 6px 0 0;
  color: #6b7280;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 44% 56%;
  gap: 16px;
}

.left-panel,
.right-panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
