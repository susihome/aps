<template>
  <div class="binding-page">
    <el-empty v-if="!canAccess" description="暂无物料模具绑定权限" />
    <template v-else>
      <div class="page-header">
        <div>
          <h2>物料模具关联</h2>
          <p>维护物料与模具的对应关系及排产参数</p>
        </div>
        <el-button v-if="canAdd" type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          新增绑定
        </el-button>
      </div>

      <div class="page-card">
        <div class="toolbar">
          <el-input
            v-model="keyword"
            placeholder="搜索物料编码/名称、模具编码/名称"
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

        <el-table v-loading="loading" :data="filteredBindings" stripe>
          <el-table-column prop="materialCode" label="物料编码" min-width="120" />
          <el-table-column prop="materialName" label="物料名称" min-width="140" />
          <el-table-column prop="moldCode" label="模具编码" min-width="120" />
          <el-table-column prop="moldName" label="模具名称" min-width="140" />
          <el-table-column prop="priority" label="优先级" width="80" align="center" />
          <el-table-column label="默认" width="70" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isDefault" type="success" size="small">是</el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="首选" width="70" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isPreferred" type="warning" size="small">是</el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="节拍(min)" width="100" align="center">
            <template #default="{ row }">{{ row.cycleTimeMinutes ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="上模(min)" width="100" align="center">
            <template #default="{ row }">{{ row.setupTimeMinutes ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="换模(min)" width="100" align="center">
            <template #default="{ row }">{{ row.changeoverTimeMinutes ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="启用" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="有效期" min-width="200">
            <template #default="{ row }">
              <span v-if="row.validFrom || row.validTo">
                {{ formatDate(row.validFrom) }} ~ {{ formatDate(row.validTo) }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column v-if="canEdit || canRemove" label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canEdit" text type="primary" :disabled="formSaving || deletingId === row.id" @click="openDialog(row)">编辑</el-button>
              <el-button v-if="canRemove" text type="danger" :loading="deletingId === row.id" :disabled="formSaving" @click="removeBinding(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && canList && bindings.length === 0" description="暂无绑定数据" />
        <el-empty v-else-if="!loading && canList && bindings.length > 0 && filteredBindings.length === 0" description="没有匹配的绑定" />
      </div>

      <el-dialog
        v-model="dialogVisible"
        :title="editingBinding ? '编辑绑定' : '新增绑定'"
        width="620px"
        @closed="handleDialogClosed"
        :close-on-click-modal="!formSaving && !isDirty"
        :close-on-press-escape="!formSaving && !isDirty"
        :show-close="!formSaving"
      >
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <div class="form-section-title">关联关系</div>
          <el-form-item label="物料" prop="materialId">
            <el-select
              v-model="form.materialId"
              filterable
              remote
              reserve-keyword
              clearable
              placeholder="请输入物料编码或名称，至少 2 个字"
              :remote-method="searchMaterialOptions"
              :loading="materialSearching"
              :disabled="!!editingBinding"
              style="width: 100%;"
              @visible-change="handleMaterialVisibleChange"
            >
              <el-option
                v-for="m in materialOptions"
                :key="m.id"
                :label="`${m.materialCode} - ${m.materialName}`"
                :value="m.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="模具" prop="moldId">
            <el-select
              v-model="form.moldId"
              filterable
              remote
              reserve-keyword
              clearable
              placeholder="请输入模具编码或名称，至少 2 个字"
              :remote-method="searchMoldOptions"
              :loading="moldSearching"
              :disabled="!!editingBinding"
              style="width: 100%;"
              @visible-change="handleMoldVisibleChange"
            >
              <el-option
                v-for="m in moldOptions"
                :key="m.id"
                :label="`${m.moldCode} - ${m.moldName}`"
                :value="m.id"
              />
            </el-select>
          </el-form-item>

          <div class="form-section-title">排产参数</div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="优先级" prop="priority" label-width="70px">
                <el-input-number v-model="form.priority" :min="0" controls-position="right" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="默认" label-width="50px">
                <el-switch v-model="form.isDefault" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="首选" label-width="50px">
                <el-switch v-model="form.isPreferred" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="节拍" prop="cycleTimeMinutes" label-width="70px">
                <el-input-number v-model="form.cycleTimeMinutes" :min="1" controls-position="right" placeholder="分钟" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="上模" prop="setupTimeMinutes" label-width="50px">
                <el-input-number v-model="form.setupTimeMinutes" :min="0" controls-position="right" placeholder="分钟" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="换模" prop="changeoverTimeMinutes" label-width="50px">
                <el-input-number v-model="form.changeoverTimeMinutes" :min="0" controls-position="right" placeholder="分钟" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-section-title">有效期与状态</div>
          <el-form-item label="有效期">
            <el-date-picker
              v-model="form.validRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="生效时间"
              end-placeholder="失效时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%;"
            />
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
          <el-button type="primary" @click="saveBinding" :loading="formSaving" :disabled="!isDirty">保存</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  materialMoldBindingApi,
  type MaterialMoldBinding,
  type CreateMaterialMoldBindingRequest,
  type UpdateMaterialMoldBindingRequest,
} from '@/api/materialMoldBinding'
import { materialApi, type Material } from '@/api/material'
import { moldApi, type Mold } from '@/api/mold'
import { useAuthStore } from '@/stores/auth'
import { confirmDanger, extractErrorMsg, msgError, msgSuccess } from '@/utils/message'

