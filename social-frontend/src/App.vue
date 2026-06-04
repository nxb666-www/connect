<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

html.dark {
  color-scheme: dark;
}

body {
  font-family: 'Sora', 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #0f0f14;
  color: #fff;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Element Plus 暗色模式全局覆盖 */
html.dark {
  --el-bg-color: rgba(22, 22, 30, 0.95);
  --el-bg-color-overlay: rgba(22, 22, 30, 0.95);
  --el-bg-color-page: #0f0f14;
  --el-text-color-primary: rgba(255, 255, 255, 0.9);
  --el-text-color-regular: rgba(255, 255, 255, 0.7);
  --el-text-color-secondary: rgba(255, 255, 255, 0.5);
  --el-text-color-placeholder: rgba(255, 255, 255, 0.25);
  --el-border-color: rgba(255, 255, 255, 0.08);
  --el-border-color-light: rgba(255, 255, 255, 0.06);
  --el-fill-color: rgba(255, 255, 255, 0.04);
  --el-fill-color-light: rgba(255, 255, 255, 0.03);
  --el-fill-color-blank: transparent;
  --el-color-primary: #FF6B6B;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* Element Plus 暗色主题覆盖 */
.el-dialog {
  --el-dialog-bg-color: rgba(22, 22, 30, 0.95) !important;
  --el-dialog-border-radius: 20px !important;
}

.el-dialog__title {
  color: #fff !important;
  font-family: 'Sora', 'Noto Sans SC', sans-serif !important;
}

.el-dialog__headerbtn .el-dialog__close {
  color: rgba(255, 255, 255, 0.4) !important;
}

.el-dropdown-menu {
  background: rgba(22, 22, 30, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.06) !important;
  backdrop-filter: blur(20px) !important;
}

.el-dropdown-menu__item {
  color: rgba(255, 255, 255, 0.7) !important;
}

.el-dropdown-menu__item:hover {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #FF6B6B !important;
}

.el-popper.is-light {
  background: rgba(22, 22, 30, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.06) !important;
}

.el-message {
  --el-message-bg-color: rgba(22, 22, 30, 0.95) !important;
  --el-message-border-color: rgba(255, 255, 255, 0.06) !important;
  --el-message-text-color: #fff !important;
  backdrop-filter: blur(20px) !important;
}

.el-form-item__error {
  font-family: 'Sora', 'Noto Sans SC', sans-serif !important;
}
</style>
