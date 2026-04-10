<template>
  <div class="workshop-management-page">
    <div class="page-header">
      <div>
        <h2>工厂建模</h2>
        <p>维护车间与注塑机基础数据</p>
      </div>
      <el-button type="primary" @click="openWorkshopDialog()">
        <el-icon><Plus /></el-icon>
        新建车间
      </el-button>
    </div>

    <div class="content-wrapper">
      <div class="left-panel">
        <div class="panel-title">车间列表</div>
        <div v-loading="loadingWorkshops" class="workshop-list">
          <div
            v-for="workshop in workshops"
            :key="workshop.id"
            :class="['workshop-card', { active: selectedWorkshop?.id === workshop.id }]"
            @click="selectWorkshop(workshop)"
          >
            <div class="workshop-card-header">
              <div class="workshop-name">{{ workshop.name }}</div>
              <el-tag :type="workshop.enabled ? 'success' : 'info'" size="small">
                {{ workshop.enabled ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="workshop-meta">编码：{{ workshop.code }}</div>
            <div class="workshop-meta">负责人：{{ workshop.managerName || '-' }}</div>
            <div class="workshop-meta">日历：{{ workshop.calendarName || '默认日历' }}</div>
          </div>
          <el-empty v-if="!workshops.length" description="暂无车间" />
        </div>
      </div>

      <div class="right-panel">
        <template v-if="selectedWorkshop">
          <div class="detail-header">
            <div>
              <h3>{{ selectedWorkshop.name }}</h3>
              <div class="detail-subtitle">
                编码 {{ selectedWorkshop.code }}
                <span class="separator">|</span>
                日历 {{ effectiveCalendarName }}
                <span class="separator">|</span>
                负责人 {{ selectedWorkshop.managerName || '-' }}
              </div>
            </div>
            <div class="detail-actions">
              <el-button @click="openWorkshopDialog(selectedWorkshop)">
                <el-icon><Edit /></el-icon>
                编辑车间
              </el-button>
              <el-button type="danger" plain @click="deleteWorkshop(selectedWorkshop)">
                <el-icon><Delete /></el-icon>
                删除车间
              </el-button>
            </div>
          </div>

          <div class="machine-toolbar">
            <div class="panel-title">注塑机列表</div>
            <el-button type="primary" @click="openMachineDialog()">
              <el-icon><Plus /></el-icon>
              添加注塑机
            </el-button>
          </div>

          <el-table v-loading="loadingResources" :data="resources" stripe>
            <el-table-column prop="resourceCode" label="编码" width="120" />
            <el-table-column prop="resourceName" label="名称" width="140" />
            <el-table-column prop="tonnage" label="吨位" width="100">
              <template #default="{ row }">
                {{ row.tonnage ? `${row.tonnage}T` : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="machineBrand" label="品牌" width="120" />
            <el-table-column prop="machineModel" label="型号" width="140" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="calendarName" label="设备日历" min-width="140">
              <template #default="{ row }">
                {{ row.calendarName || '继承车间/默认' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" @click="openMachineDialog(row)">编辑</el-button>
                <el-button text type="danger" @click="deleteMachine(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <el-empty v-else description="请选择左侧车间" />
      </div>
    </div>

    <el-dialog v-model="showWorkshopDialog" :title="editingWorkshop ? '编辑车间' : '新建车间'" width="520px">
      <el-form ref="workshopFormRef" :model="workshopForm" :rules="workshopRules" label-width="100px">
        <el-form-item label="车间编码" prop="code">
          <el-input v-model="workshopForm.code" :disabled="!!editingWorkshop" />
        </el-form-item>
        <el-form-item label="车间名称" prop="name">
          <el-input v-model="workshopForm.name" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="workshopForm.managerName" />
        </el-form-item>
        <el-form-item label="关联日历">
          <el-select v-model="workshopForm.calendarId" clearable placeholder="为空时继承默认日历">
            <el-option
              v-for="calendar in factoryCalendars"
              :key="calendar.id"
              :label="`${calendar.name} (${calendar.year})`"
              :value="calendar.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="workshopForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用状态" v-if="editingWorkshop">
          <el-switch v-model="workshopForm.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="workshopForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWorkshopDialog = false">取消</el-button>
        <el-button type="primary" @click="saveWorkshop">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMachineDialog" :title="editingMachine ? '编辑注塑机' : '添加注塑机'" width="620px">
      <el-form ref="machineFormRef" :model="machineForm" :rules="machineRules" label-width="110px">
        <el-form-item label="资源编码" prop="resourceCode">
          <el-input v-model="machineForm.resourceCode" :disabled="!!editingMachine" />
        </el-form-item>
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="machineForm.resourceName" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-input v-model="machineForm.resourceType" placeholder="如：INJECTION_MACHINE" />
        </el-form-item>
        <el-form-item label="吨位">
          <el-input-number v-model="machineForm.tonnage" :min="0" />
        </el-form-item>
        <el-form-item label="机台品牌">
          <el-input v-model="machineForm.machineBrand" />
        </el-form-item>
        <el-form-item label="机台型号">
          <el-input v-model="machineForm.machineModel" />
        </el-form-item>
        <el-form-item label="最大射出量(g)">
          <el-input-number v-model="machineForm.maxShotWeight" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="机台状态">
          <el-select v-model="machineForm.status" placeholder="请选择状态">
            <el-option
              v-for="item in machineStatusItems"
              :key="item.itemCode"
              :label="item.itemName"
              :value="item.itemCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备日历">
          <el-select v-model="machineForm.calendarId" clearable placeholder="为空时继承车间/默认日历">
            <el-option
              v-for="calendar in factoryCalendars"
              :key="calendar.id"
              :label="`${calendar.name} (${calendar.year})`"
              :value="calendar.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="可用" v-if="editingMachine">
          <el-switch v-model="machineForm.available" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMachineDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMachine">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { msgSuccess, msgError, confirmDanger, extractErrorMsg } from '@/utils/message'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { factoryCalendarApi, type FactoryCalendar } from '@/api/factoryCalendar'
import { resourceApi, workshopApi, type MachineResource, type MachineStatus, type Workshop } from '@/api/workshop'
import { dictionaryApi, type DictItem } from '@/api/dictionary'

const loadingWorkshops = ref(false)
const loadingResources = ref(false)
const workshops = ref<Workshop[]>([])
const resources = ref<MachineResource[]>([])
const factoryCalendars = ref<FactoryCalendar[]>([])
const selectedWorkshop = ref<Workshop | null>(null)
const effectiveCalendarName = ref('默认日历')
const machineStatusItems = ref<DictItem[]>([])
const machineStatusLabelMap = ref<Record<string, string>>({})

const showWorkshopDialog = ref(false)
const editingWorkshop = ref<Workshop | null>(null)
const workshopFormRef = ref()
const workshopForm = ref({
  code: '',
  name: '',
  managerName: '',
  calendarId: null as string | null,
  sortOrder: 0,
  description: '',
  enabled: true
})
const workshopRules = {
  code: [{ required: true, message: '请输入车间编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入车间名称', trigger: 'blur' }]
}

const showMachineDialog = ref(false)
const editingMachine = ref<MachineResource | null>(null)
const machineFormRef = ref()
const machineForm = ref({
  resourceCode: '',
  resourceName: '',
  resourceType: 'INJECTION_MACHINE',
  tonnage: undefined as number | undefined,
  machineBrand: '',
  machineModel: '',
  maxShotWeight: undefined as number | undefined,
  status: 'IDLE' as MachineStatus,
  calendarId: null as string | null,
  available: true
})
const machineRules = {
  resourceCode: [{ required: true, message: '请输入资源编码', trigger: 'blur' }],
  resourceName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }]
}

onMounted(async () => {
  await Promise.all([loadWorkshops(), loadFactoryCalendars(), loadMachineStatusDict()])
})

async function loadMachineStatusDict() {
  try {
    machineStatusItems.value = await dictionaryApi.getEnabledItemsByTypeCode('MACHINE_STATUS')
    machineStatusLabelMap.value = Object.fromEntries(
      machineStatusItems.value.map(item => [item.itemCode, item.itemName])
    )
  } catch {
    machineStatusItems.value = []
  }
}

async function loadFactoryCalendars() {
  try {
    factoryCalendars.value = await factoryCalendarApi.getCalendars()
  } catch (error: unknown) {
    msgError(error instanceof Error ? error.message : '加载工厂日历失败')
  }
}

async function loadWorkshops() {
  loadingWorkshops.value = true
  try {
    workshops.value = await workshopApi.getAll()
    if (workshops.value.length > 0) {
      const next = selectedWorkshop.value
        ? workshops.value.find(item => item.id === selectedWorkshop.value?.id) ?? workshops.value[0]
        : workshops.value[0]
      await selectWorkshop(next)
    } else {
      selectedWorkshop.value = null
      resources.value = []
      effectiveCalendarName.value = '默认日历'
    }
  } catch (error: unknown) {
    msgError(error instanceof Error ? error.message : '加载车间失败')
  } finally {
    loadingWorkshops.value = false
  }
}

async function selectWorkshop(workshop: Workshop) {
  selectedWorkshop.value = workshop
  await Promise.all([loadResources(workshop.id), loadEffectiveCalendar(workshop.id)])
}

async function loadResources(workshopId: string) {
  loadingResources.value = true
  try {
    resources.value = await resourceApi.getAll({ workshopId })
  } catch (error: unknown) {
    msgError(error instanceof Error ? error.message : '加载注塑机失败')
  } finally {
    loadingResources.value = false
  }
}

async function loadEffectiveCalendar(workshopId: string) {
  try {
    const calendar = await workshopApi.getEffectiveCalendar(workshopId)
    effectiveCalendarName.value = calendar?.name || '默认日历'
  } catch {
    effectiveCalendarName.value = '默认日历'
  }
}

function openWorkshopDialog(workshop?: Workshop) {
  editingWorkshop.value = workshop ?? null
  workshopForm.value = workshop
    ? {
        code: workshop.code,
        name: workshop.name,
        managerName: workshop.managerName || '',
        calendarId: workshop.calendarId,
        sortOrder: workshop.sortOrder || 0,
        description: workshop.description || '',
        enabled: workshop.enabled
      }
    : {
        code: '',
        name: '',
        managerName: '',
        calendarId: null,
        sortOrder: 0,
        description: '',
        enabled: true
      }
  showWorkshopDialog.value = true
}

async function saveWorkshop() {
  if (!workshopFormRef.value) return
  await workshopFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (editingWorkshop.value) {
        await workshopApi.update(editingWorkshop.value.id, workshopForm.value)
        msgSuccess('车间已更新')
      } else {
        await workshopApi.create(workshopForm.value)
        msgSuccess('车间已创建')
      }
      showWorkshopDialog.value = false
      await loadWorkshops()
    } catch (error: unknown) {
      msgError(error instanceof Error ? error.message : '保存车间失败')
    }
  })
}

async function deleteWorkshop(workshop: Workshop) {
  try {
    await confirmDanger(`确定删除车间“${workshop.name}”吗？`)
    await workshopApi.delete(workshop.id)
    msgSuccess('车间已删除')
    await loadWorkshops()
  } catch (error: any) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除车间失败'))
    }
  }
}

