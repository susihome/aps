<template>
  <div class="loading-spinner" :class="{ small: size === 'small', large: size === 'large' }">
    <div class="spinner-ring"></div>
    <div class="spinner-ring"></div>
    <div class="spinner-ring"></div>
    <div class="spinner-ring"></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  size?: 'small' | 'medium' | 'large'
}>()
</script>

<style scoped>
.loading-spinner {
  display: inline-block;
  position: relative;
  width: 64px;
  height: 64px;
}

.loading-spinner.small {
  width: 32px;
  height: 32px;
}

.loading-spinner.large {
  width: 80px;
  height: 80px;
}

.spinner-ring {
  box-sizing: border-box;
  display: block;
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid transparent;
  border-radius: 50%;
  animation: spinner-ring 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
  border-color: #667eea transparent transparent transparent;
}

.spinner-ring:nth-child(1) {
  animation-delay: -0.45s;
}

.spinner-ring:nth-child(2) {
  animation-delay: -0.3s;
}

.spinner-ring:nth-child(3) {
  animation-delay: -0.15s;
}

@keyframes spinner-ring {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-spinner.small .spinner-ring {
  border-width: 2px;
}

.loading-spinner.large .spinner-ring {
  border-width: 5px;
}
</style>
