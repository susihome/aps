# Vite 导入错误修复指南

## 问题
```
Failed to resolve import "@/api/role" from "src/views/Roles.vue"
```

## 原因
Vite 开发服务器的模块缓存问题，新创建的文件未被识别。

## 解决方案

### 方案 1: 重启 Vite 开发服务器（推荐）
```bash
# 停止当前的开发服务器 (Ctrl+C)
# 然后重新启动
cd aps-web
npm run dev
```

### 方案 2: 清除 Vite 缓存
```bash
cd aps-web
rm -rf node_modules/.vite
npm run dev
```

### 方案 3: 强制刷新浏览器
- 按 `Ctrl + Shift + R` (Windows/Linux)
- 或 `Cmd + Shift + R` (Mac)

## 验证文件存在
```bash
ls -la aps-web/src/api/role.ts
# 应该显示文件存在
```

## 文件已创建
- ✅ `aps-web/src/api/role.ts` - 已创建
- ✅ `aps-web/src/views/Roles.vue` - 已创建
- ✅ `aps-web/src/router/index.ts` - 已更新

## 预期结果
重启后，Vite 应该能正确解析 `@/api/role` 导入，页面正常加载。
