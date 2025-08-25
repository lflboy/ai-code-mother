<template>
  <div id="chatManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="消息内容">
        <a-input v-model:value="searchParams.message" placeholder="输入消息内容" />
      </a-form-item>
      <a-form-item label="消息类型">
        <a-select
          v-model:value="searchParams.messageType"
          placeholder="选择消息类型"
          style="width: 120px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="user">用户消息</a-select-option>
          <a-select-option value="assistant">AI消息</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="应用ID">
        <a-input v-model:value="searchParams.appId" placeholder="输入应用ID" />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 1400 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'message'">
          <a-tooltip :title="record.message">
            <div class="message-text">{{ record.message }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'messageType'">
          <a-tag :color="record.messageType === 'user' ? 'blue' : 'green'">
            {{ record.messageType === 'user' ? '用户消息' : 'AI消息' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="viewAppChat(record.appId)">
              查看对话
            </a-button>
            <a-popconfirm title="确定要删除这条消息吗？" @confirm="deleteMessage(record.id)">
              <a-button danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import { formatTime } from '@/utils/time'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
    fixed: 'left',
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    width: 300,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    width: 100,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    width: 80,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.ChatHistory[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAllChatHistoryByPageForAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 查看应用对话
const viewAppChat = (appId: number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 删除消息
const deleteMessage = async (id: number | undefined) => {
  if (!id) return

  try {
    // 注意：这里需要后端提供删除对话历史的接口
    // 目前先显示成功，实际实现需要调用删除接口
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#chatManagePage {
  padding: 32px;
  background: var(--bg-secondary);
  min-height: calc(100vh - 64px);
}

/* 搜索表单样式 */
.ant-form {
  background: var(--bg-primary);
  padding: 24px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  margin-bottom: 24px;
}

.ant-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.ant-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--text-primary);
}

.ant-form :deep(.ant-input) {
  border-radius: var(--border-radius-md);
  border-color: var(--border-color);
  transition: all 0.3s;
}

.ant-form :deep(.ant-input:hover) {
  border-color: var(--primary-hover);
}

.ant-form :deep(.ant-input:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.ant-form :deep(.ant-select) {
  border-radius: var(--border-radius-md);
}

.ant-form :deep(.ant-select-selector) {
  border-color: var(--border-color);
  border-radius: var(--border-radius-md);
  transition: all 0.3s;
}

.ant-form :deep(.ant-select:hover .ant-select-selector) {
  border-color: var(--primary-hover);
}

.ant-form :deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.ant-form :deep(.ant-btn-primary) {
  border-radius: var(--border-radius-md);
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.ant-form :deep(.ant-btn-primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

/* 分割线样式 */
.ant-divider {
  border-color: var(--border-light);
  margin: 24px 0;
}

/* 表格样式 */
.ant-table-wrapper {
  background: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

:deep(.ant-table) {
  border-radius: var(--border-radius-lg);
}

:deep(.ant-table-thead > tr > th) {
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-light);
  font-weight: 600;
  color: var(--text-primary);
  padding: 16px;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
  padding: 16px;
  border-bottom: 1px solid var(--border-light);
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: var(--bg-secondary);
}

:deep(.ant-table-tbody > tr:last-child > td) {
  border-bottom: none;
}

/* 消息文本样式 */
.message-text {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.4;
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-light);
}

/* 标签样式 */
:deep(.ant-tag) {
  border-radius: var(--border-radius-sm);
  font-weight: 500;
  padding: 4px 8px;
  border: none;
}

:deep(.ant-tag-blue) {
  background: var(--primary-color);
  color: white;
}

:deep(.ant-tag-green) {
  background: var(--success-color);
  color: white;
}

/* 按钮样式 */
:deep(.ant-btn) {
  border-radius: var(--border-radius-md);
  font-weight: 500;
  transition: all 0.3s;
}

:deep(.ant-btn-primary) {
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

:deep(.ant-btn-primary:hover) {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

:deep(.ant-btn-dangerous) {
  border-color: var(--error-color);
  color: var(--error-color);
}

:deep(.ant-btn-dangerous:hover) {
  background: var(--error-color);
  border-color: var(--error-color);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(255, 77, 79, 0.3);
}

/* 操作按钮间距 */
:deep(.ant-space-item) {
  margin-right: 8px;
}

:deep(.ant-space-item:last-child) {
  margin-right: 0;
}

/* 分页样式 */
:deep(.ant-pagination) {
  margin-top: 24px;
  text-align: center;
}

:deep(.ant-pagination-item) {
  border-radius: var(--border-radius-md);
  border-color: var(--border-light);
  transition: all 0.3s;
}

:deep(.ant-pagination-item:hover) {
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

:deep(.ant-pagination-item-active) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.ant-pagination-prev),
:deep(.ant-pagination-next) {
  border-radius: var(--border-radius-md);
  border-color: var(--border-light);
  transition: all 0.3s;
}

:deep(.ant-pagination-prev:hover),
:deep(.ant-pagination-next:hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 工具提示样式 */
:deep(.ant-tooltip-inner) {
  background: var(--text-primary);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-medium);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  #chatManagePage {
    padding: 24px 16px;
  }
  
  .ant-form {
    padding: 20px;
  }
  
  :deep(.ant-table-thead > tr > th),
  :deep(.ant-table-tbody > tr > td) {
    padding: 12px 8px;
  }
  
  .message-text {
    max-width: 200px;
  }
}

@media (max-width: 768px) {
  #chatManagePage {
    padding: 16px 12px;
  }
  
  .ant-form {
    padding: 16px;
  }
  
  :deep(.ant-form-inline) {
    display: block;
  }
  
  :deep(.ant-form-inline .ant-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  
  :deep(.ant-table-thead > tr > th),
  :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
    font-size: 13px;
  }
  
  .message-text {
    max-width: 150px;
    padding: 6px 10px;
    font-size: 12px;
  }
  
  :deep(.ant-btn) {
    padding: 4px 8px;
    font-size: 12px;
    height: auto;
  }
  
  :deep(.ant-tag) {
    font-size: 11px;
    padding: 2px 6px;
  }
}

@media (max-width: 480px) {
  .message-text {
    max-width: 100px;
  }
  
  :deep(.ant-space) {
    flex-direction: column;
    align-items: flex-start;
  }
  
  :deep(.ant-space-item) {
    margin-right: 0;
    margin-bottom: 4px;
  }
  
  :deep(.ant-space-item:last-child) {
    margin-bottom: 0;
  }
}
</style>