const authStore = useAuthStore()
const canList = authStore.hasPermission('basedata:materialmold:list')
const canAdd = authStore.hasPermission('basedata:materialmold:add')
const canEdit = authStore.hasPermission('basedata:materialmold:edit')
const canRemove = authStore.hasPermission('basedata:materialmold:remove')
const canAccess = canList || canAdd || canEdit || canRemove

const loading = ref(false)
const formSaving = ref(false)
const deletingId = ref<string | null>(null)
const bindings = ref<MaterialMoldBinding[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const editingBinding = ref<MaterialMoldBinding | null>(null)
const formRef = ref<FormInstance>()
const form = ref(createForm())
const initialFormSnapshot = ref('')

const materialOptions = ref<Material[]>([])
const moldOptions = ref<Mold[]>([])
const materialSearching = ref(false)
const moldSearching = ref(false)

const rules: FormRules = {
  materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
  moldId: [{ required: true, message: '请选择模具', trigger: 'change' }],
  priority: [{ type: 'number', min: 0, message: '优先级不能小于0', trigger: 'change' }],
  cycleTimeMinutes: [{ type: 'number', min: 1, message: '节拍时间必须大于0', trigger: 'change' }],
  setupTimeMinutes: [{ type: 'number', min: 0, message: '上模时间不能小于0', trigger: 'change' }],
  changeoverTimeMinutes: [{ type: 'number', min: 0, message: '换模时间不能小于0', trigger: 'change' }],
  remark: [{ max: 500, message: '备注长度不能超过500', trigger: 'blur' }],
}

const filteredBindings = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return bindings.value
  return bindings.value.filter(b => {
    return b.materialCode.toLowerCase().includes(kw)
      || b.materialName.toLowerCase().includes(kw)
      || b.moldCode.toLowerCase().includes(kw)
      || b.moldName.toLowerCase().includes(kw)
  })
})

const isDirty = computed(() => initialFormSnapshot.value !== '' && initialFormSnapshot.value !== serializeForm())

onMounted(async () => {
  await loadBindings()
})

function createForm() {
  return {
    materialId: '' as string,
    moldId: '' as string,
    priority: 0,
    isDefault: false,
    isPreferred: false,
    cycleTimeMinutes: undefined as number | undefined,
    setupTimeMinutes: undefined as number | undefined,
    changeoverTimeMinutes: undefined as number | undefined,
    enabled: true,
    validRange: null as [string, string] | null,
    remark: '',
  }
}

