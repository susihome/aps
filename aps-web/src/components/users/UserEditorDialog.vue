<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface RoleOption {
  id: string
  name: string
  description?: string
}

export interface UserEditorFormValue {
  username: string
  password: string
  email: string
  enabled: boolean
  roleIds: string[]
}

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  loading: boolean
  roles: RoleOption[]
  initialValue: UserEditorFormValue
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [value: UserEditorFormValue]
}>()

const formRef = ref<FormInstance>()
const form = reactive<UserEditorFormValue>(createEmptyForm())

const rules: FormRules<UserEditorFormValue> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度至少 6 个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  roleIds: [
    { type: 'array', required: true, min: 1, message: '请至少选择一个角色', trigger: 'change' },
  ],
}

watch(
  () => [props.modelValue, props.initialValue] as const,
  ([visible, initialValue]) => {
    if (!visible) {
      return
    }
    syncForm(initialValue)
    formRef.value?.clearValidate()
  },
  { immediate: true, deep: true },
)

function createEmptyForm(): UserEditorFormValue {
  return {
    username: '',
    password: '',
    email: '',
    enabled: true,
    roleIds: [],
  }
}

function syncForm(value: UserEditorFormValue) {
  form.username = value.username
  form.password = value.password
  form.email = value.email
  form.enabled = value.enabled
  form.roleIds = [...value.roleIds]
}

function closeDialog() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  emit('submit', {
    username: form.username.trim(),
    password: form.password,
    email: form.email.trim(),
    enabled: form.enabled,
    roleIds: [...form.roleIds],
  })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="mode === 'create' ? '新建用户' : '编辑用户'"
    width="92%"
    :style="{ maxWidth: '560px' }"
    :close-on-click-modal="false"
    @close="closeDialog"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          :disabled="mode === 'edit'"
          placeholder="请输入用户名"
          autocomplete="off"
        />
      </el-form-item>
      <el-form-item v-if="mode === 'create'" label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="请输入密码"
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="form.email"
          placeholder="请输入邮箱"
          autocomplete="email"
        />
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="form.roleIds"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option
            v-for="role in roles"
            :key="role.id"
            :label="role.name"
            :value="role.id"
          >
            <div class="role-option">
              <span>{{ role.name }}</span>
              <span v-if="role.description" class="role-option__desc">{{ role.description }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item v-if="mode === 'edit'" label="状态">
        <el-switch
          v-model="form.enabled"
          inline-prompt
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ mode === 'create' ? '创建' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.role-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.role-option__desc {
  color: #64748b;
  font-size: 12px;
}
</style>
