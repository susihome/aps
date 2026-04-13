import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi, type UserSession } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { confirmAction, confirmDanger, extractErrorMsg, msgError, msgSuccess } from '@/utils/message'

export function useSessionDevices() {
  const router = useRouter()
  const authStore = useAuthStore()

  const loading = ref(false)
  const revokingSessionId = ref<string | null>(null)
  const revokingAll = ref(false)
  const sessions = ref<UserSession[]>([])

  const sortedSessions = computed(() =>
    [...sessions.value].sort((left, right) => {
      if (left.current !== right.current) {
        return left.current ? -1 : 1
      }
      return new Date(right.createTime).getTime() - new Date(left.createTime).getTime()
    })
  )

  const activeSessionCount = computed(() => sessions.value.length)

  async function loadSessions() {
    loading.value = true
    try {
      sessions.value = await authApi.listSessions()
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '加载会话列表失败'))
    } finally {
      loading.value = false
    }
  }

  async function revokeSession(session: UserSession) {
    try {
      await confirmDanger(
        session.current
          ? '确定要退出当前设备吗？'
          : `确定要移除设备 ${describeSession(session)} 吗？`,
        '移除设备'
      )
      revokingSessionId.value = session.sessionId
      await authApi.revokeSession(session.sessionId)
      if (session.current) {
        authStore.user = null
        msgSuccess('当前设备已退出登录')
        await router.push('/login')
        return
      }
      sessions.value = sessions.value.filter(item => item.sessionId !== session.sessionId)
      msgSuccess('设备已移除')
    } catch (error: unknown) {
      if (error !== 'cancel' && error !== 'close') {
        msgError(extractErrorMsg(error, '移除设备失败'))
      }
    } finally {
      revokingSessionId.value = null
    }
  }

  async function revokeAllSessions() {
    try {
      await confirmAction('确定要退出全部设备吗？当前会话也会被一并下线。', '退出全部设备')
      revokingAll.value = true
      await authApi.logoutAll()
      authStore.user = null
      msgSuccess('已退出全部设备')
      await router.push('/login')
    } catch (error: unknown) {
      if (error !== 'cancel' && error !== 'close') {
        msgError(extractErrorMsg(error, '退出全部设备失败'))
      }
    } finally {
      revokingAll.value = false
    }
  }

  function describeSession(session: UserSession) {
    return session.clientType || session.clientIp || session.username
  }

  function formatDateTime(value: string | null) {
    if (!value) {
      return '暂无记录'
    }
    const date = new Date(value)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  }

  return {
    loading,
    revokingSessionId,
    revokingAll,
    sessions: sortedSessions,
    activeSessionCount,
    loadSessions,
    revokeSession,
    revokeAllSessions,
    describeSession,
    formatDateTime,
  }
}
