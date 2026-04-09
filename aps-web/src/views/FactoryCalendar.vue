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
                  <div class="month-actions">
                    <el-button @click="showBatchWeekendDialog = true">
                      <el-icon><Calendar /></el-icon>
                      批量设置单双休
                    </el-button>
                    <el-button type="primary" @click="showBatchHolidayDialog = true">
                      <el-icon><Calendar /></el-icon>
                      批量设置节假日
                    </el-button>
                  </div>
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
                      v-for="item in calendarGridCells"
                      :key="item.key"
                      :class="item.date ? ['date-cell', getDateClass(item.date)] : ['date-cell', 'placeholder']"
                      @click="item.date && selectDate(item.date)"
                      @contextmenu.prevent="item.date && showDateContextMenu($event, item.date)"
                    >
                      <template v-if="item.date">
                        <div class="date-number">{{ getDayOfMonth(item.date.date) }}</div>
                        <div class="date-type-badge">{{ getDateTypeLabel(item.date.dateType) }}</div>
                        <div v-if="item.date.label" class="date-label">{{ item.date.label }}</div>
                      </template>
                    </div>
                  </div>
                </div>

                <!-- 右键菜单 -->
                <div
                  v-if="contextMenuVisible"
                  :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
                  class="context-menu"
                  @click="contextMenuVisible = false"
                >
                  <div class="menu-item" @click="quickSetDateType('WORKDAY')">
                    <span class="type-badge workday-badge">工</span> 设为工作日
                  </div>
                  <div class="menu-item" @click="quickSetDateType('RESTDAY')">
                    <span class="type-badge restday-badge">休</span> 设为休息日
                  </div>
                  <div class="menu-item" @click="quickSetDateType('HOLIDAY')">
                    <span class="type-badge holiday-badge">假</span> 设为节假日
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
          <el-time-picker
            v-model="shiftForm.startTime"
            format="HH:mm"
            value-format="HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="shiftForm.endTime"
            format="HH:mm"
            value-format="HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="跨天班次">
          <el-checkbox v-model="shiftForm.nextDay">
            结束时间在次日（如20:00-8:00）
          </el-checkbox>
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

    <!-- 批量设置节假日对话框 -->
    <el-dialog
      v-model="showBatchHolidayDialog"
      title="批量设置法定节假日"
      width="600px"
    >
      <el-form :model="batchHolidayForm" label-width="100px">
        <el-form-item label="选择年份">
          <el-select v-model="batchHolidayForm.year" placeholder="请选择年份">
            <el-option
              v-for="year in SUPPORTED_YEARS"
              :key="year"
              :label="year"
              :value="year"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节假日模板">
          <el-select v-model="batchHolidayForm.template" placeholder="选择预设模板">
            <el-option
              v-for="template in holidayTemplates"
              :key="template.key"
              :label="template.name"
              :value="template.key"
            >
              <span>{{ template.name }}</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 8px">
                {{ template.description }}
              </span>
            </el-option>
            <el-option label="自定义日期" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="batchHolidayForm.template === 'custom'" label="选择日期">
          <el-date-picker
            v-model="batchHolidayForm.customDates"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="节假日名称">
          <el-input v-model="batchHolidayForm.label" placeholder="如：春节、清明节等" />
        </el-form-item>
        <el-alert
          title="提示"
          type="info"
          description="将选定日期设置为节假日（休息日）。系统会自动根据模板填充常见法定节假日日期。"
          :closable="false"
          style="margin-bottom: 16px"
        />
      </el-form>
      <template #footer>
        <el-button @click="showBatchHolidayDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBatchHolidays">确定设置</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showBatchWeekendDialog"
      title="批量设置单双休"
      width="520px"
    >
      <el-form :model="batchWeekendForm" label-width="100px">
        <el-form-item label="应用年份">
          <el-input :model-value="selectedCalendar?.year || ''" disabled />
        </el-form-item>
        <el-form-item label="休息规则">
          <el-radio-group v-model="batchWeekendForm.pattern">
            <el-radio value="SINGLE">单休（仅周日休）</el-radio>
            <el-radio value="DOUBLE">双休（周六周日休）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          title="提示"
          type="info"
          description="该操作会按所选日历年份批量重置周末规则，并保留已设置的法定节假日不变。"
          :closable="false"
          style="margin-bottom: 16px"
        />
      </el-form>
      <template #footer>
        <el-button @click="showBatchWeekendDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBatchWeekendPattern">确定设置</el-button>
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
import { parseTime, validateShiftTime, getDateRange } from '@/utils/date'
import { holidayTemplates, getHolidayDates, isYearSupported, SUPPORTED_YEARS } from '@/config/holidays'

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
  sortOrder: 0,
  nextDay: false
})
const shiftRules = {
  name: [{ required: true, message: '请输入班次名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'blur' }]
}

// 批量设置节假日
const showBatchHolidayDialog = ref(false)
const batchHolidayForm = ref({
  year: new Date().getFullYear(),
  template: '',
  customDates: [] as string[],
  label: ''
})
const showBatchWeekendDialog = ref(false)
const batchWeekendForm = ref({
  pattern: 'DOUBLE' as 'SINGLE' | 'DOUBLE'
})

// 月视图相关
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const calendarDates = ref<CalendarDate[]>([])
const selectedDate = ref<CalendarDate | null>(null)
const dateForm = ref({
  dateType: 'WORKDAY',
  label: ''
})

function getDefaultMonthForYear(year: number): number {
  const now = new Date()
  return year === now.getFullYear() ? now.getMonth() + 1 : 1
}

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuDate = ref<CalendarDate | null>(null)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

function sortCalendarDates(dates: CalendarDate[]): CalendarDate[] {
  return [...dates].sort((a, b) => a.date.localeCompare(b.date))
}

const calendarGridCells = computed(() => {
  if (calendarDates.value.length === 0) return []

  const firstDate = calendarDates.value[0]
  const firstWeekdayIndex = new Date(`${firstDate.date}T00:00:00`).getDay()
  const placeholders = Array.from({ length: firstWeekdayIndex }, (_, index) => ({
    key: `placeholder-${currentYear.value}-${currentMonth.value}-${index}`,
    date: null as CalendarDate | null
  }))

  const dateCells = calendarDates.value.map((date: CalendarDate) => ({
    key: date.date,
    date
  }))

  return [...placeholders, ...dateCells]
})

function getDayOfMonth(date: string): number {
  return Number(date.split('-')[2])
}

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
     if (selectedCalendar.value) {
       const refreshedSelectedCalendar = calendars.value.find(
         (calendar: FactoryCalendar) => calendar.id === selectedCalendar.value?.id
       )
       selectedCalendar.value = refreshedSelectedCalendar ?? null
     }
     if (calendars.value.length > 0 && !selectedCalendar.value) {
       await selectCalendar(calendars.value[0] as FactoryCalendar)
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
  currentMonth.value = getDefaultMonthForYear(cal.year)
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
    const dates = await factoryCalendarApi.getDatesByMonth(
      selectedCalendar.value.id,
      currentYear.value,
      currentMonth.value
    )
    calendarDates.value = sortCalendarDates(dates)
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

function getDateTypeName(dateType: string): string {
  const map: Record<string, string> = {
    WORKDAY: '工作日',
    RESTDAY: '休息日',
    HOLIDAY: '节假日'
  }
  return map[dateType] || '日期类型'
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
    ElMessage.success(`已设置为${getDateTypeName(dateForm.value.dateType)}`)
    selectedDate.value = null
    await loadMonthDates()
    await loadCalendars()
  } catch (error: any) {
    ElMessage.error(error.message || '设置失败')
  }
}

function showDateContextMenu(event: MouseEvent, date: CalendarDate) {
  contextMenuDate.value = date
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true

  document.addEventListener('click', () => {
    contextMenuVisible.value = false
  }, { once: true })
}

async function quickSetDateType(dateType: string) {
  if (!selectedCalendar.value || !contextMenuDate.value) return
  try {
    await factoryCalendarApi.updateDateType(selectedCalendar.value.id, {
      date: contextMenuDate.value.date,
      dateType,
      label: contextMenuDate.value.label || ''
    })
    ElMessage.success(`已设置为${getDateTypeName(dateType)}`)
    await loadMonthDates()
    await refreshCalendarListSelection()
  } catch (error: any) {
    ElMessage.error(error.message || '设置失败')
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
    ElMessage.success(`日历"${cal.name}"已删除`)
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
        ElMessage.success(`日历"${calendarForm.value.name}"已更新`)
      } else {
        await factoryCalendarApi.createCalendar(calendarForm.value)
        ElMessage.success(`日历"${calendarForm.value.name}"已创建`)
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
    sortOrder: 0,
    nextDay: false
  }
  showShiftDialog.value = true
}

function handleEditShift(shift: CalendarShift) {
  editingShift.value = shift
  shiftForm.value = {
    name: shift.name,
    startTime: shift.startTime,
    endTime: shift.endTime,
    sortOrder: shift.sortOrder,
    nextDay: shift.nextDay ?? false
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
    ElMessage.success(`班次"${shift.name}"已删除`)
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
      // 使用工具函数解析时间
      const startTime = parseTime(shiftForm.value.startTime)
      const endTime = parseTime(shiftForm.value.endTime)

      // 验证班次时间
      const validation = validateShiftTime(startTime, endTime, shiftForm.value.nextDay)
      if (!validation.valid) {
        ElMessage.error(validation.error || '时间设置不合法')
        return
      }

      const normalizedForm = {
        name: shiftForm.value.name,
        startTime,
        endTime,
        sortOrder: shiftForm.value.sortOrder,
        nextDay: shiftForm.value.nextDay
      }

      if (editingShift.value) {
        await factoryCalendarApi.updateShift(
          selectedCalendar.value!.id,
          editingShift.value.id,
          normalizedForm
        )
        ElMessage.success(`班次"${shiftForm.value.name}"已更新`)
      } else {
        await factoryCalendarApi.addShift(selectedCalendar.value!.id, normalizedForm)
        ElMessage.success(`班次"${shiftForm.value.name}"已添加`)
      }
      showShiftDialog.value = false
      await loadShifts()
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
    }
  })
}

