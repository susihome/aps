<template>
  <div class="material-page">
    <el-empty v-if="!canAccess" description="暂无物料管理权限" />
    <template v-else>
      <div class="page-header">
        <div>
          <h2>物料管理</h2>
          <p>维护注塑生产所需的物料基础数据</p>
        </div>
        <div class="header-actions">
          <el-button v-if="canList" :loading="exporting" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出模板.xlsx
          </el-button>
          <el-button v-if="canImport" :loading="importing" @click="triggerImport">
            <el-icon><Upload /></el-icon>
            导入
          </el-button>
          <el-button v-if="canAdd" type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>
            新增物料
          </el-button>
          <input
            ref="fileInputRef"
            type="file"
            accept=".csv,.xlsx,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            class="hidden-file-input"
            @change="handleFileChange"
          />
        </div>
      </div>

      <div class="page-card">
        <div class="toolbar">
          <el-input
            v-model="keyword"
            placeholder="搜索物料编码/名称/规格"
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

        <el-table v-loading="loading" :data="filteredMaterials" stripe row-key="id">
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="expand-detail">
                <el-descriptions :column="3" border size="small">
                  <el-descriptions-item label="颜色">{{ getColorLabel(row.colorCode) }}</el-descriptions-item>
                  <el-descriptions-item label="原料类型">{{ getRawTypeLabel(row.rawMaterialType) }}</el-descriptions-item>
                  <el-descriptions-item label="产品组">{{ row.productGroup ?? '-' }}</el-descriptions-item>
                  <el-descriptions-item label="ABC 分类">{{ row.abcClassification ?? '-' }}</el-descriptions-item>
                  <el-descriptions-item label="批量约束">{{ formatLotSize(row) }}</el-descriptions-item>
                  <el-descriptions-item label="可否延期">
                    <el-tag v-if="row.allowDelay === true" type="success" size="small">允许延期</el-tag>
                    <el-tag v-else-if="row.allowDelay === false" type="danger" size="small">不允许延期</el-tag>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="备注" :span="3">{{ row.remark ?? '-' }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="materialCode" label="物料编码" min-width="140" />
          <el-table-column prop="materialName" label="物料名称" min-width="160" />
          <el-table-column prop="specification" label="规格" min-width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="canEdit || canRemove" label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canEdit" text type="primary" :disabled="formSaving || deletingId === row.id" @click="openDialog(row)">编辑</el-button>
              <el-button v-if="canRemove" text type="danger" :loading="deletingId === row.id" :disabled="formSaving" @click="removeMaterial(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && canList && materials.length === 0" description="暂无物料数据" />
        <el-empty v-else-if="!loading && canList && materials.length > 0 && filteredMaterials.length === 0" description="没有匹配的物料" />
      </div>

      <el-dialog v-model="dialogVisible" :title="editingMaterial ? '编辑物料' : '新增物料'" width="620px" @closed="handleDialogClosed" :close-on-click-modal="!formSaving && !isDirty" :close-on-press-escape="!formSaving && !isDirty" :show-close="!formSaving">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <div class="form-section-title">基本信息</div>
          <el-form-item label="物料编码" prop="materialCode">
            <el-input v-model="form.materialCode" :disabled="!!editingMaterial" />
          </el-form-item>
          <el-form-item label="物料名称" prop="materialName">
            <el-input v-model="form.materialName" />
          </el-form-item>
          <el-form-item label="规格">
            <el-input v-model="form.specification" />
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="form.unit" />
          </el-form-item>
          <el-form-item label="产品组">
            <el-input v-model="form.productGroup" placeholder="同组产品合并排产" />
          </el-form-item>
          <el-form-item label="ABC分类">
            <el-select v-model="form.abcClassification" clearable placeholder="请选择" style="width: 120px;">
              <el-option label="A 类" value="A" />
              <el-option label="B 类" value="B" />
              <el-option label="C 类" value="C" />
            </el-select>
          </el-form-item>

          <div class="form-section-title">注塑工艺</div>
          <el-form-item label="颜色">
            <el-select v-model="form.colorCode" clearable placeholder="请选择颜色">
              <el-option v-for="opt in colorOptions" :key="opt.itemCode" :label="opt.itemName" :value="opt.itemCode" />
            </el-select>
          </el-form-item>
          <el-form-item label="原料类型">
            <el-select v-model="form.rawMaterialType" clearable placeholder="请选择原料类型">
              <el-option v-for="opt in rawMaterialOptions" :key="opt.itemCode" :label="opt.itemName" :value="opt.itemCode" />
            </el-select>
          </el-form-item>

          <div class="form-section-title">批量约束</div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="默认批量" prop="defaultLotSize" label-width="80px">
                <el-input-number v-model="form.defaultLotSize" :min="1" controls-position="right" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="最小批" prop="minLotSize" label-width="70px">
                <el-input-number v-model="form.minLotSize" :min="1" controls-position="right" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="最大批" prop="maxLotSize" label-width="70px">
                <el-input-number v-model="form.maxLotSize" :min="1" controls-position="right" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-section-title">排产策略</div>
          <el-form-item label="可否延期">
            <el-select v-model="form.allowDelay" clearable placeholder="未设置" style="width: 160px;">
              <el-option label="允许延期" :value="true" />
              <el-option label="不允许延期" :value="false" />
            </el-select>
          </el-form-item>

          <div class="form-section-title">备注与状态</div>
          <el-form-item label="启用状态">
            <el-switch v-model="form.enabled" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="handleCancel" :disabled="formSaving">取消</el-button>
          <el-button type="primary" @click="saveMaterial" :loading="formSaving" :disabled="!isDirty">保存</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="importFailureDialogVisible" title="导入失败明细" width="760px">
        <el-alert
          :type="importFailures.length > 0 ? 'warning' : 'info'"
          show-icon
          :closable="false"
          :title="importFailureDialogTitle"
        />
        <div class="import-failure-actions" v-if="importErrorFileName && importErrorFileToken">
          <el-button type="primary" @click="downloadImportErrorFile">下载错误文件</el-button>
        </div>
        <el-table :data="importFailures" stripe class="import-failure-table">
          <el-table-column prop="rowNumber" label="行号" width="100" />
          <el-table-column prop="columnName" label="字段" width="160" />
          <el-table-column prop="message" label="失败原因" min-width="320" />
        </el-table>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { Download, Plus, Upload } from '@element-plus/icons-vue'
import { materialApi, type CreateMaterialRequest, type Material, type MaterialImportFailure, type UpdateMaterialRequest } from '@/api/material'
import { dictionaryApi, type DictItem } from '@/api/dictionary'
import { useAuthStore } from '@/stores/auth'
import { confirmDanger, extractErrorMsg, msgError, msgSuccess } from '@/utils/message'

const authStore = useAuthStore()
const canList = authStore.hasPermission('basedata:material:list')
const canAdd = authStore.hasPermission('basedata:material:add')
const canEdit = authStore.hasPermission('basedata:material:edit')
const canRemove = authStore.hasPermission('basedata:material:remove')
const canImport = canAdd && canEdit
const canAccess = canList || canAdd || canEdit || canRemove

const loading = ref(false)
const formSaving = ref(false)
const deletingId = ref<string | null>(null)
const exporting = ref(false)
const importing = ref(false)
const materials = ref<Material[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const editingMaterial = ref<Material | null>(null)
const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement>()
const form = ref(createForm())
const initialFormSnapshot = ref('')
const importFailureDialogVisible = ref(false)
const importFailures = ref<MaterialImportFailure[]>([])
const importErrorFileName = ref<string | null>(null)
const importErrorFileToken = ref<string | null>(null)
const colorOptions = ref<DictItem[]>([])
const rawMaterialOptions = ref<DictItem[]>([])
const colorLabelMap = ref<Record<string, string>>({})
const rawTypeLabelMap = ref<Record<string, string>>({})

const rules: FormRules = {
  materialCode: [
    { required: true, message: '请输入物料编码', trigger: 'blur' },
    { max: 64, message: '物料编码长度不能超过64', trigger: 'blur' },
  ],
  materialName: [
    { required: true, message: '请输入物料名称', trigger: 'blur' },
    { max: 120, message: '物料名称长度不能超过120', trigger: 'blur' },
  ],
  specification: [{ max: 255, message: '规格长度不能超过255', trigger: 'blur' }],
  unit: [{ max: 32, message: '单位长度不能超过32', trigger: 'blur' }],
  remark: [{ max: 500, message: '备注长度不能超过500', trigger: 'blur' }],
  defaultLotSize: [{ type: 'number', min: 1, message: '默认批量必须大于0', trigger: 'blur' }],
  minLotSize: [{ type: 'number', min: 1, message: '最小批量必须大于0', trigger: 'blur' }],
  maxLotSize: [{ type: 'number', min: 1, message: '最大批量必须大于0', trigger: 'blur' }],
}

const filteredMaterials = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()
  if (!normalizedKeyword) {
    return materials.value
  }
  return materials.value.filter(item => {
    const materialCode = item.materialCode.toLowerCase()
    const materialName = item.materialName.toLowerCase()
    const specification = item.specification?.toLowerCase() ?? ''
    return materialCode.includes(normalizedKeyword)
      || materialName.includes(normalizedKeyword)
      || specification.includes(normalizedKeyword)
  })
})

