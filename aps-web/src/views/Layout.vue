<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-left">
        <div class="logo-area" @click="router.push('/dashboard')">
          <div class="logo-icon">
            <el-icon :size="22" color="#fff"><Cpu /></el-icon>
          </div>
          <span v-if="!isCollapsed" class="logo-text">APS 系统</span>
        </div>
        <el-button
          class="menu-toggle"
          :icon="isCollapsed ? Expand : Fold"
          @click="toggleSidebar"
          aria-label="切换侧边栏"
          text
        />
      </div>
      <div class="header-right">
        <UserMenu />
      </div>
    </el-header>

    <el-container>
      <el-aside :width="sidebarWidth" class="layout-aside" :class="{ collapsed: isCollapsed }">
        <!-- 搜索框 -->
        <div class="sidebar-search" v-if="!isCollapsed">
          <el-input
            v-model="menuSearchQuery"
            placeholder="搜索菜单..."
            :prefix-icon="Search"
            clearable
            size="small"
            class="menu-search-input"
          />
        </div>

        <!-- 分级菜单 -->
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="activeMenu"
            :default-openeds="defaultOpeneds"
            router
            class="layout-menu"
            :collapse="isCollapsed"
            :collapse-transition="true"
            :unique-opened="false"
          >
            <!-- 首页（无分组） -->
            <template v-for="item in filteredUngroupedMenus" :key="item.path">
              <el-menu-item :index="item.path" :aria-label="item.title">
                <el-icon><component :is="iconMap[item.icon]" /></el-icon>
                <template #title>{{ item.title }}</template>
              </el-menu-item>
            </template>

            <!-- 分组菜单 -->
            <template v-for="group in filteredGroupedMenus" :key="group.name">
              <div class="menu-group-label" v-if="!isCollapsed">
                <span>{{ group.name }}</span>
              </div>
              <el-sub-menu v-if="isCollapsed" :index="group.name">
                <template #title>
                  <el-icon><component :is="iconMap[group.icon]" /></el-icon>
                  <span>{{ group.name }}</span>
                </template>
                <el-menu-item
                  v-for="item in group.children"
                  :key="item.path"
                  :index="item.path"
                  :aria-label="item.title"
                >
                  <el-icon><component :is="iconMap[item.icon]" /></el-icon>
                  <template #title>{{ item.title }}</template>
                </el-menu-item>
              </el-sub-menu>
              <template v-else>
                <el-menu-item
                  v-for="item in group.children"
                  :key="item.path"
                  :index="item.path"
                  :aria-label="item.title"
                >
                  <el-icon><component :is="iconMap[item.icon]" /></el-icon>
                  <template #title>{{ item.title }}</template>
                </el-menu-item>
              </template>
            </template>
          </el-menu>
        </el-scrollbar>

        <!-- 底部版本信息 -->
        <div class="sidebar-footer" v-if="!isCollapsed">
          <span class="version-text">APS v1.0.0</span>
        </div>
      </el-aside>

      <el-container class="main-container" direction="vertical">
        <!-- 导航栏第一级：面包屑 -->
        <div class="nav-header">
          <el-breadcrumb separator=">" class="nav-breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">
              <el-icon style="vertical-align: middle;"><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentGroup">{{ currentGroup }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 导航栏第二级：标签页 -->
        <div class="tags-view">
          <el-scrollbar class="tags-scrollbar">
            <div class="tags-wrapper">
              <div
                v-for="tag in tagsStore.visitedTags"
                :key="tag.path"
                class="tag-item"
                :class="{ active: tag.path === tagsStore.activeTag }"
                @click="handleTagClick(tag)"
                @contextmenu.prevent="openTagContextMenu($event, tag)"
              >
                <el-icon v-if="tag.icon" :size="14"><component :is="iconMap[tag.icon]" /></el-icon>
                <span class="tag-title">{{ tag.title }}</span>
                <el-icon
                  v-if="tag.closable"
                  class="tag-close"
                  :size="12"
                  @click.stop="handleTagClose(tag.path)"
                >
                  <Close />
                </el-icon>
              </div>
            </div>
          </el-scrollbar>
          <div class="tags-actions">
            <el-dropdown trigger="click" @command="handleTagsCommand">
              <el-button :icon="ArrowDown" size="small" text class="tags-action-btn" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="closeOther">关闭其他</el-dropdown-item>
                  <el-dropdown-item command="closeAll">关闭全部</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 主内容区 -->
        <el-main class="layout-main">
          <transition name="fade" mode="out-in">
            <router-view />
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, type Component, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled, Document, Calendar, User, UserFilled, Expand, Fold,
  Search, Close, ArrowDown, Setting, SetUp, Cpu, Lock, Collection, Timer, Box
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useTagsStore } from '../stores/tags'
import UserMenu from '../components/UserMenu.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const tagsStore = useTagsStore()

