import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('../views/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { requiresAuth: true, title: '首页', icon: 'HomeFilled' }
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('../components/OrderList.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'PLANNER'], title: '工单管理', icon: 'Document', group: '排产管理', groupIcon: 'SetUp' }
        },
        {
          path: 'schedule',
          name: 'Schedule',
          component: () => import('../components/GanttChart.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'PLANNER'], title: '排产计划', icon: 'Calendar', group: '排产管理', groupIcon: 'SetUp' }
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('../views/Users.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '用户管理', icon: 'User', group: '系统管理', groupIcon: 'Setting' }
        },
        {
          path: 'permissions',
          name: 'Permissions',
          component: () => import('../views/Permissions.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '权限配置', icon: 'Lock', group: '系统管理', groupIcon: 'Setting' }
        },
        {
          path: 'roles',
          name: 'Roles',
          component: () => import('../views/Roles.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '角色管理', icon: 'UserFilled', group: '系统管理', groupIcon: 'Setting' }
        },
        {
          path: 'audit-logs',
          name: 'AuditLog',
          component: () => import('../views/AuditLog.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '审计日志', icon: 'Document', group: '系统管理', groupIcon: 'Setting' }
        },
        {
          path: 'dictionary',
          name: 'Dictionary',
          component: () => import('../views/Dictionary.vue'),
          meta: { requiresAuth: true, title: '编码管理', icon: 'Collection', group: '系统管理', groupIcon: 'Setting' }
        },
        {
          path: 'factory-calendar',
          name: 'FactoryCalendar',
          component: () => import('../views/FactoryCalendar.vue'),
          meta: { requiresAuth: true, title: '工厂日历', icon: 'Calendar', group: '基础数据', groupIcon: 'Setting', permissions: ['basedata:factory-calendar:list'] }
        },
        {
          path: 'workshop',
          name: 'WorkshopManagement',
          component: () => import('../views/WorkshopManagement.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '工厂建模', icon: 'Setting', group: '基础数据', groupIcon: 'Setting' }
        },
        {
          path: 'resource-capacity',
          name: 'ResourceCapacity',
          component: () => import('../views/ResourceCapacity.vue'),
          meta: { requiresAuth: true, title: '设备日产能', icon: 'Cpu', group: '基础数据', groupIcon: 'Setting', permissions: ['basedata:resource-capacity:list'] }
        },
        {
          path: 'materials',
          name: 'Material',
          component: () => import('../views/Material.vue'),
          meta: { requiresAuth: true, title: '物料管理', icon: 'Box', group: '基础数据', groupIcon: 'Setting', permissions: ['basedata:material:list'] }
        },
        {
          path: 'molds',
          name: 'Mold',
          component: () => import('../views/Mold.vue'),
          meta: { requiresAuth: true, title: '模具管理', icon: 'Box', group: '基础数据', groupIcon: 'Setting', permissions: ['basedata:mold:list'] }
        },
        {
          path: 'schedule-time-parameter',
          name: 'ScheduleTimeParameter',
          component: () => import('../views/ScheduleTimeParameter.vue'),
          meta: { requiresAuth: true, title: '时间参数', icon: 'Timer', group: '排产管理', groupIcon: 'SetUp', permissions: ['schedule:time-param:list'] }
        }
      ]
    }
  ]
})

// 标记初始化是否完成
let isInitialized = false

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 首次进入时，等待初始化完成
  if (!isInitialized) {
    try {
      await authStore.initialize()
      isInitialized = true
    } catch (error) {
      console.error('初始化认证失败:', error)
      isInitialized = true
    }
  }

  // 如果需要认证
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // 未登录，跳转到登录页
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 检查角色权限
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const hasRequiredRole = to.meta.roles.some(role => authStore.hasRole(role))
      if (!hasRequiredRole) {
        // 没有权限，跳转到首页
        next({ name: 'Dashboard' })
        return
      }
    }

    if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
      const hasRequiredPermission = to.meta.permissions.some(permission => authStore.hasPermission(permission))
      if (!hasRequiredPermission) {
        next({ name: 'Dashboard' })
        return
      }
    }
  } else {
    // 如果已登录且访问登录页，跳转到首页
    if (to.name === 'Login' && authStore.isAuthenticated) {
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

export default router