function handleSearch() {
  keyword.value = keyword.value.trim()
}

function serializeForm() {
  return JSON.stringify({
    materialId: form.value.materialId,
    moldId: form.value.moldId,
    priority: form.value.priority,
    isDefault: form.value.isDefault,
    isPreferred: form.value.isPreferred,
    cycleTimeMinutes: form.value.cycleTimeMinutes ?? null,
    setupTimeMinutes: form.value.setupTimeMinutes ?? null,
    changeoverTimeMinutes: form.value.changeoverTimeMinutes ?? null,
    enabled: form.value.enabled,
    validRange: form.value.validRange,
    remark: form.value.remark.trim(),
  })
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '不限'
  return dateStr.replace('T', ' ').slice(0, 16)
}

async function handleCancel() {
  if (formSaving.value) return
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
  editingBinding.value = null
  initialFormSnapshot.value = ''
  form.value = createForm()
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}

async function loadBindings() {
  if (!canList) {
    bindings.value = []
    return
  }
  loading.value = true
  try {
    bindings.value = await materialMoldBindingApi.getAll()
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '加载绑定列表失败'))
  } finally {
    loading.value = false
  }
}

function mergeMaterialOptions(items: Material[]) {
  const map = new Map(materialOptions.value.map(item => [item.id, item]))
  items.forEach(item => map.set(item.id, item))
  materialOptions.value = Array.from(map.values())
}

function mergeMoldOptions(items: Mold[]) {
  const map = new Map(moldOptions.value.map(item => [item.id, item]))
  items.forEach(item => map.set(item.id, item))
  moldOptions.value = Array.from(map.values())
}

async function searchMaterialOptions(keyword: string) {
  const normalizedKeyword = keyword.trim()
  if (normalizedKeyword.length < 2) {
    materialOptions.value = editingBinding.value && form.value.materialId
      ? materialOptions.value.filter(item => item.id === form.value.materialId)
      : []
    return
  }
  materialSearching.value = true
  try {
    const materials = await materialApi.getAll(normalizedKeyword, 20)
    mergeMaterialOptions(materials)
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '搜索物料失败'))
  } finally {
    materialSearching.value = false
  }
}

async function searchMoldOptions(keyword: string) {
  const normalizedKeyword = keyword.trim()
  if (normalizedKeyword.length < 2) {
    moldOptions.value = editingBinding.value && form.value.moldId
      ? moldOptions.value.filter(item => item.id === form.value.moldId)
      : []
    return
  }
  moldSearching.value = true
  try {
    const molds = await moldApi.getAll(normalizedKeyword, 20)
    mergeMoldOptions(molds)
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '搜索模具失败'))
  } finally {
    moldSearching.value = false
  }
}

function handleMaterialVisibleChange(visible: boolean) {
  if (!visible && !editingBinding.value) {
    materialOptions.value = form.value.materialId
      ? materialOptions.value.filter(item => item.id === form.value.materialId)
      : []
  }
}

function handleMoldVisibleChange(visible: boolean) {
  if (!visible && !editingBinding.value) {
    moldOptions.value = form.value.moldId
      ? moldOptions.value.filter(item => item.id === form.value.moldId)
      : []
  }
}

function ensureSelectedOptions(binding?: MaterialMoldBinding) {
  if (!binding) {
    materialOptions.value = []
    moldOptions.value = []
    return
  }
  mergeMaterialOptions([
    {
      id: binding.materialId,
      materialCode: binding.materialCode,
      materialName: binding.materialName,
      specification: null,
      unit: null,
      enabled: true,
      remark: null,
      createTime: '',
      updateTime: '',
      colorCode: null,
      rawMaterialType: null,
      defaultLotSize: null,
      minLotSize: null,
      maxLotSize: null,
      allowDelay: null,
      abcClassification: null,
      productGroup: null,
    },
  ])
  mergeMoldOptions([
    {
      id: binding.moldId,
      moldCode: binding.moldCode,
      moldName: binding.moldName,
      cavityCount: null,
      status: null,
      enabled: true,
      remark: null,
      createTime: '',
      updateTime: '',
    },
  ])
}

