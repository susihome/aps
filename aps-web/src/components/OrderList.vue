<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { orderApi } from '../api'
import type { Order } from '../api/order'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

const canCreate = computed(() => {
  return authStore.hasRole('ADMIN') || authStore.hasRole('PLANNER')
})

const orders = ref<Order[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const form = ref<Partial<Order>>({
  orderNo: '',
  productCode: '',
  productName: '',
  quantity: 0,
  priority: 'NORMAL',
  dueDate: ''
})

onMounted(() => {
  loadOrders()
})

async function loadOrders() {
  try {
    orders.value = await orderApi.list()
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : '加载工单列表失败'
    ElMessage.error(message)
  }
}

async function submitForm() {
  loading.value = true
  try {
    await orderApi.create({
      orderNo: form.value.orderNo!,
      productCode: form.value.productCode!,
      productName: form.value.productName!,
      quantity: form.value.quantity!,
      priority: form.value.priority!,
      dueDate: form.value.dueDate!
    })
    ElMessage.success('工单创建成功')
    dialogVisible.value = false
    loadOrders()
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : '创建工单失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  form.value = {
    orderNo: '',
    productCode: '',
    productName: '',
    quantity: 0,
    priority: 'NORMAL',
    dueDate: ''
  }
  dialogVisible.value = true
}
</script>

<template>
  <div class="order-list">
    <el-button
      type="primary"
      @click="showCreateDialog"
      v-if="canCreate"
      class="create-button"
      aria-label="新建工单"
    >
      新建工单
    </el-button>
    <el-table :data="orders" style="margin-top: 20px" aria-label="工单列表">
      <el-table-column prop="orderNo" label="工单号" min-width="120" />
      <el-table-column prop="productName" label="产品名称" min-width="150" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="priority" label="优先级" width="100" />
      <el-table-column prop="dueDate" label="交期" min-width="160" />
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      title="新建工单"
      width="90%"
      :style="{ maxWidth: '600px' }"
      :close-on-click-modal="false"
      aria-labelledby="dialog-title"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="工单号">
          <el-input v-model="form.orderNo" aria-label="工单号" />
        </el-form-item>
        <el-form-item label="产品编码">
          <el-input v-model="form.productCode" aria-label="产品编码" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.productName" aria-label="产品名称" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" aria-label="数量" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" aria-label="优先级">
            <el-option label="紧急" value="URGENT" />
            <el-option label="高" value="HIGH" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="交期">
          <el-date-picker v-model="form.dueDate" type="datetime" aria-label="交期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" aria-label="取消">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading" aria-label="提交工单">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-list {
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

.create-button {
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

.create-button:hover {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.create-button:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.create-button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

:deep(.el-table) {
  font-size: 14px;
  margin-top: 20px;
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  color: var(--color-text);
}

:deep(.el-table::before) {
  display: none;
}

:deep(.el-table th) {
  font-weight: 600;
  background: var(--color-surface-light);
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
}

:deep(.el-table td) {
  border-bottom: 1px solid var(--color-border);
  background: white;
}

:deep(.el-table tr:hover > td) {
  background: var(--color-surface-light);
}

:deep(.el-table__body-wrapper) {
  background: white;
}

:deep(.el-dialog) {
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
}

:deep(.el-dialog__header) {
  background: var(--color-surface-light);
  border-bottom: 1px solid var(--color-border);
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  color: var(--color-text);
  font-weight: 700;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 24px;
  background: white;
}

:deep(.el-form-item__label) {
  color: var(--color-text);
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  background: var(--color-surface-light);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: none;
  transition: all 200ms;
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--color-border-light);
  background: #F1F5F9;
}

:deep(.el-input__wrapper:focus-within) {
  border-color: var(--color-primary);
  background: white;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

:deep(.el-input__inner) {
  color: var(--color-text);
}

:deep(.el-select .el-input__wrapper) {
  background: var(--color-surface-light);
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  background: var(--color-surface-light);
}

:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  background: var(--color-surface-light);
  border-top: 1px solid var(--color-border);
}

/* 响应式 */
@media (max-width: 768px) {
  :deep(.el-table) {
    font-size: 12px;
  }

  .create-button {
    width: 100%;
  }
}

/* 支持 prefers-reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .order-list,
  .create-button {
    animation: none;
    transition: none;
  }

  .create-button:hover,
  .create-button:active {
    transform: none;
  }
}
</style>
