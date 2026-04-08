<template>
  <div class="audit-log-page">
    <h2>审计日志</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalLogs }}</div>
              <div class="stat-label">总日志数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="32"><Operation /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalActions }}</div>
              <div class="stat-label">操作类型</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>操作类型分布</span>
            </div>
          </template>
          <div ref="actionChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户活跃度 TOP 10</span>
            </div>
          </template>
          <div ref="userChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <!-- 搜索过滤区域 -->
      <div class="search-toolbar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select
              v-model="searchForm.action"
              placeholder="请选择操作类型"
              clearable
              style="width: 180px"
            >
              <el-option label="全部" value="" />
              <el-option label="登录" value="LOGIN" />
              <el-option label="登出" value="LOGOUT" />
              <el-option label="创建排产" value="SCHEDULE_CREATE" />
              <el-option label="修改排产" value="SCHEDULE_UPDATE" />
              <el-option label="启动求解" value="SCHEDULE_SOLVE" />
              <el-option label="停止求解" value="SCHEDULE_STOP" />
              <el-option label="创建工单" value="ORDER_CREATE" />
              <el-option label="修改工单" value="ORDER_UPDATE" />
              <el-option label="删除工单" value="ORDER_DELETE" />
              <el-option label="创建用户" value="USER_CREATE" />
              <el-option label="修改用户" value="USER_UPDATE" />
              <el-option label="删除用户" value="USER_DELETE" />
              <el-option label="分配角色" value="ROLE_ASSIGN" />
              <el-option label="移除角色" value="ROLE_REMOVE" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源类型">
            <el-input
              v-model="searchForm.resource"
              placeholder="请输入资源类型"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 360px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              <span>搜索</span>
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              <span>重置</span>
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              <span>导出</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="auditLogs"
        style="width: 100%"
        v-loading="loading"
        @expand-change="handleExpandChange"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <h4>详细信息</h4>
              <pre>{{ formatDetails(row.details) }}</pre>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="timestamp" label="操作时间" min-width="160" sortable>
          <template #default="{ row }">
            {{ formatDate(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="action" label="操作类型" min-width="140">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.action)" size="small">
              {{ getActionLabel(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="资源类型" min-width="120" />
        <el-table-column prop="ipAddress" label="IP地址" min-width="140" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Document, User, Operation } from '@element-plus/icons-vue'
import { searchAuditLogs, exportAuditLogs, getStatistics, type AuditLog } from '@/api/audit'
import * as echarts from 'echarts'

const searchForm = reactive({
  username: '',
  action: '',
  resource: '',
  timeRange: [] as Date[]
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const auditLogs = ref<AuditLog[]>([])
const loading = ref(false)
const totalLogs = ref(0)
const activeUsers = ref(0)
const totalActions = ref(0)

const actionChartRef = ref<HTMLElement>()
const userChartRef = ref<HTMLElement>()
let actionChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null

const loadAuditLogs = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }

    if (searchForm.username) params.username = searchForm.username
    if (searchForm.action) params.action = searchForm.action
    if (searchForm.resource) params.resource = searchForm.resource
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startTime = searchForm.timeRange[0].toISOString()
      params.endTime = searchForm.timeRange[1].toISOString()
    }

    const response = await searchAuditLogs(params)
    if (response.data.code === 200 && response.data.data) {
      auditLogs.value = response.data.data.content
      pagination.total = response.data.data.totalElements
      totalLogs.value = response.data.data.totalElements
    }
  } catch (error) {
    ElMessage.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    let startTime: string | undefined
    let endTime: string | undefined

    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      startTime = searchForm.timeRange[0].toISOString()
      endTime = searchForm.timeRange[1].toISOString()
    }

    const response = await getStatistics(startTime, endTime)
    if (response.data.code === 200 && response.data.data) {
      const stats = response.data.data
      activeUsers.value = Object.keys(stats.userStatistics).length
      totalActions.value = Object.keys(stats.actionStatistics).length

      await nextTick()
      updateActionChart(stats.actionStatistics)
      updateUserChart(stats.userStatistics)
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const updateActionChart = (data: Record<string, number>) => {
  if (!actionChartRef.value) return
  if (!actionChart) actionChart = echarts.init(actionChartRef.value)

  const chartData = Object.entries(data).map(([name, value]) => ({
    name: getActionLabel(name),
    value
  }))

  actionChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center', type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData
    }]
  })
}

const updateUserChart = (data: Record<string, number>) => {
  if (!userChartRef.value) return
  if (!userChart) userChart = echarts.init(userChartRef.value)

  const sortedData = Object.entries(data).sort((a, b) => b[1] - a[1]).slice(0, 10)

  userChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sortedData.map(([name]) => name) },
    series: [{
      type: 'bar',
      data: sortedData.map(([, value]) => value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#83bff6' },
          { offset: 1, color: '#188df0' }
        ])
      },
      barWidth: '60%'
    }]
  })
}

const handleSearch = () => {
  pagination.page = 1
  loadAuditLogs()
  loadStatistics()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.action = ''
  searchForm.resource = ''
  searchForm.timeRange = []
  pagination.page = 1
  loadAuditLogs()
  loadStatistics()
}

const handleExport = async () => {
  try {
    let startTime: string | undefined
    let endTime: string | undefined

    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      startTime = searchForm.timeRange[0].toISOString()
      endTime = searchForm.timeRange[1].toISOString()
    }

    const response = await exportAuditLogs(startTime, endTime)
    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `audit_logs_${new Date().getTime()}.csv`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadAuditLogs()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadAuditLogs()
}

const handleExpandChange = (row: AuditLog) => {}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatDetails = (details: string) => {
  if (!details) return '无'
  try {
    return JSON.stringify(JSON.parse(details), null, 2)
  } catch {
    return details
  }
}

const getActionTagType = (action: string) => {
  const typeMap: Record<string, string> = {
    LOGIN: 'success', LOGOUT: 'info',
    SCHEDULE_CREATE: 'primary', SCHEDULE_UPDATE: 'warning', SCHEDULE_DELETE: 'danger',
    ORDER_CREATE: 'primary', ORDER_UPDATE: 'warning', ORDER_DELETE: 'danger',
    USER_CREATE: 'primary', USER_UPDATE: 'warning', USER_DELETE: 'danger'
  }
  return typeMap[action] || 'info'
}

const getActionLabel = (action: string) => {
  const labelMap: Record<string, string> = {
    LOGIN: '登录', LOGOUT: '登出',
    SCHEDULE_CREATE: '创建排产', SCHEDULE_UPDATE: '修改排产', SCHEDULE_DELETE: '删除排产',
    SCHEDULE_SOLVE: '启动求解', SCHEDULE_STOP: '停止求解',
    ORDER_CREATE: '创建工单', ORDER_UPDATE: '修改工单', ORDER_DELETE: '删除工单',
    USER_CREATE: '创建用户', USER_UPDATE: '修改用户', USER_DELETE: '删除用户',
    ROLE_ASSIGN: '分配角色', ROLE_REMOVE: '移除角色'
  }
  return labelMap[action] || action
}

onMounted(() => {
  loadAuditLogs()
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  actionChart?.dispose()
  userChart?.dispose()
  actionChart = null
  userChart = null
})

const handleResize = () => {
  actionChart?.resize()
  userChart?.resize()
}
</script>

<style scoped>
.audit-log-page {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.search-toolbar {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.expand-content {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.expand-content h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.expand-content pre {
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
}
</style>
