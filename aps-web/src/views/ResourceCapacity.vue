<template>
  <div class="resource-capacity-page">
    <div class="page-header">
      <div>
        <h2>设备日产能</h2>
        <p>按设备维护每日班次时间、利用率和可用产能，班次支持按小时录入</p>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="left-panel">
        <div class="panel-title">设备列表</div>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索设备编码/名称"
          clearable
          class="search-input"
        />
        <div v-loading="loadingResources" class="resource-list">
          <div
            v-for="resource in filteredResources"
            :key="resource.id"
            :class="['resource-card', { active: selectedResource?.id === resource.id }]"
            @click="selectResource(resource)"
          >
            <div class="resource-card-header">
              <div class="resource-name">{{ resource.resourceName }}</div>
              <el-tag size="small">{{ resource.resourceCode }}</el-tag>
            </div>
            <div class="resource-meta">类型：{{ resource.resourceType || '-' }}</div>
            <div class="resource-meta">车间：{{ resource.workshopName || '-' }}</div>
            <div class="resource-meta">日历：{{ resource.calendarName || '继承车间/默认' }}</div>
          </div>
          <el-empty v-if="!filteredResources.length" description="暂无设备" />
        </div>
      </div>

      <div class="right-panel">
        <template v-if="selectedResource && monthData">
          <div class="detail-header">
            <div>
              <h3>{{ monthData.resourceName }}</h3>
              <div class="detail-subtitle">
                编码 {{ monthData.resourceCode }}
                <span class="separator">|</span>
                车间 {{ monthData.workshopName || '-' }}
                <span class="separator">|</span>
                日历 {{ monthData.calendarName || '-' }}
              </div>
            </div>
            <div class="detail-actions">
              <el-button-group>
                <el-button @click="changeMonth(-1)">上月</el-button>
                <el-button disabled>{{ currentYear }}-{{ String(currentMonth).padStart(2, '0') }}</el-button>
                <el-button @click="changeMonth(1)">下月</el-button>
              </el-button-group>
            </div>
          </div>

          <div class="summary-grid">
            <div class="summary-card">
              <div class="summary-label">工作日</div>
              <div class="summary-value">{{ monthData.workdayCount }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-label">默认班次小时</div>
              <div class="summary-value">{{ formatHours(monthData.totalDefaultShiftMinutes) }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-label">有效班次小时</div>
              <div class="summary-value">{{ formatHours(monthData.totalEffectiveShiftMinutes) }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-label">可用产能小时</div>
              <div class="summary-value">{{ formatHours(monthData.totalAvailableCapacityMinutes) }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-label">平均利用率</div>
              <div class="summary-value">{{ formatPercent(monthData.averageUtilizationRate) }}</div>
            </div>
          </div>

          <div class="batch-toolbar">
            <div class="batch-toolbar__info">
              已选 {{ selectedDates.length }} 天
            </div>
            <div class="batch-toolbar__actions">
              <el-button @click="clearSelection" :disabled="!selectedDates.length">清空选择</el-button>
              <el-button type="primary" @click="openBatchDialog" :disabled="!selectedDates.length || !canBatchEditResourceCapacity">批量修改</el-button>
            </div>
          </div>

          <el-table
            ref="monthTableRef"
            v-loading="loadingMonth || batchSaving"
            :data="monthData.days"
            height="calc(100vh - 320px)"
            stripe
            @row-click="handleRowClick"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="60" />
            <el-table-column prop="date" label="日期" width="108" />
            <el-table-column label="星期" width="78">
              <template #default="{ row }">{{ formatWeekday(row.date) }}</template>
            </el-table-column>
            <el-table-column label="日期类型" width="88">
              <template #default="{ row }">
                <el-tag :type="dateTypeTagMap[row.dateType]">{{ dateTypeLabelMap[row.dateType] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="默认班次" width="92">
              <template #default="{ row }">{{ formatHours(row.defaultShiftMinutes) }}</template>
            </el-table-column>
            <el-table-column label="班次小时" width="150">
              <template #default="{ row }">
                <el-input-number
                  :model-value="minutesToHours(row.shiftMinutesOverride)"
                  @update:model-value="(value: number | null | undefined) => updateRowShiftHours(row, value)"
                  :disabled="!canEditResourceCapacity"
                  :min="0"
                  :step="0.5"
                  :precision="2"
                  placeholder="留空用默认"
                  controls-position="right"
                  style="width: 124px"
                />
              </template>
            </el-table-column>
            <el-table-column label="利用率" width="118">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.utilizationRate"
                  :disabled="!canEditResourceCapacity"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  :precision="2"
                  controls-position="right"
                  style="width: 102px"
                />
              </template>
            </el-table-column>
            <el-table-column label="可用产能" width="96">
              <template #default="{ row }">{{ formatHours(computeCapacity(row)) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.remark" :disabled="!canEditResourceCapacity" placeholder="备注" clearable />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="76" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" :loading="savingDate === row.date" :disabled="!canEditResourceCapacity" @click="saveDay(row)">保存</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <el-empty v-else description="请选择左侧设备" />
      </div>
    </div>

    <el-dialog v-model="batchDialogVisible" title="批量修改日产能" width="520px">
      <el-form label-width="110px">
        <el-form-item label="已选日期">
          <div class="batch-selected-dates">{{ selectedDatesText }}</div>
          <div class="batch-form-tip">班次小时留空表示恢复默认班次；关闭开关表示该字段不修改</div>
        </el-form-item>
        <el-form-item label="班次小时">
          <div class="batch-input-row">
            <el-switch v-model="batchForm.updateShiftMinutesOverride" :disabled="!canBatchEditResourceCapacity" />
            <el-input-number
              :model-value="minutesToHours(batchForm.shiftMinutesOverride)"
              @update:model-value="updateBatchShiftHours"
              :disabled="!batchForm.updateShiftMinutesOverride"
              :min="0"
              :step="0.5"
              :precision="2"
              placeholder="留空恢复默认"
              controls-position="right"
              style="width: 180px"
            />
          </div>
        </el-form-item>
        <el-form-item label="利用率">
          <div class="batch-input-row">
            <el-switch v-model="batchForm.updateUtilizationRate" :disabled="!canBatchEditResourceCapacity" />
            <el-input-number
              v-model="batchForm.utilizationRate"
              :disabled="!batchForm.updateUtilizationRate"
              :min="0"
              :max="1"
              :step="0.1"
              :precision="2"
              controls-position="right"
              style="width: 180px"
            />
          </div>
        </el-form-item>
        <el-form-item label="备注操作">
          <el-radio-group v-model="batchForm.remarkAction">
            <el-radio value="ignore">不修改</el-radio>
            <el-radio value="set">设置备注</el-radio>
            <el-radio value="clear">清空备注</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="batchForm.remark"
            :disabled="batchForm.remarkAction !== 'set'"
            placeholder="输入要批量设置的备注"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="batchSaving" :disabled="!canBatchEditResourceCapacity" @click="saveBatchDays">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { msgError, msgSuccess, msgWarning, extractErrorMsg } from '@/utils/message'
import { useAuthStore } from '@/stores/auth'
import type { ResourceCapacityBatchUpdatePayload, ResourceCapacityDayUpdatePayload } from '@/api/resourceCapacity'
import { resourceCapacityApi, type ResourceCapacityDay, type ResourceCapacityMonthResult } from '@/api/resourceCapacity'
import type { Resource } from '@/api/types'

interface ResourceCapacityBatchForm {
  updateShiftMinutesOverride: boolean
  shiftMinutesOverride: number | null
  updateUtilizationRate: boolean
  utilizationRate: number | null
  remark: string | null
  remarkAction: 'ignore' | 'set' | 'clear'
}

type ResourceCapacityTableRef = {
  clearSelection: () => void
  toggleRowSelection: (row: ResourceCapacityDay, selected?: boolean) => void
}

const authStore = useAuthStore()
const loadingResources = ref(false)
const loadingMonth = ref(false)
const batchSaving = ref(false)
const resources = ref<Resource[]>([])
const selectedResource = ref<Resource | null>(null)
const monthData = ref<ResourceCapacityMonthResult | null>(null)
const searchKeyword = ref('')
const savingDate = ref<string | null>(null)
const selectedDates = ref<string[]>([])
const batchDialogVisible = ref(false)
const batchForm = ref<ResourceCapacityBatchForm>(createBatchForm())
const monthTableRef = ref<ResourceCapacityTableRef | null>(null)

const now = new Date()
const currentYear = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)

const dateTypeLabelMap: Record<string, string> = {
  WORKDAY: '工作日',
  RESTDAY: '休息日',
  HOLIDAY: '节假日'
}

const dateTypeTagMap: Record<string, 'success' | 'info' | 'danger'> = {
  WORKDAY: 'success',
  RESTDAY: 'info',
  HOLIDAY: 'danger'
}

const canEditResourceCapacity = computed(() => authStore.hasPermission('basedata:resource-capacity:edit'))
const canBatchEditResourceCapacity = computed(() => authStore.hasPermission('basedata:resource-capacity:batch-edit'))

const filteredResources = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return resources.value
  return resources.value.filter(item => {
    const resourceCode = item.resourceCode?.toLowerCase() ?? ''
    const resourceName = item.resourceName?.toLowerCase() ?? ''
    return resourceCode.includes(keyword) || resourceName.includes(keyword)
  })
})

const selectedDatesText = computed(() => {
  if (!selectedDates.value.length) {
    return '未选择日期'
  }
  return selectedDates.value.join('，')
})

onMounted(async () => {
  await loadResources()
})

function createBatchForm(): ResourceCapacityBatchForm {
  return {
    updateShiftMinutesOverride: false,
    shiftMinutesOverride: null,
    updateUtilizationRate: false,
    utilizationRate: null,
    remark: null,
    remarkAction: 'ignore'
  }
}

function normalizeRemark(remark: string | null | undefined): string | null {
  if (remark == null) {
    return null
  }
  const trimmed = remark.trim()
  return trimmed ? trimmed : null
}

function normalizeShiftMinutes(value: number | null | undefined): number | null | undefined {
  if (value == null) {
    return value
  }
  return Number.isFinite(value) ? value : undefined
}

function minutesToHours(value: number | null | undefined): number | null | undefined {
  if (value == null) {
    return value
  }
  return Number((value / 60).toFixed(2))
}

function hoursToMinutes(value: number | null | undefined): number | null | undefined {
  if (value == null) {
    return value
  }
  if (!Number.isFinite(value)) {
    return undefined
  }
  return Math.round(value * 60)
}

function updateRowShiftHours(row: ResourceCapacityDay, value: number | null | undefined) {
  row.shiftMinutesOverride = hoursToMinutes(value) ?? null
}

function updateBatchShiftHours(value: number | null | undefined) {
  batchForm.value.shiftMinutesOverride = hoursToMinutes(value) ?? null
}

function normalizeUtilizationRate(value: number | null | undefined): number | null | undefined {
  if (value == null) {
    return value
  }
  return Number.isFinite(value) ? value : undefined
}

function validateShiftMinutes(value: number | null | undefined): boolean {
  if (value == null) {
    return true
  }
  if (!Number.isFinite(value)) {
    msgWarning('班次小时必须是有效数字')
    return false
  }
  if (value < 0) {
    msgWarning('班次小时不能小于0')
    return false
  }
  return true
}

function validateUtilizationRate(value: number | null | undefined): boolean {
  if (value == null) {
    msgWarning('利用率不能为空')
    return false
  }
  if (!Number.isFinite(value)) {
    msgWarning('利用率必须是有效数字')
    return false
  }
  if (value < 0 || value > 1) {
    msgWarning('利用率必须在0到1之间')
    return false
  }
  return true
}

function buildSingleDayPayload(row: ResourceCapacityDay): ResourceCapacityDayUpdatePayload | null {
  const shiftMinutesOverride = normalizeShiftMinutes(row.shiftMinutesOverride)
  const utilizationRate = normalizeUtilizationRate(row.utilizationRate)
  const remark = normalizeRemark(row.remark)

  if (!validateShiftMinutes(shiftMinutesOverride) || !validateUtilizationRate(utilizationRate) || utilizationRate == null) {
    return null
  }

  return {
    ...(shiftMinutesOverride === null ? { shiftMinutesOverride: null } : shiftMinutesOverride !== undefined ? { shiftMinutesOverride } : {}),
    utilizationRate,
    remark
  }
}

function buildBatchPayload(form: ResourceCapacityBatchForm): Omit<ResourceCapacityBatchUpdatePayload, 'dates'> | null {
  const payload: Omit<ResourceCapacityBatchUpdatePayload, 'dates'> = {}

  if (form.updateShiftMinutesOverride) {
    const shiftMinutesOverride = normalizeShiftMinutes(form.shiftMinutesOverride)
    if (!validateShiftMinutes(shiftMinutesOverride)) {
      return null
    }
    payload.shiftMinutesOverride = shiftMinutesOverride ?? null
  }

  if (form.updateUtilizationRate) {
    const utilizationRate = normalizeUtilizationRate(form.utilizationRate)
    if (!validateUtilizationRate(utilizationRate) || utilizationRate == null) {
      return null
    }
    payload.utilizationRate = utilizationRate
  }

  if (form.remarkAction === 'set') {
    const remark = normalizeRemark(form.remark)
    if (remark == null) {
      msgWarning('请输入要设置的备注')
      return null
    }
    payload.remark = remark
  }

  if (form.remarkAction === 'clear') {
    payload.remark = null
  }

  if (!form.updateShiftMinutesOverride && !form.updateUtilizationRate && form.remarkAction === 'ignore') {
    msgWarning('请至少填写一个需要修改的字段')
    return null
  }

  return payload
}

async function loadResources() {
  loadingResources.value = true
  try {
    resources.value = await resourceCapacityApi.getResources()
    if (resources.value.length > 0) {
      await selectResource(resources.value[0])
    }
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '加载设备列表失败'))
  } finally {
    loadingResources.value = false
  }
}

async function selectResource(resource: Resource) {
  selectedResource.value = resource
  clearSelection()
  batchDialogVisible.value = false
  batchForm.value = createBatchForm()
  await loadMonthData()
}

async function loadMonthData(): Promise<boolean> {
  if (!selectedResource.value) return false
  loadingMonth.value = true
  try {
    monthData.value = await resourceCapacityApi.getMonthCapacity({
      resourceId: selectedResource.value.id,
      year: currentYear.value,
      month: currentMonth.value
    })
    return true
  } catch (error: unknown) {
    monthData.value = null
    msgError(extractErrorMsg(error, '加载设备日产能失败'))
    return false
  } finally {
    loadingMonth.value = false
  }
}

async function changeMonth(offset: number) {
  const previousYear = currentYear.value
  const previousMonth = currentMonth.value
  const next = new Date(currentYear.value, currentMonth.value - 1 + offset, 1)

  currentYear.value = next.getFullYear()
  currentMonth.value = next.getMonth() + 1
  clearSelection()
  batchDialogVisible.value = false

  const loaded = await loadMonthData()
  if (!loaded) {
    currentYear.value = previousYear
    currentMonth.value = previousMonth
  }
}

function handleSelectionChange(rows: ResourceCapacityDay[]) {
  selectedDates.value = rows.map(row => row.date)
}

function handleRowClick(row: ResourceCapacityDay, _column: unknown, event: Event) {
  const target = event.target as HTMLElement | null
  if (target?.closest('.el-input, .el-input-number, .el-button, .el-checkbox, textarea, input, button')) {
    return
  }

  const isSelected = selectedDates.value.includes(row.date)
  monthTableRef.value?.toggleRowSelection(row, !isSelected)
}

function clearSelection() {
  selectedDates.value = []
  monthTableRef.value?.clearSelection()
}

function openBatchDialog() {
  if (!selectedDates.value.length) {
    msgWarning('请先选择日期')
    return
  }
  batchForm.value = createBatchForm()
  batchDialogVisible.value = true
}

async function saveDay(row: ResourceCapacityDay) {
  if (!selectedResource.value) return
  const payload = buildSingleDayPayload(row)
  if (!payload || payload.utilizationRate == null) {
    return
  }

  savingDate.value = row.date
  try {
    const saved = await resourceCapacityApi.updateDay(selectedResource.value.id, row.date, payload)
    updateDayRow(saved)
    recomputeSummary()
    msgSuccess('保存成功')
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '保存失败'))
  } finally {
    savingDate.value = null
  }
}