const isDirty = computed(() => initialFormSnapshot.value !== '' && initialFormSnapshot.value !== serializeForm())
const importFailureDialogTitle = computed(() => {
  if (importFailures.value.length === 0) {
    return '导入已完成'
  }
  return `部分数据已导入，仍有 ${importFailures.value.length} 条失败，请下载错误文件修正后重试`
})

onMounted(async () => {
  await Promise.all([loadMaterials(), loadDictOptions()])
})

async function loadDictOptions() {
  try {
    const [colors, rawTypes] = await Promise.all([
      dictionaryApi.getEnabledItemsByTypeCode('MATERIAL_COLOR'),
      dictionaryApi.getEnabledItemsByTypeCode('MATERIAL_RAW_TYPE'),
    ])
    colorOptions.value = colors
    rawMaterialOptions.value = rawTypes
    colorLabelMap.value = Object.fromEntries(colors.map(i => [i.itemCode, i.itemName]))
    rawTypeLabelMap.value = Object.fromEntries(rawTypes.map(i => [i.itemCode, i.itemName]))
  } catch {
    /* 字典加载失败不影响主功能 */
  }
}

function getColorLabel(code: string | null): string {
  if (!code) return '-'
  return colorLabelMap.value[code] ?? code
}

function getRawTypeLabel(code: string | null): string {
  if (!code) return '-'
  return rawTypeLabelMap.value[code] ?? code
}