const isCollapsed = ref(false)
const isMobile = ref(false)
const menuSearchQuery = ref('')

// 图标映射
const iconMap: Record<string, Component> = {
  HomeFilled: markRaw(HomeFilled),
  Document: markRaw(Document),
  Calendar: markRaw(Calendar),
  User: markRaw(User),
  UserFilled: markRaw(UserFilled),
  Lock: markRaw(Lock),
  Setting: markRaw(Setting),
  SetUp: markRaw(SetUp),
  Cpu: markRaw(Cpu),
  Collection: markRaw(Collection),
  Timer: markRaw(Timer),
  Box: markRaw(Box),
}

// 菜单项接口
interface MenuItem {
  path: string
  title: string
  icon: string
  roles?: string[]
  permissions?: string[]
}

interface MenuGroup {
  name: string
  icon: string
  children: MenuItem[]
}

// 从路由构建菜单数据
const layoutRoute = computed(() => {
  return router.options.routes.find(r => r.path === '/')
})

const allMenuItems = computed(() => {
  if (!layoutRoute.value?.children) return []
  return layoutRoute.value.children
    .filter(r => r.meta?.title && !r.meta?.hideInMenu)
    .map(r => ({
      path: '/' + r.path,
      title: r.meta!.title as string,
      icon: (r.meta!.icon as string) || '',
      group: (r.meta!.group as string) || '',
      groupIcon: (r.meta!.groupIcon as string) || '',
      roles: (r.meta!.roles as string[]) || [],
      permissions: (r.meta!.permissions as string[]) || [],
    }))
})

// 根据权限过滤菜单项
const accessibleMenuItems = computed(() => {
  return allMenuItems.value.filter(item => {
    const roleAllowed = !item.roles || item.roles.length === 0 || item.roles.some(role => authStore.hasRole(role))
    const permissionAllowed = !item.permissions || item.permissions.length === 0 || item.permissions.some(permission => authStore.hasPermission(permission))
    return roleAllowed && permissionAllowed
  })
})

// 搜索过滤
const searchFilteredItems = computed(() => {
  if (!menuSearchQuery.value.trim()) return accessibleMenuItems.value
  const query = menuSearchQuery.value.toLowerCase()
  return accessibleMenuItems.value.filter(item =>
    item.title.toLowerCase().includes(query) ||
    item.group.toLowerCase().includes(query)
  )
})

// 无分组菜单
const filteredUngroupedMenus = computed(() => {
  return searchFilteredItems.value.filter(item => !item.group)
})

// 分组菜单
const filteredGroupedMenus = computed(() => {
  const groups: Record<string, MenuGroup> = {}
  searchFilteredItems.value
    .filter(item => item.group)
    .forEach(item => {
      if (!groups[item.group]) {
        groups[item.group] = {
          name: item.group,
          icon: item.groupIcon,
          children: []
        }
      }
      groups[item.group].children.push({
        path: item.path,
        title: item.title,
        icon: item.icon,
        roles: item.roles,
        permissions: item.permissions
      })
    })
  return Object.values(groups)
})

// 默认展开的分组
const defaultOpeneds = computed(() => {
  return filteredGroupedMenus.value.map(g => g.name)
})

const activeMenu = computed(() => route.path)
const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '220px')

// 面包屑信息
const currentTitle = computed(() => (route.meta?.title as string) || '')
const currentGroup = computed(() => (route.meta?.group as string) || '')

// 监听路由变化，添加标签
watch(
  () => route.path,
  () => {
    if (route.meta?.title) {
      tagsStore.addTag(route)
    }
  },
  { immediate: true }
)

// 标签页操作
function handleTagClick(tag: { path: string }) {
  if (tag.path !== route.path) {
    router.push(tag.path)
  }
}

function handleTagClose(path: string) {
  const redirectPath = tagsStore.removeTag(path)
  if (redirectPath) {
    router.push(redirectPath)
  }
}

function openTagContextMenu(_event: MouseEvent, _tag: { path: string }) {
  // 可扩展右键菜单
}

function handleTagsCommand(command: string) {
  if (command === 'closeOther') {
    tagsStore.removeOtherTags(route.path)
  } else if (command === 'closeAll') {
    const redirectPath = tagsStore.removeAllTags()
    if (route.path !== redirectPath) {
      router.push(redirectPath)
    }
  }
}

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: var(--color-background);
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 0 24px;
  height: 56px;
  border-bottom: 1px solid var(--color-border);
  position: relative;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 0;
}

.logo-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.menu-toggle {
  width: 32px;
  height: 32px;
  color: var(--color-text-secondary);
  cursor: pointer;
}

.menu-toggle:hover {
  color: var(--color-primary);
}