async function saveBatchDays() {
  if (!selectedResource.value) return
  if (!selectedDates.value.length) {
    msgWarning('请先选择日期')
    return
  }

  const payload = buildBatchPayload(batchForm.value)
  if (!payload) {
    return
  }

  batchSaving.value = true
  try {
    await resourceCapacityApi.batchUpdateDays(selectedResource.value.id, {
      dates: [...selectedDates.value],
      ...payload
    })
    batchDialogVisible.value = false
    batchForm.value = createBatchForm()
    clearSelection()

    const refreshed = await loadMonthData()
    msgSuccess('批量保存成功')
    if (!refreshed) {
      msgWarning('数据已保存，列表刷新失败，请手动重新加载')
    }
  } catch (error: unknown) {
    msgError(extractErrorMsg(error, '批量保存失败'))
  } finally {
    batchSaving.value = false
  }
}

function updateDayRow(saved: ResourceCapacityDay) {
  if (!monthData.value) {
    return
  }
  monthData.value = {
    ...monthData.value,
    days: monthData.value.days.map(item => item.date === saved.date ? saved : item)
  }
}

function recomputeSummary() {
  if (!monthData.value) return
  const days = monthData.value.days.map(item => calculateCapacity(item))
  monthData.value = {
    ...monthData.value,
    days,
    totalDefaultShiftMinutes: days.reduce((sum, item) => sum + item.defaultShiftMinutes, 0),
    totalEffectiveShiftMinutes: days.reduce((sum, item) => sum + item.effectiveShiftMinutes, 0),
    totalAvailableCapacityMinutes: days.reduce((sum, item) => sum + item.availableCapacityMinutes, 0),
    averageUtilizationRate: days.length
      ? days.reduce((sum, item) => sum + Number(item.utilizationRate || 0), 0) / days.length
      : 0
  }
}

