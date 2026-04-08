<template>
  <div class="factory-calendar-page">
    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 左侧：日历列表 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3><el-icon><Calendar /></el-icon> 工厂日历</h3>
          <el-button type="primary" size="small" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>

        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索日历..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />

        <!-- 日历卡片列表 -->
        <div v-loading="loading" class="calendar-list">
          <div
            v-for="cal in filteredCalendars"
            :key="cal.id"
            :class="['calendar-card', { active: selectedCalendar?.id === cal.id }]"
            @click="selectCalendar(cal)"
          >
            <div class="calendar-header">
              <div class="calendar-name">{{ cal.name }}</div>
              <el-tag v-if="cal.isDefault" type="success" size="small">默认</el-tag>
            </div>
            <div class="calendar-info">
              <div class="info-item">
                <span class="label">代码:</span>
                <span class="value">{{ cal.code }}</span>
              </div>
              <div class="info-item">
                <span class="label">年份:</span>
                <span class="value">{{ cal.year }}</span>
              </div>
              <div class="info-item">
                <span class="label">工作日:</span>
                <span class="value">{{ cal.workdayCount }}</span>
              </div>
            </div>
            <div class="calendar-actions">
              <el-button text size="small" @click.stop="handleEdit(cal)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button text size="small" @click.stop="handleDelete(cal)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>

          <el-empty v-if="filteredCalendars.length === 0" description="暂无日历" />
        </div>
      </div>

      <!-- 右侧：日历详情 -->
      <div class="right-panel">
        <div v-if="selectedCalendar" class="calendar-detail">
          <!-- 标签页 -->
          <el-tabs v-model="activeTab" class="detail-tabs">
            <!-- 月视图标签页 -->
            <el-tab-pane label="月视图" name="month">
              <div class="month-view">
                <!-- 月份选择 -->
                <div class="month-selector">
                  <el-button-group>
                    <el-button @click="previousMonth">
                      <el-icon><ArrowLeft /></el-icon>
                    </el-button>
                    <el-button disabled>{{ currentYear }}-{{ String(currentMonth).padStart(2, '0') }}</el-button>
                    <el-button @click="nextMonth">
                      <el-icon><ArrowRight /></el-icon>
                    </el-button>
                  </el-button-group>
                </div>

                <!-- 日历网格 -->
                <div class="calendar-grid">
                  <!-- 星期头 -->
                  <div class="weekday-header">
                    <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
                  </div>

                  <!-- 日期单元格 -->
                  <div class="date-cells">
                    <div
                      v-for="date in calendarDates"
                      :key="date.date"
                      :class="['date-cell', getDateClass(date)]"
                      @click="selectDate(date)"
                    >
                      <div class="date-number">{{ new Date(date.date).getDate() }}</div>
                      <div class="date-type-badge">{{ getDateTypeLabel(date.dateType) }}</div>
                    </div>
                  </div>
                </div>

                <!-- 日期类型编辑 -->
                <div v-if="selectedDate" class="date-editor">
                  <div class="editor-header">
                    <span>{{ selectedDate.date }} - 编辑</span>
                    <el-button text size="small" @click="selectedDate = null">
                      <el-icon><Close /></el-icon>
                    </el-button>
                  </div>
                  <el-form :model="dateForm" label-width="80px" size="small">
                    <el-form-item label="日期类型">
                      <el-select v-model="dateForm.dateType">
                        <el-option label="工作日" value="WORKDAY" />
                        <el-option label="休息日" value="RESTDAY" />
                        <el-option label="节假日" value="HOLIDAY" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="标签">
                      <el-input v-model="dateForm.label" placeholder="如：春节" />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" size="small" @click="saveDateType">保存</el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </el-tab-pane>

            <!-- 班次配置标签页 -->
            <el-tab-pane label="班次配置" name="shifts">
              <div class="shifts-view">
                <div class="shifts-header">
                  <el-button type="primary" size="small" @click="handleAddShift">
                    <el-icon><Plus /></el-icon>
                    添加班次
                  </el-button>
                </div>

                <!-- 班次列表 -->
                <el-table :data="shifts" stripe size="small" class="shifts-table">
                  <el-table-column prop="name" label="班次名称" width="120" />
                  <el-table-column prop="startTime" label="开始时间" width="120" />
                  <el-table-column prop="endTime" label="结束时间" width="120" />
                  <el-table-column prop="sortOrder" label="排序" width="80" />
                  <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                      <el-button text size="small" @click="handleEditShift(row)">
                        <el-icon><Edit /></el-icon>
                      </el-button>
                      <el-button text size="small" @click="handleDeleteShift(row)">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>

                <el-empty v-if="shifts.length === 0" description="暂无班次" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <el-empty v-else description="请选择左侧日历查看详情" />
      </div>
    </div>

    <!-- 创建/编辑日历对话框 -->
    <el-dialog
      v-model="showCalendarDialog"
      :title="editingCalendar ? '编辑日历' : '新增日历'"
      width="500px"
    >
      <el-form ref="calendarFormRef" :model="calendarForm" :rules="calendarRules" label-width="100px">
        <el-form-item label="日历名称" prop="name">
          <el-input v-model="calendarForm.name" placeholder="请输入日历名称" />
        </el-form-item>
        <el-form-item label="日历代码" prop="code">
          <el-input
            v-model="calendarForm.code"
            :disabled="!!editingCalendar"
            placeholder="请输入日历代码"
          />
        </el-form-item>
        <el-form-item label="年份" prop="year">
          <el-input-number v-model="calendarForm.year" :min="2020" :max="2099" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="calendarForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCalendarDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCalendar">保存</el-button>
      </template>
    </el-dialog>

    <!-- 班次编辑对话框 -->
    <el-dialog
      v-model="showShiftDialog"
      :title="editingShift ? '编辑班次' : '添加班次'"
      width="400px"
    >
      <el-form ref="shiftFormRef" :model="shiftForm" :rules="shiftRules" label-width="100px">
        <el-form-item label="班次名称" prop="name">
          <el-input v-model="shiftForm.name" placeholder="如：早班" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="shiftForm.startTime" format="HH:mm" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="shiftForm.endTime" format="HH:mm" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="shiftForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShiftDialog = false">取消</el-button>
        <el-button type="primary" @click="saveShift">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar,
  Plus,
  Search,
  Edit,
  Delete,
  ArrowLeft,
  ArrowRight,
  Close
} from '@element-plus/icons-vue'
import { factoryCalendarApi, type FactoryCalendar, type CalendarShift, type CalendarDate } from '@/api/factoryCalendar'

