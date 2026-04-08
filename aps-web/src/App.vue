<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

:root {
  /* 主色调 - 明亮蓝色 */
  --color-primary: #2563EB;
  --color-primary-light: #3B82F6;
  --color-primary-dark: #1E40AF;
  --color-secondary: #60A5FA;

  /* 状态色 */
  --color-success: #16A34A;
  --color-warning: #F59E0B;
  --color-error: #DC2626;
  --color-manual: #8B5CF6;
  --color-offline: #94A3B8;

  /* 背景色 - 浅色主题 */
  --color-background: #F8FAFC;
  --color-surface: #FFFFFF;
  --color-surface-light: #F1F5F9;
  --color-surface-lighter: #E2E8F0;

  /* 文字色 */
  --color-text: #0F172A;
  --color-text-secondary: #64748B;
  --color-text-tertiary: #94A3B8;

  /* 边框色 */
  --color-border: #E2E8F0;
  --color-border-light: #CBD5E1;

  /* 字体 */
  --font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

  /* 阴影 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px rgba(0, 0, 0, 0.15);

  /* 圆角 */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: var(--font-family);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: var(--color-background);
  color: var(--color-text);
  min-height: 100vh;
}

body {
  margin: 0;
  padding: 0;
  background: var(--color-background);
  overflow-x: hidden;
}

/* Scrollbar styling */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--color-surface-light);
}

::-webkit-scrollbar-thumb {
  background: var(--color-border-light);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-tertiary);
}

/* Selection */
::selection {
  background: var(--color-primary);
  color: white;
}
</style>