function computeCapacity(row: ResourceCapacityDay) {
  return calculateCapacity(row).availableCapacityMinutes
}

function calculateCapacity(row: ResourceCapacityDay): ResourceCapacityDay {
  const shiftMinutes = row.shiftMinutesOverride ?? row.defaultShiftMinutes
  return {
    ...row,
    effectiveShiftMinutes: shiftMinutes,
    availableCapacityMinutes: Math.round(shiftMinutes * Number(row.utilizationRate || 0))
  }
}

function formatHours(minutes: number) {
  return (minutes / 60).toFixed(2)
}

function formatPercent(rate: number) {
  return `${(Number(rate || 0) * 100).toFixed(2)}%`
}

function parseLocalDate(dateText: string): Date {
  const [yearText, monthText, dayText] = dateText.split('-')
  const year = Number(yearText)
  const month = Number(monthText)
  const day = Number(dayText)
  return new Date(year, month - 1, day)
}

function formatWeekday(dateText: string) {
  const date = parseLocalDate(dateText)
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
}
</script>

<style scoped>
.resource-capacity-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(100vh - 96px);
  min-height: 0;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  line-height: 1.2;
}

.page-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 248px 1fr;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.left-panel,
.right-panel {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  min-height: 0;
  overflow: hidden;
}

