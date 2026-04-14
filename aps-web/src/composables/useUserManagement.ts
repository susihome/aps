import { computed, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { useRoleApi, type Role } from '@/api/role'
import type { User } from '@/types/auth'
import { confirmDanger, extractErrorMsg, msgError, msgSuccess } from '@/utils/message'
import type { UserEditorFormValue } from '@/components/users/UserEditorDialog.vue'

type DialogMode = 'create' | 'edit'

const DEFAULT_FORM: UserEditorFormValue = {
  username: '',
  password: '',
  email: '',
  enabled: true,
  roleIds: [],
}

export function useUserManagement() {
  const roleApi = useRoleApi()

  const loading = ref(false)
  const submitting = ref(false)
  const users = ref<User[]>([])
  const roleOptions = ref<Role[]>([])
  const dialogVisible = ref(false)
  const dialogMode = ref<DialogMode>('create')
  const activeUser = ref<User | null>(null)

  const filters = reactive({
    keyword: '',
    enabled: undefined as boolean | undefined,
  })

  const pagination = reactive({
    pageNo: 1,
    pageSize: 10,
    total: 0,
  })

  const formInitialValue = computed<UserEditorFormValue>(() => {
    if (!activeUser.value) {
      return { ...DEFAULT_FORM }
    }

    return {
      username: activeUser.value.username,
      password: '',
      email: activeUser.value.email || '',
      enabled: activeUser.value.enabled,
      roleIds: activeUser.value.roles
        .filter((role): role is string => typeof role === 'string')
        .map((roleName) => roleOptions.value.find((role) => role.name === roleName)?.id)
        .filter((roleId): roleId is string => Boolean(roleId)),
    }
  })

  async function initialize() {
    await Promise.all([loadUsers(), loadRoles()])
  }

  async function loadUsers() {
    loading.value = true
    try {
      const result = await userApi.list({
        pageNo: pagination.pageNo,
        pageSize: pagination.pageSize,
        keyword: filters.keyword.trim(),
        enabled: filters.enabled,
      })
      users.value = result.items
      pagination.total = result.total
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '加载用户列表失败'))
    } finally {
      loading.value = false
    }
  }

  async function loadRoles() {
    try {
      roleOptions.value = await roleApi.getAllRoles()
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, '加载角色列表失败'))
    }
  }

  function handleSearch() {
    pagination.pageNo = 1
    loadUsers()
  }

  function handleResetFilters() {
    filters.keyword = ''
    filters.enabled = undefined
    pagination.pageNo = 1
    loadUsers()
  }

  function handlePageChange(pageNo: number) {
    pagination.pageNo = pageNo
    loadUsers()
  }

  function handlePageSizeChange(pageSize: number) {
    pagination.pageSize = pageSize
    pagination.pageNo = 1
    loadUsers()
  }

  function openCreateDialog() {
    dialogMode.value = 'create'
    activeUser.value = null
    dialogVisible.value = true
  }

  function openEditDialog(user: User) {
    dialogMode.value = 'edit'
    activeUser.value = user
    dialogVisible.value = true
  }

  function closeDialog() {
    dialogVisible.value = false
  }

  async function submitUser(form: UserEditorFormValue) {
    submitting.value = true
    try {
      if (dialogMode.value === 'create') {
        await userApi.create({
          username: form.username,
          password: form.password,
          email: form.email,
          roleIds: form.roleIds,
        })
        msgSuccess('用户创建成功')
      } else if (activeUser.value) {
        await userApi.update(activeUser.value.id, {
          email: form.email,
          enabled: form.enabled,
          roleIds: form.roleIds,
        })
        msgSuccess('用户更新成功')
      }

      dialogVisible.value = false
      await Promise.all([loadUsers(), loadRoles()])
    } catch (error: unknown) {
      msgError(extractErrorMsg(error, dialogMode.value === 'create' ? '创建用户失败' : '更新用户失败'))
    } finally {
      submitting.value = false
    }
  }

  async function deleteUser(user: User) {
    try {
      await confirmDanger(`确定删除用户“${user.username}”吗？此操作不可恢复。`, '删除用户')
      await userApi.delete(user.id)
      msgSuccess('用户删除成功')

      if (users.value.length === 1 && pagination.pageNo > 1) {
        pagination.pageNo -= 1
      }
      await loadUsers()
    } catch (error: unknown) {
      if (error === 'cancel' || error === 'close') {
        return
      }
      msgError(extractErrorMsg(error, '删除用户失败'))
    }
  }

  async function resetPassword(user: User) {
    try {
      const { value } = await ElMessageBox.prompt(
        `请输入用户“${user.username}”的新密码`,
        '重置密码',
        {
          inputType: 'password',
          inputPlaceholder: '至少 6 位',
          inputPattern: /^.{6,100}$/,
          inputErrorMessage: '密码长度必须在 6 到 100 个字符之间',
          confirmButtonText: '重置',
          cancelButtonText: '取消',
          closeOnClickModal: false,
        },
      )

      await userApi.resetPassword(user.id, { newPassword: value })
      msgSuccess('密码重置成功')
    } catch (error: unknown) {
      if (error === 'cancel' || error === 'close') {
        return
      }
      msgError(extractErrorMsg(error, '重置密码失败'))
    }
  }

  return {
    loading,
    submitting,
    users,
    roleOptions,
    dialogVisible,
    dialogMode,
    filters,
    pagination,
    formInitialValue,
    initialize,
    handleSearch,
    handleResetFilters,
    handlePageChange,
    handlePageSizeChange,
    openCreateDialog,
    openEditDialog,
    closeDialog,
    submitUser,
    deleteUser,
    resetPassword,
  }
}
