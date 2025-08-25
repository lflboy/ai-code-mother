<template>
  <a-modal v-model:open="visible" title="应用详情" :footer="null" width="500px">
    <div class="app-detail-content">
      <!-- 应用基础信息 -->
      <div class="app-basic-info">
        <div class="info-item">
          <span class="info-label">创建者：</span>
          <UserInfo :user="app?.user" size="small" />
        </div>
        <div class="info-item">
          <span class="info-label">创建时间：</span>
          <span>{{ formatTime(app?.createTime) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">生成类型：</span>
          <a-tag v-if="app?.codeGenType" color="blue">
            {{ formatCodeGenType(app.codeGenType) }}
          </a-tag>
          <span v-else>未知类型</span>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div v-if="showActions" class="app-actions">
        <a-space>
          <a-button type="primary" @click="handleEdit">
            <template #icon>
              <EditOutlined />
            </template>
            修改
          </a-button>
          <a-popconfirm
            title="确定要删除这个应用吗？"
            @confirm="handleDelete"
            ok-text="确定"
            cancel-text="取消"
          >
            <a-button danger>
              <template #icon>
                <DeleteOutlined />
              </template>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatTime } from '@/utils/time'
import {formatCodeGenType} from "../utils/codeGenTypes.ts";

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}
</script>

<style scoped>
.app-detail-content {
  padding: 16px 0;
}

.app-basic-info {
  margin-bottom: 32px;
  background: var(--bg-color-container);
  border-radius: var(--border-radius-lg);
  padding: 20px;
  border: 1px solid var(--border-color-light);
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color-split);
  transition: all 0.3s ease;
}

.info-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.info-item:hover {
  background: var(--bg-color-hover);
  border-radius: var(--border-radius-base);
  padding-left: 8px;
  padding-right: 8px;
}

.info-label {
  width: 90px;
  color: var(--text-color-secondary);
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
  position: relative;
}

.info-label::after {
  content: '';
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 12px;
  background: var(--primary-color);
  border-radius: 1px;
  opacity: 0.6;
}

.app-actions {
  padding: 20px;
  background: var(--bg-color-container);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color-light);
  margin-top: 16px;
}

.app-actions :deep(.ant-btn) {
  height: 40px;
  border-radius: var(--border-radius-base);
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
}

.app-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-color-hover));
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.app-actions :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.app-actions :deep(.ant-btn-dangerous) {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

.app-actions :deep(.ant-btn-dangerous:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.4);
}

/* 模态框样式覆盖 */
:deep(.ant-modal) {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

:deep(.ant-modal-header) {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-color-hover));
  border-bottom: none;
  padding: 20px 24px;
}

:deep(.ant-modal-title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
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
  padding: 24px;
  background: var(--bg-color-page);
}

/* 标签样式增强 */
:deep(.ant-tag) {
  border-radius: var(--border-radius-base);
  padding: 4px 12px;
  font-weight: 500;
  border: none;
  box-shadow: var(--shadow-light);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-basic-info {
    padding: 16px;
    margin-bottom: 24px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .info-label {
    width: auto;
  }
  
  .info-label::after {
    display: none;
  }
  
  .app-actions {
    padding: 16px;
  }
  
  .app-actions :deep(.ant-space) {
    width: 100%;
  }
  
  .app-actions :deep(.ant-btn) {
    flex: 1;
  }
}
</style>
