<template>
  <div class="stp-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2>排程时间参数</h2>
        <p class="page-subtitle">配置排程引擎的工单筛选范围、排程起点、显示窗口和辅助参数，支持按设备独立设置</p>
      </div>
      <el-button class="btn-cta" :icon="Plus" @click="openCreate">新增配置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" class="data-table" :header-cell-style="{ background: '#F8FAFC', color: '#1E293B', fontWeight: 600, fontSize: '13px' }">
        <el-table-column label="设备" min-width="130">
          <template #default="{ row }">
            <div v-if="row.resourceName" class="resource-cell">
              <span class="resource-name">{{ row.resourceName }}</span>
              <span v-if="row.resourceCode" class="resource-code">{{ row.resourceCode }}</span>
            </div>
            <el-tag v-else type="success" size="small" effect="plain">全局默认</el-tag>
          </template>
        </el-table-column>

        <el-table-column min-width="210">
          <template #header>
            <el-tooltip content="交期落在此范围内的工单才会被排程引擎处理" placement="top">
              <span>工单筛选范围 <el-icon :size="12" style="vertical-align:-1px"><QuestionFilled /></el-icon></span>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span class="mono">+{{ row.orderFilterStartDays }}d {{ fmtTime(row.orderFilterStartTime) }}</span>
            <span class="range-sep">~</span>
            <span class="mono">+{{ row.orderFilterEndDays }}d {{ fmtTime(row.orderFilterEndTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column min-width="140">
          <template #header>
            <el-tooltip content="Solver 可安排任务的最早时刻，同时也是冻结期的边界" placement="top">
              <span>排程起点 <el-icon :size="12" style="vertical-align:-1px"><QuestionFilled /></el-icon></span>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span class="mono">+{{ row.planningStartDays }}d {{ fmtTime(row.planningStartTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column min-width="120">
          <template #header>
            <el-tooltip content="甘特图展示的日期范围（天）" placement="top">
              <span>显示范围 <el-icon :size="12" style="vertical-align:-1px"><QuestionFilled /></el-icon></span>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span class="mono">{{ fmtOffsetDays(row.displayStartDays) }} ~ {{ fmtOffsetDays(row.displayEndDays) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="完成(天)" width="80" align="center">
          <template #default="{ row }"><span class="mono">{{ row.completionDays }}</span></template>
        </el-table-column>
        <el-table-column label="刻度" width="65" align="center">
          <template #default="{ row }"><span class="mono">{{ row.timeScale }}</span></template>
        </el-table-column>
        <el-table-column label="因子" width="65" align="center">
          <template #default="{ row }"><span class="mono">{{ row.factor }}</span></template>
        </el-table-column>
        <el-table-column width="80" align="center">
          <template #header>
            <el-tooltip content="允许在完成天数基础上额外超出的天数" placement="top">
              <span>超出期间 <el-icon :size="12" style="vertical-align:-1px"><QuestionFilled /></el-icon></span>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span class="mono">{{ row.exceedPeriod ?? '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag v-if="row.isDefault" type="warning" size="small" effect="plain">默认</el-tag>
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small" :effect="row.enabled ? 'light' : 'plain'">
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="View" @click="openPreview(row)">预览</el-button>
            <el-button link type="primary" size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm
              :title="`确认删除「${row.resourceName || '全局默认'}」的排程时间参数？`"
              confirm-button-text="删除"
              cancel-button-text="取消"
              confirm-button-type="danger"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button link type="danger" size="small" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无排程时间参数配置" :image-size="80">
            <el-button class="btn-cta" size="small" @click="openCreate">新增第一条配置</el-button>
          </el-empty>
        </template>
      </el-table>
    </div>

    <!-- 新建/编辑 对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑排程时间参数' : '新增排程时间参数'"
      width="780px"
      destroy-on-close
      @close="resetForm"
    >
      <div class="dialog-body">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="110px" class="param-form">

          <div class="form-section-title">基本信息</div>
          <el-form-item label="设备">
            <el-select v-model="form.resourceId" placeholder="不选则为全局默认" clearable style="width: 100%" :disabled="!!editingId" filterable>
              <el-option
                v-for="r in resources"
                :key="r.id"
                :label="`${r.resourceCode} - ${r.resourceName}`"
                :value="r.id"
              />
            </el-select>
          </el-form-item>
          <el-row>
            <el-col :span="12">
              <el-form-item label="是否默认">
                <el-switch v-model="form.isDefault" />
                <span class="hint-text">全局仅一条</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="启用">
                <el-switch v-model="form.enabled" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-section-title">工单筛选范围 <span class="section-hint">交期在此范围内的工单进入排程</span></div>
          <el-form-item label="排程始" required>
            <el-row :gutter="8" style="width:100%">
              <el-col :span="10">
                <el-form-item prop="orderFilterStartDays" style="margin-bottom:0">
                  <el-input-number v-model="form.orderFilterStartDays" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="2" class="unit-label">天</el-col>
              <el-col :span="12">
                <el-time-picker v-model="form.orderFilterStartTime" format="HH:mm:ss" value-format="HH:mm:ss" style="width:100%" placeholder="始时间" />
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item label="排程终" required>
            <el-row :gutter="8" style="width:100%">
              <el-col :span="10">
                <el-form-item prop="orderFilterEndDays" style="margin-bottom:0">
                  <el-input-number v-model="form.orderFilterEndDays" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="2" class="unit-label">天</el-col>
              <el-col :span="12">
                <el-time-picker v-model="form.orderFilterEndTime" format="HH:mm:ss" value-format="HH:mm:ss" style="width:100%" placeholder="终时间" />
              </el-col>
            </el-row>
          </el-form-item>

          <div class="form-section-title">排程安排起点 <span class="section-hint">Solver 最早可安排时刻，之前的任务冻结</span></div>
          <el-form-item label="排程起" required>
            <el-row :gutter="8" style="width:100%">
              <el-col :span="10">
                <el-form-item prop="planningStartDays" style="margin-bottom:0">
                  <el-input-number v-model="form.planningStartDays" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="2" class="unit-label">天</el-col>
              <el-col :span="12">
                <el-time-picker v-model="form.planningStartTime" format="HH:mm:ss" value-format="HH:mm:ss" style="width:100%" placeholder="起时间" />
              </el-col>
            </el-row>
          </el-form-item>

          <div class="form-section-title">显示范围 <span class="section-hint">甘特图展示的日期跨度</span></div>
          <el-row>
            <el-col :span="12">
              <el-form-item label="显示始(天)" prop="displayStartDays">
                <el-input-number v-model="form.displayStartDays" style="width:100%" />
                <div class="field-hint"><font color="red">支持负数，-1:昨天</font></div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="显示终(天)" prop="displayEndDays">
                <el-input-number v-model="form.displayEndDays" :min="1" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-section-title">辅助参数</div>
          <el-row>
            <el-col :span="12">
              <el-form-item label="完成(天)" prop="completionDays">
                <el-input-number v-model="form.completionDays" :min="0" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="超出期间(天)">
                <el-input-number v-model="form.exceedPeriod" :min="0" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="刻度(天)" prop="timeScale">
                <el-input-number v-model="form.timeScale" :min="1" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="因子">
                <el-input-number v-model="form.factor" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>

        <!-- 实时预览面板 -->
        <div class="live-preview">
          <div class="live-preview-title">实时计算预览</div>
          <div class="timeline-bar">
            <div class="tl-label">Today</div>
            <div class="tl-segments">
              <div class="tl-frozen" :style="{ width: frozenWidth + '%' }" title="冻结期">
                <span v-if="frozenWidth > 8">冻结</span>
              </div>
              <div class="tl-schedule" :style="{ width: scheduleWidth + '%' }" title="可排程区间">
                <span v-if="scheduleWidth > 10">可排程</span>
              </div>
              <div class="tl-tail" :style="{ width: tailWidth + '%' }"></div>
            </div>
            <div class="tl-label">+{{ form.displayEndDays }}d</div>
          </div>
          <div class="live-preview-grid">
            <div class="lp-item">
              <div class="lp-key">工单筛选</div>
              <div class="lp-val">{{ calcDate(form.orderFilterStartDays) }} {{ fmtTime(form.orderFilterStartTime) }} ~ {{ calcDate(form.orderFilterEndDays) }} {{ fmtTime(form.orderFilterEndTime) }}</div>
            </div>
            <div class="lp-item">
              <div class="lp-key">排程起点</div>
              <div class="lp-val">{{ calcDate(form.planningStartDays) }} {{ fmtTime(form.planningStartTime) }}</div>
            </div>
            <div class="lp-item">
              <div class="lp-key">显示范围</div>
              <div class="lp-val">{{ calcDate(form.displayStartDays) }} ~ {{ calcDate(form.displayEndDays) }}</div>
            </div>
            <div class="lp-item">
              <div class="lp-key">完成上限</div>
              <div class="lp-val">{{ (form.completionDays ?? 0) + (form.exceedPeriod ?? 0) }} 天 <span v-if="form.exceedPeriod" class="lp-detail">({{ form.completionDays }}+{{ form.exceedPeriod }})</span></div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="排程时间参数预览" width="560px">
      <div v-loading="previewLoading">
        <div v-if="previewData" class="preview-panel">
          <div class="preview-header">
            <span class="preview-resource">{{ previewData.resourceName || '全局默认' }}</span>
            <el-tag v-if="previewData.isDefault" type="warning" size="small" effect="plain">默认配置</el-tag>
          </div>

          <div class="timeline-bar" style="margin: 16px 0">
            <div class="tl-label">Today</div>
            <div class="tl-segments">
              <div class="tl-frozen" :style="{ width: previewFrozenWidth + '%' }"><span v-if="previewFrozenWidth > 8">冻结</span></div>
              <div class="tl-schedule" :style="{ width: previewScheduleWidth + '%' }"><span v-if="previewScheduleWidth > 10">可排程</span></div>
              <div class="tl-tail" :style="{ width: previewTailWidth + '%' }"></div>
            </div>
            <div class="tl-label">+{{ previewData.displayEndDays }}d</div>
          </div>

          <div class="preview-grid">
            <div class="pg-row">
              <div class="pg-icon order-filter-icon"></div>
              <div class="pg-content">
                <div class="pg-label">工单筛选范围</div>
                <div class="pg-value">{{ fmtDateTime(previewData.calculatedOrderFilterStart) }} ~ {{ fmtDateTime(previewData.calculatedOrderFilterEnd) }}</div>
              </div>
            </div>
            <div class="pg-row">
              <div class="pg-icon planning-start-icon"></div>
              <div class="pg-content">
                <div class="pg-label">排程安排起点（冻结期边界）</div>
                <div class="pg-value">{{ fmtDateTime(previewData.calculatedPlanningStart) }}</div>
              </div>
            </div>
            <div class="pg-row">
              <div class="pg-icon display-icon"></div>
              <div class="pg-content">
                <div class="pg-label">显示范围</div>
                <div class="pg-value">{{ previewData.calculatedDisplayStart }} ~ {{ previewData.calculatedDisplayEnd }}</div>
              </div>
            </div>
            <div class="pg-row">
              <div class="pg-icon completion-icon"></div>
              <div class="pg-content">
                <div class="pg-label">完成期限上限</div>
                <div class="pg-value">
                  {{ (previewData.completionDays ?? 0) + (previewData.exceedPeriod ?? 0) }} 天
                  <span v-if="previewData.exceedPeriod" class="lp-detail">({{ previewData.completionDays }} + {{ previewData.exceedPeriod }})</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, View, QuestionFilled } from '@element-plus/icons-vue'
import { scheduleTimeParameterApi, type ScheduleTimeParameter } from '../api/scheduleTimeParameter'
import axiosInstance from '../api/axios'
import type { AjaxResult } from '../api/types'

interface ResourceOption { id: string; resourceCode: string; resourceName: string }

const loading = ref(false)
const saving = ref(false)
const previewLoading = ref(false)
const list = ref<ScheduleTimeParameter[]>([])
const resources = ref<ResourceOption[]>([])
const dialogVisible = ref(false)
const previewVisible = ref(false)
const editingId = ref<string | null>(null)
const previewData = ref<ScheduleTimeParameter | null>(null)
const formRef = ref()

const defaultForm = (): ScheduleTimeParameter => ({
  resourceId: undefined,
  orderFilterStartDays: 0,
  orderFilterStartTime: '08:00:00',
  orderFilterEndDays: 14,
  orderFilterEndTime: '00:00:00',
  planningStartDays: 0,
  planningStartTime: '09:00:00',
  displayStartDays: 0,
  displayEndDays: 30,
  completionDays: 0,
  timeScale: 1,
  factor: 0,
  exceedPeriod: undefined,
  isDefault: false,
  enabled: true,
  remark: ''
})

const form = ref<ScheduleTimeParameter>(defaultForm())

const rules = {
  orderFilterStartDays: [{ required: true, message: '请输入筛选起始天数', trigger: 'blur' }],
  orderFilterEndDays: [{ required: true, message: '请输入筛选终止天数', trigger: 'blur' }],
  planningStartDays: [{ required: true, message: '请输入排程起点天数', trigger: 'blur' }],
  displayStartDays: [{ required: true, message: '请输入显示起始天数', trigger: 'blur' }],
  displayEndDays: [{ required: true, message: '请输入显示终止天数', trigger: 'blur' }],
  completionDays: [{ required: true, message: '请输入完成天数', trigger: 'blur' }],
  timeScale: [{ required: true, message: '请输入时间刻度', trigger: 'blur' }]
}

// ===== 实时预览计算 =====

const displayTotal = computed(() => Math.max((form.value.displayEndDays ?? 30) - (form.value.displayStartDays ?? 0), 1))
const frozenWidth = computed(() => {
  const frozen = (form.value.planningStartDays ?? 0) - (form.value.displayStartDays ?? 0)
  return Math.max(0, Math.min(100, (frozen / displayTotal.value) * 100))
})
const scheduleWidth = computed(() => {
  const sched = (form.value.displayEndDays ?? 30) - (form.value.planningStartDays ?? 0)
  return Math.max(0, Math.min(100 - frozenWidth.value, (sched / displayTotal.value) * 100))
})
const tailWidth = computed(() => Math.max(0, 100 - frozenWidth.value - scheduleWidth.value))

// 预览弹窗的时间轴
const previewDisplayTotal = computed(() => {
  if (!previewData.value) return 1
  return Math.max((previewData.value.displayEndDays ?? 30) - (previewData.value.displayStartDays ?? 0), 1)
})
const previewFrozenWidth = computed(() => {
  if (!previewData.value) return 0
  const frozen = (previewData.value.planningStartDays ?? 0) - (previewData.value.displayStartDays ?? 0)
  return Math.max(0, Math.min(100, (frozen / previewDisplayTotal.value) * 100))
})
const previewScheduleWidth = computed(() => {
  if (!previewData.value) return 100
  const sched = (previewData.value.displayEndDays ?? 30) - (previewData.value.planningStartDays ?? 0)
  return Math.max(0, Math.min(100 - previewFrozenWidth.value, (sched / previewDisplayTotal.value) * 100))
})
const previewTailWidth = computed(() => Math.max(0, 100 - previewFrozenWidth.value - previewScheduleWidth.value))

// ===== 格式化工具 =====

function fmtTime(t?: string) {
  if (!t) return ''
  return t.length > 5 ? t.slice(0, 5) : t
}

function fmtDateTime(dt?: string) {
  if (!dt) return '-'
  return dt.replace('T', ' ').slice(0, 16)
}

function calcDate(days?: number): string {
  if (days == null) return '-'
  const d = new Date()
  d.setDate(d.getDate() + days)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${m}-${dd}`
}

function fmtOffsetDays(days?: number): string {
  if (days == null) return '-'
  return `${days >= 0 ? '+' : ''}${days}d`
}

// ===== 数据加载 =====

onMounted(() => {
  loadList()
  loadResources()
})

async function loadList() {
  loading.value = true
  try {
    list.value = await scheduleTimeParameterApi.list()
  } catch (e: any) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function loadResources() {
  try {
    const res = await axiosInstance.get<AjaxResult<ResourceOption[]>>('/resources')
    if (res.data.code === 200 && res.data.data) {
      resources.value = res.data.data
    }
  } catch { /* 忽略 */ }
}

// ===== 操作 =====

function openCreate() {
  editingId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

function openEdit(row: ScheduleTimeParameter) {
  editingId.value = row.id!
  form.value = { ...row }
  dialogVisible.value = true
}

async function openPreview(row: ScheduleTimeParameter) {
  previewLoading.value = true
  previewVisible.value = true
  previewData.value = null
  try {
    previewData.value = await scheduleTimeParameterApi.preview(row.resourceId)
  } catch (e: any) {
    ElMessage.error(e.message)
    previewVisible.value = false
  } finally {
    previewLoading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await formRef.value?.validate()
    if (editingId.value) {
      await scheduleTimeParameterApi.update(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await scheduleTimeParameterApi.create(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e: any) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: ScheduleTimeParameter) {
  try {
    await scheduleTimeParameterApi.remove(row.id!)
    ElMessage.success('删除成功')
    loadList()
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

function resetForm() {
  formRef.value?.resetFields()
  editingId.value = null
}
</script>

<style scoped>
/* ===== 页面骨架 ===== */
.stp-page {
  padding: 20px 24px;
  font-family: 'Fira Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 4px;
}
.page-subtitle {
  font-size: 13px;
  color: #64748B;
  margin: 0;
}
.btn-cta {
  background: #F97316 !important;
  border-color: #F97316 !important;
  color: #fff !important;
  font-weight: 600;
  border-radius: 8px;
}
.btn-cta:hover {
  background: #EA580C !important;
  border-color: #EA580C !important;
}

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #E2E8F0;
  overflow: hidden;
}
.data-table {
  width: 100%;
  --el-table-border-color: #E2E8F0;
}
.mono {
  font-family: 'Fira Code', monospace;
  font-size: 12px;
  color: #334155;
  font-variant-numeric: tabular-nums;
}
.range-sep {
  margin: 0 4px;
  color: #94A3B8;
}
.resource-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.resource-name {
  font-weight: 500;
  color: #1E293B;
}
.resource-code {
  font-size: 11px;
  color: #94A3B8;
  font-family: 'Fira Code', monospace;
}
.status-cell {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: wrap;
}

/* ===== 对话框 ===== */
.dialog-body {
  display: flex;
  gap: 20px;
}
.param-form {
  flex: 1;
  min-width: 0;
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 12px;
}
.form-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #2563EB;
  padding: 8px 0 10px;
  border-bottom: 2px solid #DBEAFE;
  margin-bottom: 14px;
  margin-top: 8px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.section-hint {
  font-size: 11px;
  font-weight: 400;
  color: #94A3B8;
}
.hint-text {
  font-size: 11px;
  color: #94A3B8;
  margin-left: 8px;
}
.unit-label {
  line-height: 32px;
  text-align: center;
  font-size: 13px;
  color: #64748B;
}

/* ===== 实时预览面板 ===== */
.live-preview {
  width: 220px;
  flex-shrink: 0;
  background: #F8FAFC;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  padding: 14px;
  align-self: flex-start;
  position: sticky;
  top: 0;
}
.live-preview-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748B;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}
.live-preview-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.lp-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.lp-key {
  font-size: 11px;
  font-weight: 500;
  color: #94A3B8;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.lp-val {
  font-size: 12px;
  font-weight: 500;
  color: #1E293B;
  font-family: 'Fira Code', monospace;
  word-break: break-all;
}
.lp-detail {
  font-size: 11px;
  color: #94A3B8;
}

/* ===== 时间轴条 ===== */
.timeline-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
}
.tl-label {
  font-size: 10px;
  font-weight: 600;
  color: #94A3B8;
  white-space: nowrap;
  font-family: 'Fira Code', monospace;
}
.tl-segments {
  flex: 1;
  height: 20px;
  display: flex;
  border-radius: 4px;
  overflow: hidden;
  background: #E2E8F0;
}
.tl-frozen {
  background: #FEE2E2;
  border-right: 2px solid #EF4444;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 600;
  color: #B91C1C;
  overflow: hidden;
  transition: width 0.2s ease;
}
.tl-schedule {
  background: #DBEAFE;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 600;
  color: #1D4ED8;
  overflow: hidden;
  transition: width 0.2s ease;
}
.tl-tail {
  background: #F1F5F9;
  transition: width 0.2s ease;
}

/* ===== 预览弹窗 ===== */
.preview-panel {
  padding: 4px 0;
}
.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E2E8F0;
}
.preview-resource {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}
.preview-grid {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.pg-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #F1F5F9;
}
.pg-row:last-child {
  border-bottom: none;
}
.pg-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 5px;
}
.order-filter-icon { background: #3B82F6; }
.planning-start-icon { background: #EF4444; }
.display-icon { background: #10B981; }
.completion-icon { background: #F59E0B; }
.pg-content {
  flex: 1;
}
.pg-label {
  font-size: 12px;
  color: #64748B;
  margin-bottom: 2px;
}
.pg-value {
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
  font-family: 'Fira Code', monospace;
  font-variant-numeric: tabular-nums;
}
</style>
