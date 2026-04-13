<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Connection, Delete, SwitchButton } from '@element-plus/icons-vue'
import { useSessionDevices } from '@/composables/useSessionDevices'

const {
  loading,
  revokingSessionId,
  revokingAll,
  sessions,
  activeSessionCount,
  loadSessions,
  revokeSession,
  revokeAllSessions,
  describeSession,
  formatDateTime,
} = useSessionDevices()

const currentSession = computed(() => sessions.value.find(session => session.current) ?? null)
const otherSessions = computed(() => sessions.value.filter(session => !session.current))

onMounted(() => {
  loadSessions()
})
</script>

<template>
  <div class="session-devices-page">
    <div class="page-header">
      <div>
        <h2>设备管理</h2>
        <p>查看当前账号的在线设备，并支持移除指定会话。</p>
      </div>
      <div class="page-actions">
        <el-button :loading="loading" @click="loadSessions">刷新</el-button>
        <el-button
          type="danger"
          :loading="revokingAll"
          :disabled="activeSessionCount === 0"
          @click="revokeAllSessions"
        >
          <el-icon><SwitchButton /></el-icon>
          退出全部设备
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">活动会话</div>
          <div class="summary-value">{{ activeSessionCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">当前设备</div>
          <div class="summary-value">{{ currentSession ? describeSession(currentSession) : '无' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="sessions-card">
      <template #header>
        <div class="card-header">
          <span>在线设备</span>
          <span class="header-meta">共 {{ activeSessionCount }} 个会话</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="sessions" row-key="sessionId" stripe>
        <el-table-column label="设备" min-width="220">
          <template #default="{ row }">
            <div class="device-name">
              <el-icon class="device-icon"><Connection /></el-icon>
              <div>
                <div class="device-title">
                  {{ describeSession(row) }}
                  <el-tag v-if="row.current" size="small" type="success">当前设备</el-tag>
                </div>
                <div class="device-subtitle">{{ row.userAgent || '未记录客户端标识' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="clientIp" label="IP 地址" min-width="140" />
        <el-table-column label="登录时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="最近访问" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.lastAccessAt) }}</template>
        </el-table-column>
        <el-table-column label="到期时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.expiresAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="danger"
              :loading="revokingSessionId === row.sessionId"
              @click="revokeSession(row)"
            >
              <el-icon><Delete /></el-icon>
              {{ row.current ? '退出当前' : '移除设备' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && sessions.length === 0"
        description="当前没有可用会话记录"
      />
    </el-card>

    <el-card v-if="otherSessions.length > 0" shadow="never" class="tips-card">
      <div class="tips-title">建议</div>
      <p>如果发现陌生设备或异常 IP，请立即移除设备，并重新登录以刷新当前会话。</p>
    </el-card>
  </div>
</template>

<style scoped>
.session-devices-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 8px;
  color: #1e293b;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.page-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.summary-row {
  margin: 0;
}

.summary-card {
  border-radius: 12px;
}

.summary-label {
  color: #64748b;
  font-size: 13px;
}

.summary-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.sessions-card,
.tips-card {
  border-radius: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.header-meta {
  color: #64748b;
  font-size: 13px;
}

.device-name {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.device-icon {
  margin-top: 2px;
  color: #2563eb;
}

.device-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1e293b;
}

.device-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
  word-break: break-all;
}

.tips-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.tips-card p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .page-actions {
    width: 100%;
  }
}
</style>