function openMachineDialog(machine?: MachineResource) {
  editingMachine.value = machine ?? null
  machineForm.value = machine
    ? {
        resourceCode: machine.resourceCode,
        resourceName: machine.resourceName,
        resourceType: machine.resourceType || 'INJECTION_MACHINE',
        tonnage: machine.tonnage ?? undefined,
        machineBrand: machine.machineBrand || '',
        machineModel: machine.machineModel || '',
        maxShotWeight: machine.maxShotWeight ?? undefined,
        status: machine.status || 'IDLE',
        calendarId: machine.calendarId,
        available: machine.available
      }
    : {
        resourceCode: '',
        resourceName: '',
        resourceType: 'INJECTION_MACHINE',
        tonnage: undefined,
        machineBrand: '',
        machineModel: '',
        maxShotWeight: undefined,
        status: 'IDLE',
        calendarId: null,
        available: true
      }
  showMachineDialog.value = true
}

async function saveMachine() {
  if (!machineFormRef.value || !selectedWorkshop.value) return
  await machineFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      const payload = {
        ...machineForm.value,
        workshopId: selectedWorkshop.value!.id
      }
      if (editingMachine.value) {
        await resourceApi.update(editingMachine.value.id, payload)
        msgSuccess('注塑机已更新')
      } else {
        await resourceApi.create(payload)
        msgSuccess('注塑机已创建')
      }
      showMachineDialog.value = false
      await Promise.all([
        loadResources(selectedWorkshop.value!.id),
        loadWorkshops()
      ])
    } catch (error: unknown) {
      msgError(error instanceof Error ? error.message : '保存注塑机失败')
    }
  })
}

