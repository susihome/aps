import { onUnmounted, watch } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { msgError } from '@/utils/message'
import { useScheduleStore } from '@/stores/schedule'

interface ScheduleProgressMessage {
  type?: string
  progress?: number
  message?: string
  currentScore?: string | null
}

export function useScheduleProgress() {
  const scheduleStore = useScheduleStore()
  let stompClient: Client | null = null
  let subscribedScheduleId: string | null = null

  function connect(scheduleId: string) {
    if (subscribedScheduleId === scheduleId && stompClient?.connected) {
      return
    }

    disconnect()
    subscribedScheduleId = scheduleId
    stompClient = new Client({
      reconnectDelay: 5000,
      webSocketFactory: () => new SockJS('/ws/schedule-progress'),
      onConnect: () => {
        stompClient?.subscribe(`/topic/schedule/${scheduleId}`, (frame) => {
          try {
            const payload = JSON.parse(frame.body) as ScheduleProgressMessage
            handleMessage(payload)
          } catch (error: unknown) {
            msgError(error instanceof Error ? error.message : 'WebSocket消息解析失败')
          }
        })
      },
      onStompError: () => {
        msgError('排产进度订阅失败')
      },
      onWebSocketError: () => {
        msgError('排产进度连接失败')
      }
    })
    stompClient.activate()
  }

  function disconnect() {
    if (stompClient) {
      void stompClient.deactivate()
      stompClient = null
    }
    subscribedScheduleId = null
  }

  function handleMessage(payload: ScheduleProgressMessage) {
    switch (payload.type) {
      case 'PROGRESS':
        scheduleStore.updateProgress({
          progress: payload.progress ?? 0,
          message: payload.message,
          currentScore: payload.currentScore
        })
        break
      case 'COMPLETED':
        scheduleStore.updateProgress({
          progress: 100,
          message: '排产完成',
          currentScore: payload.currentScore
        })
        break
      case 'CONFLICT':
        scheduleStore.markFailed(payload.message || '排产执行失败')
        break
      default:
        break
    }
  }

  watch(
    () => scheduleStore.currentScheduleId,
    (scheduleId) => {
      if (scheduleId) {
        connect(scheduleId)
        return
      }
      disconnect()
    },
    { immediate: true }
  )

  onUnmounted(() => {
    disconnect()
  })

  return {
    connect,
    disconnect
  }
}