function formatLotSize(row: Material): string {
  const def = row.defaultLotSize
  const min = row.minLotSize
  const max = row.maxLotSize
  if (def == null && min == null && max == null) return '-'
  const parts: string[] = []
  if (def != null) parts.push(`默认 ${def}`)
  if (min != null && max != null) {
    parts.push(`范围 ${min}~${max}`)
  } else if (min != null) {
    parts.push(`最小 ${min}`)
  } else if (max != null) {
    parts.push(`最大 ${max}`)
  }
  return parts.join(' / ')
}

function createForm() {
  return {
    materialCode: '',
    materialName: '',
    specification: '',
    unit: '',
    enabled: true,
    remark: '',
    colorCode: null as string | null,
    rawMaterialType: null as string | null,
    defaultLotSize: null as number | null,
    minLotSize: null as number | null,
    maxLotSize: null as number | null,
    allowDelay: null as boolean | null,
    abcClassification: null as string | null,
    productGroup: '',
  }
}

function handleSearch() {
  keyword.value = keyword.value.trim()
}

function serializeForm() {
  return JSON.stringify({
    materialCode: form.value.materialCode.trim(),
    materialName: form.value.materialName.trim(),
    specification: form.value.specification.trim(),
    unit: form.value.unit.trim(),
    enabled: form.value.enabled,
    remark: form.value.remark.trim(),
    colorCode: form.value.colorCode,
    rawMaterialType: form.value.rawMaterialType,
    defaultLotSize: form.value.defaultLotSize,
    minLotSize: form.value.minLotSize,
    maxLotSize: form.value.maxLotSize,
    allowDelay: form.value.allowDelay,
    abcClassification: form.value.abcClassification,
    productGroup: form.value.productGroup.trim(),
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
  editingMaterial.value = null
  initialFormSnapshot.value = ''
  form.value = createForm()
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}

async function loadMaterials() {
  if (!canList) {
    materials.value = []
    return
  }
  loading.value = true
  try {
    materials.value = await materialApi.getAll()
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '加载物料失败'))
  } finally {
    loading.value = false
  }
}

function openDialog(material?: Material) {
  editingMaterial.value = material ?? null
  formRef.value?.clearValidate()
  form.value = material
    ? {
        materialCode: material.materialCode,
        materialName: material.materialName,
        specification: material.specification ?? '',
        unit: material.unit ?? '',
        enabled: material.enabled,
        remark: material.remark ?? '',
        colorCode: material.colorCode ?? null,
        rawMaterialType: material.rawMaterialType ?? null,
        defaultLotSize: material.defaultLotSize ?? null,
        minLotSize: material.minLotSize ?? null,
        maxLotSize: material.maxLotSize ?? null,
        allowDelay: material.allowDelay ?? null,
        abcClassification: material.abcClassification ?? null,
        productGroup: material.productGroup ?? '',
      }
    : createForm()
  initialFormSnapshot.value = serializeForm()
  dialogVisible.value = true
}

