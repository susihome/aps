<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import Gantt from 'frappe-gantt'
import 'frappe-gantt/dist/frappe-gantt.css'
import { useScheduleStore } from '../stores/schedule'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ElMessage } from 'element-plus'

interface GanttTask {
  id: string
  name: string
  start: string
  end: string
  progress: number
  dependencies: string
}

const scheduleStore = useScheduleStore()
const ganttContainer = ref<HTMLElement>()
let ganttInstance: Gantt | null = null
let stompClient: Client | null = null

onMounted(() => {
  initGantt()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate()
  }
})

function initGantt() {
  const tasks: GanttTask[] = [
    {
      id: '1',
      name: '工序1',
      start: '2026-04-02',
      end: '2026-04-03',
      progress: 0,
      dependencies: ''
    }
  ]

  if (ganttContainer.value) {
    try {
      ganttInstance = new Gantt(ganttContainer.value, tasks, {
        view_mode: 'Day',
        on_click: (task: GanttTask) => {
          // 任务点击事件
        },
        on_date_change: (task: GanttTask, start: Date, end: Date) => {
          // 任务日期变更事件
        }
      })
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : '初始化甘特图失败'
      ElMessage.error(message)
    }
  }
}

function connectWebSocket() {
  try {
    stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws/schedule-progress'),
      onConnect: () => {
        stompClient?.subscribe('/topic/schedule/1', (message) => {
          try {
            const data = JSON.parse(message.body)
            if (data.type === 'PROGRESS') {
              scheduleStore.updateProgress(data.progress)
            }
          } catch (error: unknown) {
            const errorMessage = error instanceof Error ? error.message : 'WebSocket消息解析失败'
            ElMessage.error(errorMessage)
          }
        })
      },
      onStompError: (frame) => {
        ElMessage.error('WebSocket连接错误')
      },
      onWebSocketError: () => {
        ElMessage.error('WebSocket连接失败')
      }
    })
    stompClient.activate()
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'WebSocket初始化失败'
    ElMessage.error(message)
  }
}
</script>

<template>
  <div class="gantt-wrapper">
    <div class="toolbar">
      <el-button
        type="primary"
        @click="scheduleStore.triggerSolve"
        class="solve-button"
        aria-label="开始排产"
      >
        开始排产
      </el-button>
      <el-progress
        :percentage="scheduleStore.progress"
        :style="{ width: '300px', marginLeft: '20px' }"
        :stroke-width="8"
        aria-label="排产进度"
      />
    </div>
    <div
      ref="ganttContainer"
      class="gantt-container"
      role="region"
      aria-label="甘特图"
    ></div>
  </div>
</template>

<style scoped>
.gantt-wrapper {
  padding: 20px;
  animation: fadeIn 300ms ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
  padding: 20px;
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.solve-button {
  min-height: 44px;
  padding: 0 24px;
  transition: all 200ms ease;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  color: white;
  box-shadow: var(--shadow-sm);
}

.solve-button:hover {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.solve-button:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.solve-button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.toolbar :deep(.el-progress) {
  flex: 1;
  min-width: 300px;
}

.toolbar :deep(.el-progress__text) {
  color: var(--color-text);
  font-weight: 600;
}

.toolbar :deep(.el-progress-bar__outer) {
  background: var(--color-surface-light);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.toolbar :deep(.el-progress-bar__inner) {
  background: var(--color-primary);
  border-radius: var(--radius-sm);
}

.gantt-container {
  overflow: auto;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: white;
  min-height: 400px;
  box-shadow: var(--shadow-sm);
}

/* Frappe Gantt customization */
.gantt-container :deep(.gantt) {
  background: white;
}

.gantt-container :deep(.gantt .grid-background) {
  fill: #F8FAFC;
}

.gantt-container :deep(.gantt .grid-row) {
  fill: transparent;
}

.gantt-container :deep(.gantt .grid-header) {
  fill: #F1F5F9;
  stroke: var(--color-border);
}

.gantt-container :deep(.gantt .tick) {
  stroke: var(--color-border);
}

.gantt-container :deep(.gantt .today-highlight) {
  fill: rgba(37, 99, 235, 0.08);
  stroke: var(--color-primary);
}

.gantt-container :deep(.gantt .bar) {
  fill: var(--color-primary);
  stroke: var(--color-border);
}

.gantt-container :deep(.gantt .bar-progress) {
  fill: var(--color-secondary);
}

.gantt-container :deep(.gantt .bar-label) {
  fill: white;
  font-weight: 600;
}

.gantt-container :deep(.gantt .arrow) {
  stroke: var(--color-text-secondary);
}

/* 响应式 */
@media (max-width: 768px) {
  .gantt-wrapper {
    padding: 16px;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
    padding: 16px;
  }

  .toolbar :deep(.el-progress) {
    margin-left: 0 !important;
    width: 100% !important;
    min-width: 0;
  }

  .solve-button {
    width: 100%;
  }
}

/* 支持 prefers-reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .gantt-wrapper,
  .solve-button {
    animation: none;
    transition: none;
  }

  .solve-button:hover,
  .solve-button:active {
    transform: none;
  }
}
</style>
