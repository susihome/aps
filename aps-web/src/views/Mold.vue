<template>
  <div class="mold-page">
    <el-empty v-if="!canAccess" description="暂无模具管理权限" />
    <template v-else>
      <div class="page-header">
        <div>
          <h2>模具管理</h2>
          <p>维护注塑生产所需的模具基础数据</p>
        </div>
        <el-button v-if="canAdd" type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          新增模具
        </el-button>
      </div>

      <div class="page-card">
        <div class="toolbar">
          <el-input
            v-model="keyword"
            placeholder="搜索模具编码/名称/状态"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch" :disabled="loading">查询</el-button>
            </template>
          </el-input>
        </div>

        <el-table v-loading="loading" :data="filteredMolds" stripe>
          <el-table-column prop="moldCode" label="模具编码" min-width="140" />
          <el-table-column prop="moldName" label="模具名称" min-width="180" />
          <el-table-column prop="cavityCount" label="模穴数" width="100" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="启用" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column v-if="canEdit || canRemove" label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canEdit" text type="primary" :disabled="formSaving || deletingId === row.id" @click="openDialog(row)">编辑</el-button>
              <el-button v-if="canRemove" text type="danger" :loading="deletingId === row.id" :disabled="formSaving" @click="removeMold(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && canList && molds.length === 0" description="暂无模具数据" />
        <el-empty v-else-if="!loading && canList && molds.length > 0 && filteredMolds.length === 0" description="没有匹配的模具" />
      </div>

      <el-dialog v-model="dialogVisible" :title="editingMold ? '编辑模具' : '新增模具'" width="560px" @closed="handleDialogClosed" :close-on-click-modal="!formSaving && !isDirty" :close-on-press-escape="!formSaving && !isDirty" :show-close="!formSaving">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="模具编码" prop="moldCode">
            <el-input v-model="form.moldCode" :disabled="!!editingMold" />
          </el-form-item>
          <el-form-item label="模具名称" prop="moldName">
            <el-input v-model="form.moldName" />
          </el-form-item>
          <el-form-item label="模穴数">
            <el-input-number v-model="form.cavityCount" :min="1" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" placeholder="请选择状态" clearable>
              <el-option
                v-for="item in moldStatusItems"
                :key="item.itemCode"
                :label="item.itemName"
                :value="item.itemCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="form.enabled" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="handleCancel" :disabled="formSaving">取消</el-button>
          <el-button type="primary" @click="saveMold" :loading="formSaving" :disabled="!isDirty">保存</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { moldApi, type CreateMoldRequest, type Mold, type UpdateMoldRequest } from '@/api/mold'
import { dictionaryApi, type DictItem } from '@/api/dictionary'
import { useAuthStore } from '@/stores/auth'
import { confirmDanger, extractErrorMsg, msgError, msgSuccess } from '@/utils/message'

const moldStatusItems = ref<DictItem[]>([])
const moldStatusLabelMap = ref<Record<string, string>>({})
const moldStatusTagMap = ref<Record<string, string>>({})

const authStore = useAuthStore()
const canList = authStore.hasPermission('basedata:mold:list')
const canAdd = authStore.hasPermission('basedata:mold:add')
const canEdit = authStore.hasPermission('basedata:mold:edit')
const canRemove = authStore.hasPermission('basedata:mold:remove')
const canAccess = canList || canAdd || canEdit || canRemove

const loading = ref(false)
const formSaving = ref(false)
const deletingId = ref<string | null>(null)
const molds = ref<Mold[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const editingMold = ref<Mold | null>(null)
const formRef = ref<FormInstance>()
const form = ref(createForm())
const initialFormSnapshot = ref('')

const rules: FormRules = {
  moldCode: [
    { required: true, message: '请输入模具编码', trigger: 'blur' },
    { max: 64, message: '模具编码长度不能超过64', trigger: 'blur' },
  ],
  moldName: [
    { required: true, message: '请输入模具名称', trigger: 'blur' },
    { max: 120, message: '模具名称长度不能超过120', trigger: 'blur' },
  ],
  cavityCount: [{ type: 'number', min: 1, message: '模穴数必须大于0', trigger: 'change' }],
  status: [{ max: 20, message: '状态长度不能超过20', trigger: 'blur' }],
  remark: [{ max: 500, message: '备注长度不能超过500', trigger: 'blur' }],
}

const filteredMolds = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()
  if (!normalizedKeyword) {
    return molds.value
  }
  return molds.value.filter(item => {
    const moldCode = item.moldCode.toLowerCase()
    const moldName = item.moldName.toLowerCase()
    const status = item.status?.toLowerCase() ?? ''
    const statusLabel = getStatusLabel(item.status).toLowerCase()
    return moldCode.includes(normalizedKeyword)
      || moldName.includes(normalizedKeyword)
      || status.includes(normalizedKeyword)
      || statusLabel.includes(normalizedKeyword)
  })
})