async function saveMaterial() {
  if (!formRef.value || formSaving.value) {
    return
  }

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      return
    }

    formSaving.value = true
    try {
      const payload: CreateMaterialRequest = {
        materialCode: form.value.materialCode.trim().toUpperCase(),
        materialName: form.value.materialName.trim(),
        specification: form.value.specification.trim() || null,
        unit: form.value.unit.trim() || null,
        enabled: form.value.enabled,
        remark: form.value.remark.trim() || null,
        colorCode: form.value.colorCode || null,
        rawMaterialType: form.value.rawMaterialType || null,
        defaultLotSize: form.value.defaultLotSize,
        minLotSize: form.value.minLotSize,
        maxLotSize: form.value.maxLotSize,
        allowDelay: form.value.allowDelay,
        abcClassification: form.value.abcClassification || null,
        productGroup: form.value.productGroup.trim() || null,
      }

      if (editingMaterial.value) {
        const updatePayload: UpdateMaterialRequest = {
          materialName: payload.materialName,
          specification: payload.specification,
          unit: payload.unit,
          enabled: payload.enabled,
          remark: payload.remark,
          colorCode: form.value.colorCode ?? '',
          rawMaterialType: form.value.rawMaterialType ?? '',
          defaultLotSize: payload.defaultLotSize,
          minLotSize: payload.minLotSize,
          maxLotSize: payload.maxLotSize,
          allowDelay: payload.allowDelay,
          abcClassification: form.value.abcClassification ?? '',
          productGroup: form.value.productGroup.trim(),
        }
        await materialApi.update(editingMaterial.value.id, updatePayload)
        msgSuccess('物料已更新')
      } else {
        await materialApi.create(payload)
        msgSuccess('物料已创建')
      }

      dialogVisible.value = false
      initialFormSnapshot.value = ''
      await loadMaterials()
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '保存物料失败'))
    } finally {
      formSaving.value = false
    }
  })
}

async function removeMaterial(material: Material) {
  if (deletingId.value) {
    return
  }

  try {
    await confirmDanger(`确定删除物料“${material.materialName}”吗？`)
    deletingId.value = material.id
    await materialApi.delete(material.id)
    msgSuccess('物料已删除')
    await loadMaterials()
  } catch (error: unknown) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除物料失败'))
    }
  } finally {
    deletingId.value = null
  }
}

function triggerImport() {
  if (importing.value) {
    return
  }
  fileInputRef.value?.click()
}

async function handleExport() {
  if (exporting.value) {
    return
  }
  exporting.value = true
  try {
    const response = await materialApi.exportFile('xlsx')
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const disposition = String(response.headers['content-disposition'] ?? '')
    const matched = disposition.match(/filename="?([^"]+)"?/)
    const fileName = matched?.[1] ?? 'materials-template.xlsx'
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    msgSuccess('物料模板已导出')
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '导出物料模板失败'))
  } finally {
    exporting.value = false
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  importing.value = true
  importFailures.value = []
  importErrorFileName.value = null
  importErrorFileToken.value = null
  try {
    const result = await materialApi.importFile(file)
    if (result.failedCount > 0) {
      importFailures.value = result.failures
      importErrorFileName.value = result.errorFileName
      importErrorFileToken.value = result.errorFileToken
      importFailureDialogVisible.value = true
      msgSuccess(`导入完成：成功 ${result.createdCount + result.updatedCount} 条，失败 ${result.failedCount} 条`)
    } else {
      msgSuccess(`导入完成：共 ${result.totalCount} 条，新增 ${result.createdCount} 条，更新 ${result.updatedCount} 条`)
    }
    await loadMaterials()
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '导入物料失败'))
  } finally {
    importing.value = false
    input.value = ''
  }
}

function downloadImportErrorFile() {
  if (!importErrorFileName.value || !importErrorFileToken.value) {
    return
  }
  materialApi.downloadImportErrorFile(importErrorFileToken.value).then(response => {
    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = importErrorFileName.value as string
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }).catch((error: unknown) => {
    msgError(extractErrorMsg(error, '下载错误文件失败'))
  })
}
</script>

<style scoped>
.material-page {
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
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

.hidden-file-input {
  display: none;
}

.import-failure-table {
  margin-top: 16px;
}

.import-failure-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
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

.expand-detail {
  padding: 12px 20px;
  background: #fafbfc;
}

.expand-detail :deep(.el-descriptions__label) {
  color: #64748b;
  font-weight: 500;
  width: 90px;
}
</style>