async function saveBatchHolidays() {
  if (!selectedCalendar.value) return
  if (!batchHolidayForm.value.template) {
    ElMessage.warning('请选择节假日模板或自定义日期')
    return
  }

  // 检查年份是否支持
  if (!isYearSupported(batchHolidayForm.value.year)) {
    ElMessage.warning(`年份 ${batchHolidayForm.value.year} 暂不支持，支持的年份：${SUPPORTED_YEARS.join(', ')}`)
    return
  }

  let dates: string[] = []
  if (batchHolidayForm.value.template === 'custom') {
    if (batchHolidayForm.value.customDates.length !== 2) {
      ElMessage.warning('请选择开始和结束日期')
      return
    }
    const [start, end] = batchHolidayForm.value.customDates
    try {
      dates = getDateRange(start, end)
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '日期范围无效')
      return
    }
  } else {
    try {
      dates = getHolidayDates(batchHolidayForm.value.template, batchHolidayForm.value.year)
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '获取节假日日期失败')
      return
    }
  }

  if (dates.length === 0) {
    ElMessage.warning('未获取到节假日日期，请检查年份和模板配置')
    return
  }

  try {
    await factoryCalendarApi.batchSetHolidays(selectedCalendar.value.id, {
      dates,
      label: batchHolidayForm.value.label
    })
    ElMessage.success(`已设置${dates.length}个${batchHolidayForm.value.label || '节假日'}`)
    showBatchHolidayDialog.value = false
    batchHolidayForm.value = {
      year: new Date().getFullYear(),
      template: '',
      customDates: [],
      label: ''
    }
    await loadMonthDates()
    await refreshCalendarListSelection()
  } catch (error: any) {
    ElMessage.error(error.message || '设置失败')
  }
}

