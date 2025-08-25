<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式优化 - 参考百度智能云妙搭设计 */
:root {
  /* 主色调 - 蓝色系 */
  --primary-color: #1890ff;
  --primary-color-hover: #40a9ff;
  --primary-color-active: #096dd9;
  --primary-color-light: #e6f7ff;
  
  /* 辅助色 */
  --success-color: #52c41a;
  --warning-color: #faad14;
  --error-color: #ff4d4f;
  --info-color: #1890ff;
  
  /* 中性色 */
  --text-color: #262626;
  --text-color-secondary: #595959;
  --text-color-tertiary: #8c8c8c;
  --text-color-quaternary: #bfbfbf;
  
  /* 背景色 */
  --bg-color-page: #ffffff;
  --bg-color-container: #fafafa;
  --bg-color-hover: #f5f5f5;
  --bg-color-active: #f0f0f0;
  
  /* 边框色 */
  --border-color: #d9d9d9;
  --border-color-light: #f0f0f0;
  --border-color-split: #f0f0f0;
  
  /* 阴影 */
  --shadow-light: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-medium: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-heavy: 0 8px 32px rgba(0, 0, 0, 0.12);
  
  /* 圆角 */
  --border-radius-sm: 4px;
  --border-radius-base: 8px;
  --border-radius-lg: 12px;
  --border-radius-xl: 16px;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  background-color: var(--bg-color-container);
  color: var(--text-color);
  line-height: 1.5715;
}

html {
  overflow-x: hidden;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--bg-color-hover);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-color-quaternary);
}

/* 全局链接样式 */
a {
  color: var(--primary-color);
  text-decoration: none;
  transition: color 0.3s;
}

a:hover {
  color: var(--primary-color-hover);
}

/* 全局按钮样式优化 */
.ant-btn {
  border-radius: var(--border-radius-base);
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.ant-btn-primary {
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 2px 0 rgba(24, 144, 255, 0.1);
}

.ant-btn-primary:hover {
  background: var(--primary-color-hover);
  border-color: var(--primary-color-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

/* 全局卡片样式 */
.ant-card {
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-color-light);
  transition: all 0.3s;
}

.ant-card:hover {
  box-shadow: var(--shadow-medium);
  transform: translateY(-2px);
}

/* 全局表单样式 */
.ant-input,
.ant-input-password,
.ant-select-selector,
.ant-textarea {
  border-radius: var(--border-radius-base);
  border-color: var(--border-color);
  transition: all 0.3s;
}

.ant-input:focus,
.ant-input-password:focus,
.ant-select-focused .ant-select-selector,
.ant-textarea:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

/* 全局表格样式 */
.ant-table {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.ant-table-thead > tr > th {
  background: var(--bg-color-hover);
  border-bottom: 1px solid var(--border-color-light);
  font-weight: 600;
}

/* 全局模态框样式 */
.ant-modal-content {
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-heavy);
}

/* 全局消息提示样式 */
.ant-message {
  top: 80px;
}

.ant-message-notice-content {
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-medium);
}
</style>