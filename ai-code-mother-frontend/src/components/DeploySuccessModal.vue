<template>
  <a-modal v-model:open="visible" title="部署成功" :footer="null" width="600px">
    <div class="deploy-success">
      <div class="success-icon">
        <CheckCircleOutlined style="color: #52c41a; font-size: 48px" />
      </div>
      <h3>网站部署成功！</h3>
      <p>你的网站已经成功部署，可以通过以下链接访问：</p>
      <div class="deploy-url">
        <a-input :value="deployUrl" readonly>
          <template #suffix>
            <a-button type="text" @click="handleCopyUrl">
              <CopyOutlined />
            </a-button>
          </template>
        </a-input>
      </div>
      <div class="deploy-actions">
        <a-button type="primary" @click="handleOpenSite">访问网站</a-button>
        <a-button @click="handleClose">关闭</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { message } from 'ant-design-vue'
import { CheckCircleOutlined, CopyOutlined } from '@ant-design/icons-vue'

interface Props {
  open: boolean
  deployUrl: string
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'open-site'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const handleCopyUrl = async () => {
  try {
    await navigator.clipboard.writeText(props.deployUrl)
    message.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制失败：', error)
    message.error('复制失败')
  }
}

const handleOpenSite = () => {
  emit('open-site')
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.deploy-success {
  text-align: center;
  padding: 32px 24px;
  background: var(--bg-color-container);
  border-radius: var(--border-radius-lg);
  position: relative;
  overflow: hidden;
}

.deploy-success::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--success-color), #73d13d);
}

.success-icon {
  margin-bottom: 24px;
  position: relative;
  display: inline-block;
}

.success-icon::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(82, 196, 26, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  animation: successPulse 2s ease-in-out infinite;
}

@keyframes successPulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.3;
  }
}

.success-icon :deep(.anticon) {
  position: relative;
  z-index: 1;
  color: var(--success-color) !important;
  font-size: 56px !important;
  filter: drop-shadow(0 4px 8px rgba(82, 196, 26, 0.3));
}

.deploy-success h3 {
  margin: 0 0 16px;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-color);
  background: linear-gradient(135deg, var(--success-color), #73d13d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.deploy-success p {
  margin: 0 0 32px;
  color: var(--text-color-secondary);
  font-size: 16px;
  line-height: 1.6;
}

.deploy-url {
  margin-bottom: 32px;
  position: relative;
}

.deploy-url :deep(.ant-input-affix-wrapper) {
  border-radius: var(--border-radius-lg);
  border: 2px solid var(--border-color-light);
  background: var(--bg-color-page);
  padding: 12px 16px;
  font-size: 14px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
}

.deploy-url :deep(.ant-input-affix-wrapper:hover) {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.deploy-url :deep(.ant-input) {
  background: transparent;
  color: var(--primary-color);
  font-weight: 500;
}

.deploy-url :deep(.ant-btn) {
  border: none;
  background: transparent;
  color: var(--text-color-secondary);
  transition: all 0.3s ease;
  border-radius: var(--border-radius-base);
}

.deploy-url :deep(.ant-btn:hover) {
  background: var(--primary-color);
  color: white;
  transform: scale(1.1);
}

.deploy-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.deploy-actions :deep(.ant-btn) {
  height: 44px;
  padding: 0 32px;
  border-radius: var(--border-radius-lg);
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
  min-width: 120px;
}

.deploy-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, var(--success-color), #73d13d);
  border: none;
  box-shadow: 0 6px 16px rgba(82, 196, 26, 0.3);
}

.deploy-actions :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(82, 196, 26, 0.4);
}

.deploy-actions :deep(.ant-btn:not(.ant-btn-primary)) {
  background: var(--bg-color-container);
  border: 2px solid var(--border-color-light);
  color: var(--text-color);
}

.deploy-actions :deep(.ant-btn:not(.ant-btn-primary):hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.15);
}

/* 模态框样式覆盖 */
:deep(.ant-modal) {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

:deep(.ant-modal-header) {
  background: linear-gradient(135deg, var(--success-color), #73d13d);
  border-bottom: none;
  padding: 20px 24px;
}

:deep(.ant-modal-title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.ant-modal-title)::before {
  content: '🎉';
  font-size: 20px;
}

:deep(.ant-modal-close) {
  color: white;
  opacity: 0.8;
  transition: all 0.3s ease;
}

:deep(.ant-modal-close:hover) {
  opacity: 1;
  transform: scale(1.1);
}

:deep(.ant-modal-body) {
  padding: 0;
  background: var(--bg-color-page);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .deploy-success {
    padding: 24px 16px;
  }
  
  .deploy-success h3 {
    font-size: 20px;
  }
  
  .deploy-success p {
    font-size: 14px;
    margin-bottom: 24px;
  }
  
  .deploy-url {
    margin-bottom: 24px;
  }
  
  .deploy-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .deploy-actions :deep(.ant-btn) {
    width: 100%;
    min-width: auto;
  }
}

/* 成功动画 */
.deploy-success {
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