async function saveBatchWeekendPattern() {
  if (!selectedCalendar.value) return
  if (!['SINGLE', 'DOUBLE'].includes(batchWeekendForm.value.pattern)) {
    ElMessage.warning('请选择有效的休息规则')
    return
  }
  try {
    await factoryCalendarApi.applyWeekendPattern(selectedCalendar.value.id, {
      pattern: batchWeekendForm.value.pattern
    })
    ElMessage.success(batchWeekendForm.value.pattern === 'SINGLE' ? '已设置为单休' : '已设置为双休')
    showBatchWeekendDialog.value = false
    await loadMonthDates()
    await refreshCalendarListSelection()
  } catch (error: any) {
    ElMessage.error(error.message || '设置失败')
  }
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
  overflow: auto;
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
  min-height: 0;
}

:deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.month-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.month-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.month-actions {
  display: flex;
  gap: 12px;
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

.date-cell.placeholder {
  cursor: default;
  background: white;
}

.date-cell:hover {
  background: #f0f9ff;
}

.date-cell.placeholder:hover {
  background: white;
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

.date-label {
  font-size: 9px;
  margin-top: 1px;
  opacity: 0.6;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 150px;
}

.menu-item {
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.workday-badge {
  background-color: #409eff;
}

.restday-badge {
  background-color: #f56c6c;
}

.holiday-badge {
  background-color: #e6a23c;
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