async function deleteMachine(machine: MachineResource) {
  try {
    await confirmDanger(`确定删除注塑机“${machine.resourceName}”吗？`)
    await resourceApi.delete(machine.id)
    msgSuccess('注塑机已删除')
    if (selectedWorkshop.value) {
      await loadResources(selectedWorkshop.value.id)
    }
  } catch (error: unknown) {
    if (error !== 'cancel') {
      msgError(extractErrorMsg(error, '删除注塑机失败'))
    }
  }
}

function getStatusLabel(status: MachineStatus | null): string {
  if (!status) return '-'
  return machineStatusLabelMap.value[status] ?? status
}

function getStatusTagType(status: MachineStatus | null): 'success' | 'info' | 'warning' | 'danger' {
  switch (status) {
    case 'RUNNING':
      return 'success'
    case 'IDLE':
      return 'info'
    case 'MAINTENANCE':
      return 'warning'
    case 'DISABLED':
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.workshop-management-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px;
}

.page-header p {
  margin: 0;
  color: #606266;
}

.content-wrapper {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 180px);
}

.left-panel,
.right-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.left-panel {
  width: 320px;
}

.right-panel {
  flex: 1;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.workshop-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.workshop-card {
  padding: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.workshop-card:hover,
.workshop-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.workshop-card-header,
.detail-header,
.machine-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.workshop-card-header {
  margin-bottom: 8px;
}

.workshop-name {
  font-weight: 600;
}

.workshop-meta,
.detail-subtitle {
  font-size: 13px;
  color: #606266;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-subtitle {
  margin-top: 8px;
}

.separator {
  margin: 0 8px;
  color: #c0c4cc;
}

.detail-actions,
.machine-toolbar {
  margin-bottom: 16px;
}
</style>