function openDialog(binding?: MaterialMoldBinding) {
  editingBinding.value = binding ?? null
  formRef.value?.clearValidate()
  ensureSelectedOptions(binding)
  if (binding) {
    const validRange: [string, string] | null =
      binding.validFrom && binding.validTo ? [binding.validFrom, binding.validTo]
        : binding.validFrom ? [binding.validFrom, '']
          : binding.validTo ? ['', binding.validTo]
            : null
    form.value = {
      materialId: binding.materialId,
      moldId: binding.moldId,
      priority: binding.priority,
      isDefault: binding.isDefault,
      isPreferred: binding.isPreferred,
      cycleTimeMinutes: binding.cycleTimeMinutes ?? undefined,
      setupTimeMinutes: binding.setupTimeMinutes ?? undefined,
      changeoverTimeMinutes: binding.changeoverTimeMinutes ?? undefined,
      enabled: binding.enabled,
      validRange,
      remark: binding.remark ?? '',
    }
  } else {
    form.value = createForm()
  }
  initialFormSnapshot.value = serializeForm()
  dialogVisible.value = true
}

async function saveBinding() {
  if (!formRef.value || formSaving.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    formSaving.value = true
    try {
      const validFrom = form.value.validRange?.[0] || null
      const validTo = form.value.validRange?.[1] || null

      if (editingBinding.value) {
        const payload: UpdateMaterialMoldBindingRequest = {
          priority: form.value.priority,
          isDefault: form.value.isDefault,
          isPreferred: form.value.isPreferred,
          cycleTimeMinutes: form.value.cycleTimeMinutes ?? null,
          setupTimeMinutes: form.value.setupTimeMinutes ?? null,
          changeoverTimeMinutes: form.value.changeoverTimeMinutes ?? null,
          enabled: form.value.enabled,
          validFrom,
          validTo,
          remark: form.value.remark.trim() || null,
        }
        await materialMoldBindingApi.update(editingBinding.value.id, payload)
        msgSuccess('绑定已更新')
      } else {
        const payload: CreateMaterialMoldBindingRequest = {
          materialId: form.value.materialId,
          moldId: form.value.moldId,
          priority: form.value.priority,
          isDefault: form.value.isDefault,
          isPreferred: form.value.isPreferred,
          cycleTimeMinutes: form.value.cycleTimeMinutes ?? null,
          setupTimeMinutes: form.value.setupTimeMinutes ?? null,
          changeoverTimeMinutes: form.value.changeoverTimeMinutes ?? null,
          enabled: form.value.enabled,
          validFrom,
          validTo,
          remark: form.value.remark.trim() || null,
        }
        await materialMoldBindingApi.create(payload)
        msgSuccess('绑定已创建')
      }

      dialogVisible.value = false
      initialFormSnapshot.value = ''
      await loadBindings()
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '保存绑定失败'))
    } finally {
      formSaving.value = false
    }
  })
}

async function removeBinding(binding: MaterialMoldBinding) {
  if (deletingId.value) return

  try {
    await confirmDanger(`确定删除"${binding.materialCode} ↔ ${binding.moldCode}"的绑定关系吗？`)
    deletingId.value = binding.id
    await materialMoldBindingApi.delete(binding.id)
    msgSuccess('绑定已删除')
    await loadBindings()
  } catch (error: unknown) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除绑定失败'))
    }
  } finally {
    deletingId.value = null
  }
}
</script>

<style scoped>
.binding-page {
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
  width: 380px;
}

.text-muted {
  color: #c0c4cc;
}

.form-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  border-left: 3px solid #409eff;
  padding-left: 8px;
  margin: 16px 0 12px;
}

.form-section-title:first-child {
  margin-top: 0;
}
</style>
