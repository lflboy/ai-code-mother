<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" />
      </a-form-item>
      <a-form-item label="创建者">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" />
      </a-form-item>
      <a-form-item label="生成类型">
        <a-select
          v-model:value="searchParams.codeGenType"
          placeholder="选择生成类型"
          style="width: 150px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option
            v-for="option in CODE_GEN_TYPE_OPTIONS"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
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
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image v-if="record.cover" :src="record.cover" :width="80" :height="60" />
          <div v-else class="no-cover">无封面</div>
        </template>
        <template v-else-if="column.dataIndex === 'initPrompt'">
          <a-tooltip :title="record.initPrompt">
            <div class="prompt-text">{{ record.initPrompt }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'codeGenType'">
          {{ formatCodeGenType(record.codeGenType) }}
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
          <span v-else>{{ record.priority || 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          <span v-if="record.deployedTime">
            {{ formatTime(record.deployedTime) }}
          </span>
          <span v-else class="text-gray">未部署</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.dataIndex === 'user'">
          <UserInfo :user="record.user" size="small" />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="editApp(record)"> 编辑 </a-button>
            <a-button
              type="default"
              size="small"
              @click="toggleFeatured(record)"
              :class="{ 'featured-btn': record.priority === 99 }"
            >
              {{ record.priority === 99 ? '取消精选' : '精选' }}
            </a-button>
            <a-popconfirm title="确定要删除这个应用吗？" @confirm="deleteApp(record.id)">
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
import { listAppVoByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
    fixed: 'left',
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    width: 150,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    width: 200,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 80,
  },
  {
    title: '部署时间',
    dataIndex: 'deployedTime',
    width: 160,
  },
  {
    title: '创建者',
    dataIndex: 'user',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAppVoByPageByAdmin({
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

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.id) return

  const newPriority = app.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: app.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      // 刷新数据
      fetchData()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 删除应用
const deleteApp = async (id: number | undefined) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#appManagePage {
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

/* 封面样式 */
.no-cover {
  width: 80px;
  height: 60px;
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-quaternary);
  font-size: 12px;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-light);
}

:deep(.ant-image) {
  border-radius: var(--border-radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-light);
}

/* 提示词文本样式 */
.prompt-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.4;
}

/* 文本颜色 */
.text-gray {
  color: var(--text-tertiary);
  font-style: italic;
}

/* 标签样式 */
:deep(.ant-tag) {
  border-radius: var(--border-radius-sm);
  font-weight: 500;
  padding: 4px 8px;
  border: none;
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

:deep(.ant-btn-default) {
  border-color: var(--border-color);
  color: var(--text-secondary);
}

:deep(.ant-btn-default:hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-1px);
}

.featured-btn {
  background: var(--warning-color) !important;
  border-color: var(--warning-color) !important;
  color: white !important;
}

.featured-btn:hover {
  background: #d48806 !important;
  border-color: #d48806 !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 8px rgba(250, 173, 20, 0.3) !important;
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

/* 响应式设计 */
@media (max-width: 1024px) {
  #appManagePage {
    padding: 24px 16px;
  }
  
  .ant-form {
    padding: 20px;
  }
  
  :deep(.ant-table-thead > tr > th),
  :deep(.ant-table-tbody > tr > td) {
    padding: 12px 8px;
  }
  
  .prompt-text {
    max-width: 150px;
  }
}

@media (max-width: 768px) {
  #appManagePage {
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
  
  .no-cover {
    width: 60px;
    height: 45px;
    font-size: 10px;
  }
  
  .prompt-text {
    max-width: 100px;
    font-size: 12px;
  }
  
  :deep(.ant-btn) {
    padding: 4px 8px;
    font-size: 12px;
    height: auto;
  }
}
</style>