.panel-title {
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 14px;
}

.search-input {
  margin-bottom: 8px;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: calc(100vh - 190px);
  overflow: auto;
}

.resource-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
}

.resource-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.resource-card-header,
.detail-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.resource-name,
.detail-header h3 {
  font-weight: 600;
}

.resource-meta,
.detail-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.separator {
  margin: 0 6px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
  margin: 10px 0;
}

.summary-card {
  padding: 10px 12px;
  border-radius: 10px;
  background: #f8fafc;
}

.summary-label {
  color: #64748b;
  font-size: 12px;
}

.summary-value {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 600;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.batch-toolbar__info {
  color: #64748b;
  font-size: 12px;
}

.batch-toolbar__actions {
  display: flex;
  gap: 8px;
}

.detail-actions :deep(.el-button) {
  padding: 8px 12px;
}

:deep(.el-table td),
:deep(.el-table th) {
  padding-top: 8px;
  padding-bottom: 8px;
}

:deep(.el-table .cell) {
  line-height: 1.2;
}

:deep(.el-input-number) {
  --el-input-height: 30px;
}

:deep(.el-input__wrapper) {
  padding-top: 1px;
  padding-bottom: 1px;
}

.batch-selected-dates {
  line-height: 1.6;
  color: #334155;
  word-break: break-all;
}

.batch-form-tip {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.batch-input-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.el-table .el-table-column--selection .cell) {
  padding-left: 12px;
  padding-right: 12px;
}

:deep(.el-table__row) {
  cursor: pointer;
}

@media (max-height: 900px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .summary-card {
    padding: 8px 10px;
  }

  .summary-value {
    font-size: 16px;
  }

  .resource-list {
    max-height: calc(100vh - 176px);
  }
}

@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .batch-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .batch-toolbar__actions {
    justify-content: flex-end;
  }
}
</style>
