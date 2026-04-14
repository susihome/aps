<template>
  <div class="login-container">
    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-header">
        <div class="brand-icon">
          <img src="@/assets/logo.png" alt="APS" class="brand-icon-img" />
        </div>
        <h2>APS</h2>
      </div>

      <div class="brand-content">
        <h1>智能排产<br/>调度系统</h1>
        <p class="brand-subtitle">生产计划 · 资源优化 · 约束求解 · 实时调度</p>

        <div class="feature-list">
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>约束求解引擎优化排产</span>
          </div>
          <div class="feature-item">
            <el-icon><Setting /></el-icon>
            <span>MES 实时集成与动态重排产</span>
          </div>
          <div class="feature-item">
            <el-icon><DataLine /></el-icon>
            <span>可视化甘特图交互式调整</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-section">
      <div class="login-card" :class="{ shake: showShake }">
        <div class="card-icon">
          <img src="@/assets/logo.png" alt="APS" class="card-icon-img" />
        </div>

        <h3>APS智能排产</h3>
        <p class="card-subtitle">账号登录</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
          aria-label="登录表单"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
              autocomplete="username"
              aria-label="用户名"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              autocomplete="current-password"
              aria-label="密码"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
              aria-label="登录"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <p>© 2026-2056 APS智能排产管理系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { msgSuccess, msgError } from '@/utils/message'
import { User, Lock, Document, Setting, DataLine } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const showShake = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await authStore.login({
        username: form.username,
        password: form.password
      })

      msgSuccess('登录成功')

      const redirect = route.query.redirect as string
      router.push(redirect || '/dashboard')
    } catch (error: unknown) {
      showShake.value = true
      setTimeout(() => {
        showShake.value = false
      }, 500)

      const message = error instanceof Error ? error.message : '登录失败，请检查用户名和密码'
      msgError(message)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  background: #F1F5F9;
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #2563EB 0%, #1E40AF 100%);
  padding: 60px;
  display: flex;
  flex-direction: column;
  color: white;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 80px;
  position: relative;
  z-index: 1;
}

.brand-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.brand-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.brand-header h2 {
  font-size: 32px;
  font-weight: 600;
  margin: 0;
  letter-spacing: -0.01em;
  color: white;
}

.brand-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.brand-content h1 {
  font-size: 72px;
  font-weight: 700;
  line-height: 1.15;
  margin: 0 0 32px 0;
  letter-spacing: -0.02em;
  position: relative;
  padding-bottom: 32px;
  color: white;
}

.brand-subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 64px 0;
  line-height: 1.6;
  font-weight: 300;
  letter-spacing: 0.02em;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.95);
  padding: 16px 0;
}

.feature-item .el-icon {
  font-size: 24px;
  opacity: 0.9;
  flex-shrink: 0;
}

/* 右侧表单区 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #F8FAFC;
}

.login-card {
  width: 100%;
  max-width: 440px;
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 10px 15px rgba(0, 0, 0, 0.1);
  animation: slideUp 500ms cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-card.shake {
  animation: shake 400ms cubic-bezier(0.36, 0.07, 0.19, 0.97);
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-8px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(8px);
  }
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  overflow: hidden;
  margin: 0 auto 24px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.card-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-card h3 {
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 8px 0;
  text-align: center;
  letter-spacing: -0.01em;
}

.card-subtitle {
  font-size: 14px;
  color: #64748B;
  text-align: center;
  margin: 0 0 32px 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 8px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  box-shadow: none;
  transition: all 200ms;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #CBD5E1;
  background: #F1F5F9;
}

.login-form :deep(.el-input__wrapper:focus-within) {
  border-color: #2563EB;
  background: white;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.login-form :deep(.el-input__inner) {
  font-size: 15px;
  color: #0F172A;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #94A3B8;
}

.login-form :deep(.el-input__prefix) {
  color: #64748B;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: #2563EB;
  border: none;
  border-radius: 8px;
  transition: all 200ms;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.login-button:hover {
  background: #1E40AF;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #E2E8F0;
  text-align: center;
}

.login-footer p {
  font-size: 12px;
  color: #94A3B8;
  margin: 0;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 1024px) {
  .brand-section {
    display: none;
  }

  .form-section {
    flex: none;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .form-section {
    padding: 20px;
  }

  .login-card {
    padding: 36px 28px;
  }

  .login-card h3 {
    font-size: 20px;
  }

  .brand-content h1 {
    font-size: 42px;
  }
}

/* 支持 prefers-reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .login-card,
  .login-card.shake,
  .login-form :deep(.el-input__wrapper),
  .login-button {
    animation: none;
    transition: none;
  }

  .login-button:hover,
  .login-button:active {
    transform: none;
  }
}
</style>