.layout-aside {
  background: white;
  transition: width 300ms cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.layout-aside.collapsed {
  width: 64px;
}

/* 侧边栏搜索框 */
.sidebar-search {
  padding: 12px 12px 4px;
  flex-shrink: 0;
}

.menu-search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  background: var(--color-surface-light);
  box-shadow: none;
  border: 1px solid transparent;
  transition: all 200ms ease;
}

.menu-search-input :deep(.el-input__wrapper:hover),
.menu-search-input :deep(.el-input__wrapper.is-focus) {
  background: white;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
}

/* 菜单滚动区 */
.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

/* 菜单分组标签 */
.menu-group-label {
  padding: 16px 20px 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  line-height: 1;
}

.layout-menu {
  border: none;
  background: transparent;
  transition: all 250ms ease;
  padding: 4px 0;
}

.layout-menu :deep(.el-menu-item) {
  color: var(--color-text-secondary);
  transition: all 200ms ease;
  min-height: 42px;
  line-height: 42px;
  margin: 2px 8px;
  border-radius: var(--radius-md);
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
}

.layout-menu :deep(.el-menu-item:hover) {
  background: rgba(37, 99, 235, 0.08);
  color: var(--color-primary);
}

.layout-menu :deep(.el-menu-item.is-active) {
  background: rgba(37, 99, 235, 0.12);
  color: var(--color-primary);
  font-weight: 600;
}

.layout-menu :deep(.el-menu-item:focus-visible) {
  outline: 2px solid var(--color-primary);
  outline-offset: -2px;
}

.layout-menu :deep(.el-menu-item .el-icon) {
  font-size: 16px;
}

.layout-menu :deep(.el-sub-menu__title) {
  color: var(--color-text-secondary);
  min-height: 42px;
  line-height: 42px;
  margin: 2px 8px;
  border-radius: var(--radius-md);
  font-weight: 500;
  font-size: 14px;
}

.layout-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(37, 99, 235, 0.08);
  color: var(--color-primary);
}

.layout-menu :deep(.el-sub-menu .el-menu-item) {
  min-height: 38px;
  line-height: 38px;
  padding-left: 48px !important;
}

/* 底部版本 */
.sidebar-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--color-border);
  flex-shrink: 0;
}

.version-text {
  font-size: 11px;
  color: var(--color-text-tertiary);
}

/* 主内容容器 */
.main-container {
  overflow: hidden;
}

/* 面包屑导航 */
.nav-header {
  background: white;
  padding: 10px 20px;
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.nav-breadcrumb {
  font-size: 13px;
}

.nav-breadcrumb :deep(.el-breadcrumb__item) {
  display: inline-flex;
  align-items: center;
}

/* 标签页导航 */
.tags-view {
  background: white;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  padding: 0;
  height: 38px;
  flex-shrink: 0;
}

.tags-scrollbar {
  flex: 1;
  overflow: hidden;
}

.tags-scrollbar :deep(.el-scrollbar__bar.is-horizontal) {
  height: 3px;
}

.tags-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  white-space: nowrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--color-text-secondary);
  background: var(--color-surface-light);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 150ms ease;
  border: 1px solid transparent;
  white-space: nowrap;
  height: 28px;
  line-height: 1;
}

.tag-item:hover {
  color: var(--color-primary);
  background: rgba(37, 99, 235, 0.06);
}

.tag-item.active {
  color: var(--color-primary);
  background: rgba(37, 99, 235, 0.1);
  border-color: rgba(37, 99, 235, 0.2);
  font-weight: 500;
}

.tag-title {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-close {
  margin-left: 2px;
  border-radius: 50%;
  padding: 1px;
  transition: all 150ms ease;
}

.tag-close:hover {
  background: rgba(37, 99, 235, 0.15);
  color: var(--color-primary);
}

.tags-actions {
  padding: 0 8px;
  border-left: 1px solid var(--color-border);
  height: 100%;
  display: flex;
  align-items: center;
}

.tags-action-btn {
  width: 28px;
  height: 28px;
  color: var(--color-text-tertiary);
}

.tags-action-btn:hover {
  color: var(--color-primary);
}

.layout-main {
  background: var(--color-background);
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

/* 页面切换动画 */
.fade-enter-active {
  transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-leave-active {
  transition: all 200ms cubic-bezier(0.4, 0, 1, 0.5);
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 768px) {
  .layout-header {
    padding: 0 16px;
    height: 48px;
  }

  .logo-text {
    display: none;
  }

  .layout-main {
    padding: 16px;
  }

  .nav-header {
    padding: 8px 16px;
  }
}

/* 支持 prefers-reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .layout-aside,
  .layout-menu,
  .layout-menu :deep(.el-menu-item),
  .menu-toggle,
  .tag-item,
  .tag-close,
  .fade-enter-active,
  .fade-leave-active {
    transition: none;
  }
}
</style>
