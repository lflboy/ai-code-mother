<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span>🤖</span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployKey" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="app.user?.userAvatar" :size="40">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          {{ app.user?.userName || (featured ? '官方' : '未知用户') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  background: var(--bg-primary);
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.02) 0%, rgba(114, 46, 209, 0.02) 100%);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.app-card:hover::before {
  opacity: 1;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-heavy);
  border-color: var(--primary-color);
}

.app-card--featured {
  border: 2px solid var(--warning-color);
  position: relative;
}

.app-card--featured::after {
  content: '精选';
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--warning-color);
  color: white;
  padding: 4px 8px;
  border-radius: var(--border-radius-sm);
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
  box-shadow: var(--shadow-light);
}

.app-preview {
  height: 200px;
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-quaternary) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.app-card:hover .app-preview img {
  transform: scale(1.05);
}

.app-placeholder {
  font-size: 56px;
  color: var(--text-quaternary);
  background: linear-gradient(135deg, var(--primary-color), var(--warning-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.9) 0%, rgba(114, 46, 209, 0.9) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s;
  backdrop-filter: blur(4px);
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.app-overlay .ant-btn {
  margin: 0 6px;
  border-radius: var(--border-radius-lg);
  font-weight: 500;
  box-shadow: var(--shadow-medium);
}

.app-overlay .ant-btn-primary {
  background: white;
  color: var(--primary-color);
  border-color: white;
}

.app-overlay .ant-btn-primary:hover {
  background: var(--primary-light);
  transform: translateY(-2px);
}

.app-overlay .ant-btn-default {
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-primary);
  border-color: rgba(255, 255, 255, 0.9);
}

.app-overlay .ant-btn-default:hover {
  background: white;
  transform: translateY(-2px);
}

.app-info {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--bg-primary);
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-left .ant-avatar {
  border: 2px solid var(--border-light);
  transition: all 0.3s;
}

.app-card:hover .app-info-left .ant-avatar {
  border-color: var(--primary-color);
  transform: scale(1.1);
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.app-author {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 400;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-preview {
    height: 160px;
  }
  
  .app-info {
    padding: 16px;
    gap: 12px;
  }
  
  .app-title {
    font-size: 15px;
  }
  
  .app-author {
    font-size: 13px;
  }
}
</style>