const loading = ref(false)
const calendars = ref<FactoryCalendar[]>([])
const selectedCalendar = ref<FactoryCalendar | null>(null)
const searchKeyword = ref('')
const activeTab = ref('month')

// 日历表单
const showCalendarDialog = ref(false)
const editingCalendar = ref<FactoryCalendar | null>(null)
const calendarFormRef = ref()
const calendarForm = ref({
  name: '',
  code: '',
  year: new Date().getFullYear(),
  description: ''
})
const calendarRules = {
  name: [{ required: true, message: '请输入日历名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入日历代码', trigger: 'blur' }],
  year: [{ required: true, message: '请选择年份', trigger: 'blur' }]
}

// 班次相关
const shifts = ref<CalendarShift[]>([])
const showShiftDialog = ref(false)
const editingShift = ref<CalendarShift | null>(null)
const shiftFormRef = ref()
const shiftForm = ref({
  name: '',
  startTime: '',
  endTime: '',
  sortOrder: 0
})
const shiftRules = {
  name: [{ required: true, message: '请输入班次名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'blur' }]
}

// 月视图相关
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const calendarDates = ref<CalendarDate[]>([])
const selectedDate = ref<CalendarDate | null>(null)
const dateForm = ref({
  dateType: 'WORKDAY',
  label: ''
})

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const filteredCalendars = computed(() => {
  if (!searchKeyword.value) return calendars.value
  return calendars.value.filter(
    (cal: FactoryCalendar) =>
      cal.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      cal.code.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

onMounted(async () => {
  await loadCalendars()
})

async function loadCalendars() {
  loading.value = true
  try {
    const result = await factoryCalendarApi.getCalendars()
    calendars.value = result
    if (calendars.value.length > 0 && !selectedCalendar.value) {
      await selectCalendar(calendars.value[0])
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载日历失败')
  } finally {
    loading.value = false
  }
}

async function selectCalendar(cal: FactoryCalendar) {
  selectedCalendar.value = cal
  currentYear.value = cal.year
  currentMonth.value = 1
  await loadShifts()
  await loadMonthDates()
}

async function loadShifts() {
  if (!selectedCalendar.value) return
  try {
    shifts.value = await factoryCalendarApi.getShifts(selectedCalendar.value.id)
  } catch (error: any) {
    ElMessage.error(error.message || '加载班次失败')
  }
}

async function loadMonthDates() {
  if (!selectedCalendar.value) return
  try {
    calendarDates.value = await factoryCalendarApi.getDatesByMonth(
      selectedCalendar.value.id,
      currentYear.value,
      currentMonth.value
    )
  } catch (error: any) {
    ElMessage.error(error.message || '加载日期失败')
  }
}

function previousMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  loadMonthDates()
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  loadMonthDates()
}

function getDateClass(date: CalendarDate): string {
  const classes = ['date-cell']
  if (date.dateType === 'WORKDAY') classes.push('workday')
  else if (date.dateType === 'RESTDAY') classes.push('restday')
  else if (date.dateType === 'HOLIDAY') classes.push('holiday')
  if (selectedDate.value?.date === date.date) classes.push('selected')
  return classes.join(' ')
}

function getDateTypeLabel(dateType: string): string {
  const map: Record<string, string> = {
    WORKDAY: '工',
    RESTDAY: '休',
    HOLIDAY: '假'
  }
  return map[dateType] || ''
}

function selectDate(date: CalendarDate) {
  selectedDate.value = date
  dateForm.value = {
    dateType: date.dateType,
    label: date.label || ''
  }
}

async function saveDateType() {
  if (!selectedCalendar.value || !selectedDate.value) return
  try {
    await factoryCalendarApi.updateDateType(selectedCalendar.value.id, {
      date: selectedDate.value.date,
      dateType: dateForm.value.dateType,
      label: dateForm.value.label
    })
    ElMessage.success('日期类型已更新')
    selectedDate.value = null
    await loadMonthDates()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  }
}

function handleCreate() {
  editingCalendar.value = null
  calendarForm.value = {
    name: '',
    code: '',
    year: new Date().getFullYear(),
    description: ''
  }
  showCalendarDialog.value = true
}

function handleEdit(cal: FactoryCalendar) {
  editingCalendar.value = cal
  calendarForm.value = {
    name: cal.name,
    code: cal.code,
    year: cal.year,
    description: cal.description || ''
  }
  showCalendarDialog.value = true
}

async function handleDelete(cal: FactoryCalendar) {
  try {
    await ElMessageBox.confirm(`确定删除日历 "${cal.name}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await factoryCalendarApi.deleteCalendar(cal.id)
    ElMessage.success('删除成功')
    if (selectedCalendar.value?.id === cal.id) {
      selectedCalendar.value = null
    }
    await loadCalendars()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function saveCalendar() {
  if (!calendarFormRef.value) return
  await calendarFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (editingCalendar.value) {
        await factoryCalendarApi.updateCalendar(editingCalendar.value.id, calendarForm.value)
        ElMessage.success('日历更新成功')
      } else {
        await factoryCalendarApi.createCalendar(calendarForm.value)
        ElMessage.success('日历创建成功')
      }
      showCalendarDialog.value = false
      await loadCalendars()
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
    }
  })
}

function handleAddShift() {
  editingShift.value = null
  shiftForm.value = {
    name: '',
    startTime: '',
    endTime: '',
    sortOrder: 0
  }
  showShiftDialog.value = true
}

function handleEditShift(shift: CalendarShift) {
  editingShift.value = shift
  shiftForm.value = {
    name: shift.name,
    startTime: shift.startTime,
    endTime: shift.endTime,
    sortOrder: shift.sortOrder
  }
  showShiftDialog.value = true
}

async function handleDeleteShift(shift: CalendarShift) {
  if (!selectedCalendar.value) return
  try {
    await ElMessageBox.confirm(`确定删除班次 "${shift.name}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await factoryCalendarApi.deleteShift(selectedCalendar.value.id, shift.id)
    ElMessage.success('删除成功')
    await loadShifts()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function saveShift() {
  if (!shiftFormRef.value) return
  if (!selectedCalendar.value) return
  await shiftFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (editingShift.value) {
        await factoryCalendarApi.updateShift(
          selectedCalendar.value!.id,
          editingShift.value.id,
          shiftForm.value
        )
        ElMessage.success('班次更新成功')
      } else {
        await factoryCalendarApi.addShift(selectedCalendar.value!.id, shiftForm.value)
        ElMessage.success('班次添加成功')
      }
      showShiftDialog.value = false
      await loadShifts()
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
    }
  })
}
</script>

<style scoped>
.factory-calendar-page {
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

.search-input {
  margin-bottom: 16px;
}

.calendar-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 8px;
}

.calendar-list::-webkit-scrollbar {
  width: 6px;
}

.calendar-list::-webkit-scrollbar-track {
  background: transparent;
}

.calendar-list::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.calendar-card {
  padding: 14px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.calendar-card:hover {
  border-color: #409eff;
  background: #f0f9ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.calendar-card.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.calendar-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.calendar-info {
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
}

.info-item {
  display: flex;
  gap: 4px;
  margin-bottom: 4px;
}

.info-item .label {
  font-weight: 500;
}

.calendar-actions {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}

.detail-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
}

.month-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.month-selector {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.calendar-grid {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.weekday {
  padding: 8px;
  text-align: center;
  font-weight: 600;
  font-size: 12px;
  color: #606266;
}

.date-cells {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e4e7ed;
  padding: 1px;
}

.date-cell {
  aspect-ratio: 1;
  background: white;
  padding: 4px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.3s;
  position: relative;
}

.date-cell:hover {
  background: #f0f9ff;
}

.date-cell.selected {
  background: #ecf5ff;
  border: 2px solid #409eff;
}

.date-cell.workday {
  color: #303133;
}

.date-cell.restday {
  background: #fef0f0;
  color: #f56c6c;
}

.date-cell.holiday {
  background: #fef0f0;
  color: #f56c6c;
  font-weight: 600;
}

.date-number {
  font-weight: 600;
}

.date-type-badge {
  font-size: 10px;
  margin-top: 2px;
  opacity: 0.7;
}

.date-editor {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
}

.shifts-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.shifts-header {
  display: flex;
  justify-content: flex-end;
}

.shifts-table {
  width: 100%;
}
</style>
