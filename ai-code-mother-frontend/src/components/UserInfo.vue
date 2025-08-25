<template>
  <div class="user-info" :data-size="size">
    <a-avatar :src="user?.userAvatar" :size="size">
      {{ user?.userName?.charAt(0) || 'U' }}
    </a-avatar>
    <span v-if="showName" class="user-name">{{ user?.userName || '未知用户' }}</span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  user?: API.UserVO
  size?: number | 'small' | 'default' | 'large'
  showName?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'default',
  showName: true,
})
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: var(--border-radius-base);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.user-info:hover {
  background: var(--bg-color-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
}

.user-info :deep(.ant-avatar) {
  border: 2px solid var(--border-color-light);
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-color-hover));
  color: white;
  font-weight: 600;
}

.user-info:hover :deep(.ant-avatar) {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transform: scale(1.05);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
  transition: color 0.3s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.user-info:hover .user-name {
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info {
    gap: 8px;
    padding: 6px 8px;
  }
  
  .user-name {
    font-size: 13px;
    max-width: 80px;
  }
}

/* 不同尺寸的适配 */
.user-info[data-size="small"] {
  gap: 6px;
  padding: 4px 8px;
}

.user-info[data-size="small"] .user-name {
  font-size: 12px;
}

.user-info[data-size="large"] {
  gap: 16px;
  padding: 12px 16px;
}

.user-info[data-size="large"] .user-name {
  font-size: 16px;
  font-weight: 600;
}

/* 加载状态 */
.user-info.loading {
  opacity: 0.7;
  pointer-events: none;
}

.user-info.loading :deep(.ant-avatar) {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
