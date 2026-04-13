import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { scheduleApi } from '@/api/schedule'

export const useScheduleStore = defineStore('schedule', () => {
  const progress = ref(0)
  const currentScheduleId = ref<string | null>(null)
  const currentTaskId = ref<string | null>(null)
  const progressMessage = ref('等待开始排产')
  const currentScore = ref<string | null>(null)
  const currentTaskStatus = ref<string | null>(null)
  const isSolving = computed(() => currentTaskStatus.value === 'PENDING' || currentTaskStatus.value === 'RUNNING')

  async function triggerSolve() {
    if (!currentScheduleId.value) {
      const schedule = await scheduleApi.create()
      currentScheduleId.value = schedule.id
    }
    const task = await scheduleApi.solve(currentScheduleId.value)
    currentTaskId.value = task.taskId
    currentTaskStatus.value = task.status
    progress.value = task.progress ?? 0
    currentScore.value = task.score ?? null
    progressMessage.value = task.status === 'PENDING' ? '求解任务已提交，等待 worker 执行' : '开始求解'
  }

  async function loadLatestTask(scheduleId: string) {
    currentScheduleId.value = scheduleId
    const task = await scheduleApi.getLatestSolverTask(scheduleId)
    currentTaskId.value = task.taskId
    currentTaskStatus.value = task.status
    progress.value = task.progress ?? 0
    currentScore.value = task.score ?? null
    progressMessage.value = task.errorMessage ?? task.status
  }

  function updateProgress(payload: { progress: number; message?: string; currentScore?: string | null }) {
    progress.value = payload.progress
    progressMessage.value = payload.message || progressMessage.value
    currentScore.value = payload.currentScore ?? currentScore.value
    currentTaskStatus.value = payload.progress >= 100 ? 'SUCCESS' : 'RUNNING'
  }

  function markStopped() {
    currentTaskStatus.value = 'STOPPED'
    progressMessage.value = '求解已手动停止'
  }

  function markFailed(message: string) {
    currentTaskStatus.value = 'FAILED'
    progressMessage.value = message
  }

  function resetProgress() {
    progress.value = 0
    progressMessage.value = '等待开始排产'
    currentScore.value = null
    currentTaskStatus.value = null
  }

  return {
    progress,
    currentScheduleId,
    currentTaskId,
    currentScore,
    currentTaskStatus,
    progressMessage,
    isSolving,
    triggerSolve,
    loadLatestTask,
    updateProgress,
    markStopped,
    markFailed,
    resetProgress
  }
})