const isDirty = computed(() => initialFormSnapshot.value !== '' && initialFormSnapshot.value !== serializeForm())

onMounted(async () => {
  await Promise.all([loadMolds(), loadMoldStatusDict()])
})

function createForm() {
  return {
    moldCode: '',
    moldName: '',
    cavityCount: undefined as number | undefined,
    status: 'ACTIVE',
    enabled: true,
    remark: '',
  }
}

function handleSearch() {
  keyword.value = keyword.value.trim()
}

function serializeForm() {
  return JSON.stringify({
    moldCode: form.value.moldCode.trim(),
    moldName: form.value.moldName.trim(),
    cavityCount: form.value.cavityCount ?? null,
    status: form.value.status.trim(),
    enabled: form.value.enabled,
    remark: form.value.remark.trim(),
  })
}

async function handleCancel() {
  if (formSaving.value) {
    return
  }
  if (!isDirty.value) {
    dialogVisible.value = false
    return
  }
  try {
    await confirmDanger('当前内容尚未保存，确认放弃修改吗？', '放弃修改')
    dialogVisible.value = false
  } catch (error: unknown) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '关闭弹窗失败'))
    }
  }
}

function handleDialogClosed() {
  editingMold.value = null
  initialFormSnapshot.value = ''
  form.value = createForm()
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}

async function loadMoldStatusDict() {
  try {
    moldStatusItems.value = await dictionaryApi.getEnabledItemsByTypeCode('MOLD_STATUS')
    moldStatusLabelMap.value = Object.fromEntries(
      moldStatusItems.value.map(item => [item.itemCode, item.itemName])
    )
    moldStatusTagMap.value = Object.fromEntries(
      moldStatusItems.value.map(item => [item.itemCode, item.itemValue])
    )
  } catch {
    moldStatusItems.value = []
  }
}

function getStatusLabel(status: string | null): string {
  if (!status) return '-'
  return moldStatusLabelMap.value[status] ?? status
}

function statusTagType(status: string | null): 'success' | 'warning' | 'info' | 'danger' {
  if (!status) return 'info'
  const tag = moldStatusTagMap.value[status]
  return (tag as 'success' | 'warning' | 'info' | 'danger') ?? 'info'
}

async function loadMolds() {
  if (!canList) {
    molds.value = []
    return
  }
  loading.value = true
  try {
    molds.value = await moldApi.getAll()
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '加载模具失败'))
  } finally {
    loading.value = false
  }
}

function openDialog(mold?: Mold) {
  editingMold.value = mold ?? null
  formRef.value?.clearValidate()
  form.value = mold
    ? {
        moldCode: mold.moldCode,
        moldName: mold.moldName,
        cavityCount: mold.cavityCount ?? undefined,
        status: mold.status ?? '',
        enabled: mold.enabled,
        remark: mold.remark ?? '',
      }
    : createForm()
  initialFormSnapshot.value = serializeForm()
  dialogVisible.value = true
}

async function saveMold() {
  if (!formRef.value || formSaving.value) {
    return
  }

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      return
    }

    formSaving.value = true
    try {
      const payload: CreateMoldRequest = {
        moldCode: form.value.moldCode.trim().toUpperCase(),
        moldName: form.value.moldName.trim(),
        cavityCount: form.value.cavityCount ?? null,
        status: form.value.status.trim() || null,
        enabled: form.value.enabled,
        remark: form.value.remark.trim() || null,
      }

      if (editingMold.value) {
        const updatePayload: UpdateMoldRequest = {
          moldName: payload.moldName,
          cavityCount: payload.cavityCount,
          status: payload.status,
          enabled: payload.enabled,
          remark: payload.remark,
        }
        await moldApi.update(editingMold.value.id, updatePayload)
        msgSuccess('模具已更新')
      } else {
        await moldApi.create(payload)
        msgSuccess('模具已创建')
      }

      dialogVisible.value = false
      initialFormSnapshot.value = ''
      await loadMolds()
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '保存模具失败'))
    } finally {
      formSaving.value = false
    }
  })
}

async function removeMold(mold: Mold) {
  if (deletingId.value) {
    return
  }

  try {
    await confirmDanger(`确定删除模具“${mold.moldName}”吗？`)
    deletingId.value = mold.id
    await moldApi.delete(mold.id)
    msgSuccess('模具已删除')
    await loadMolds()
  } catch (error: unknown) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除模具失败'))
    }
  } finally {
    deletingId.value = null
  }
}
</script>

<style scoped>
.mold-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px;
}

.page-header p {
  margin: 0;
  color: #606266;
}

.page-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.search-input {
  width: 320px;
}
</style>
