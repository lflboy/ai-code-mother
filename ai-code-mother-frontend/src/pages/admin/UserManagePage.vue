<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
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
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" @click="doEdit(record)">编辑</a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 编辑用户模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑用户"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
      :confirm-loading="editLoading"
      width="600px"
    >
      <a-form ref="editFormRef" :model="editForm" :rules="editRules" layout="vertical">
        <a-form-item label="用户ID" name="id">
          <a-input v-model:value="editForm.id" disabled />
        </a-form-item>

        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="editForm.userAccount" placeholder="请输入账号" />
        </a-form-item>

        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
        </a-form-item>

        <a-form-item label="头像" name="userAvatar">
          <div class="avatar-upload-container">
            <a-upload
              v-model:file-list="avatarFileList"
              :before-upload="beforeAvatarUpload"
              :custom-request="handleAvatarUpload"
              list-type="picture-card"
              :show-upload-list="false"
              accept="image/*"
            >
              <div v-if="editForm.userAvatar" class="avatar-preview">
                <img
                  :src="editForm.userAvatar"
                  alt="avatar"
                  style="width: 100%; height: 100%; object-fit: cover"
                />
                <div class="avatar-overlay">
                  <upload-outlined />
                </div>
              </div>
              <div v-else class="avatar-upload-placeholder">
                <upload-outlined />
                <div style="margin-top: 8px">上传头像</div>
              </div>
            </a-upload>
          </div>
        </a-form-item>

        <a-form-item label="简介" name="userProfile">
          <a-textarea v-model:value="editForm.userProfile" placeholder="请输入用户简介" :rows="3" />
        </a-form-item>

        <a-form-item label="用户角色" name="userRole">
          <a-select v-model:value="editForm.userRole" placeholder="请选择用户角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="新密码" name="userPassword">
          <a-input-password v-model:value="editForm.userPassword" placeholder="留空则不修改密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'
import { upload } from '@/api/fileController.ts'
import { message, Modal } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import type { FormInstance, UploadFile } from 'ant-design-vue'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

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

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = (id: string) => {
  if (!id) {
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个用户吗？此操作不可恢复。',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const res = await deleteUser({ id })
        if (res.data.code === 0) {
          message.success('删除成功')
          fetchData() // 刷新数据
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    },
  })
}

// 编辑相关状态
const editModalVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const avatarFileList = ref<UploadFile[]>([])

// 编辑表单数据
const editForm = reactive<API.UserUpdateRequest>({
  id: undefined,
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
  userPassword: '',
})

// 编辑表单验证规则
const editRules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 16, message: '账号长度为4-16位', trigger: 'blur' },
  ],
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 20, message: '用户名不能超过20个字符', trigger: 'blur' },
  ],
  userRole: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
}

// 打开编辑模态框
const doEdit = (record: API.UserVO) => {
  editForm.id = record.id
  editForm.userAccount = record.userAccount || ''
  editForm.userName = record.userName || ''
  editForm.userAvatar = record.userAvatar || ''
  editForm.userProfile = record.userProfile || ''
  editForm.userRole = record.userRole || 'user'
  editForm.userPassword = '' // 密码字段清空

  editModalVisible.value = true
}

// 处理编辑提交
const handleEditSubmit = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()
    editLoading.value = true

    // 构建更新请求参数
    const updateParams: API.UserUpdateRequest = {
      id: editForm.id,
      userAccount: editForm.userAccount,
      userName: editForm.userName,
      userAvatar: editForm.userAvatar,
      userProfile: editForm.userProfile,
      userRole: editForm.userRole,
    }

    // 如果密码不为空，则包含密码字段
    if (editForm.userPassword && editForm.userPassword.trim()) {
      updateParams.userPassword = editForm.userPassword
    }

    const res = await updateUser(updateParams)

    if (res.data.code === 0) {
      message.success('用户信息更新成功')
      editModalVisible.value = false
      fetchData() // 刷新列表
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    editLoading.value = false
  }
}

// 取消编辑
const handleEditCancel = () => {
  editModalVisible.value = false
  editFormRef.value?.resetFields()
  avatarFileList.value = []
}

// 头像上传前的验证
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }

  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }

  return true
}

// 处理头像上传
const handleAvatarUpload = async (options: any) => {
  const { file } = options

  try {
    const formData = new FormData()
    formData.append('file', file)

    // const response = await upload(formData)
    // 使用request库调用上传接口
    const response = await fetch('/api/file/upload', {
      method: 'POST',
      body: formData,
    })

    const res = await response.json()

    if (res.code === 0 && res.data) {
      editForm.userAvatar = res.data
      message.success('头像上传成功')
    } else {
      message.error('头像上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    message.error('头像上传失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
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
  padding: 16px;
  border-bottom: 1px solid var(--border-light);
  vertical-align: middle;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: var(--bg-secondary);
}

:deep(.ant-table-tbody > tr:last-child > td) {
  border-bottom: none;
}

/* 头像样式 */
:deep(.ant-image) {
  border-radius: var(--border-radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-light);
}

:deep(.ant-image-img) {
  border-radius: var(--border-radius-md);
}

/* 标签样式 */
:deep(.ant-tag) {
  border-radius: var(--border-radius-sm);
  font-weight: 500;
  padding: 4px 8px;
  border: none;
}

:deep(.ant-tag-green) {
  background: var(--success-color);
  color: white;
}

:deep(.ant-tag-blue) {
  background: var(--primary-color);
  color: white;
}

/* 按钮样式 */
:deep(.ant-btn) {
  border-radius: var(--border-radius-md);
  font-weight: 500;
  transition: all 0.3s;
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
  #userManagePage {
    padding: 24px 16px;
  }

  .ant-form {
    padding: 20px;
  }

  :deep(.ant-table-thead > tr > th),
  :deep(.ant-table-tbody > tr > td) {
    padding: 12px 8px;
  }
}

@media (max-width: 768px) {
  #userManagePage {
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

  :deep(.ant-image) {
    width: 60px !important;
    height: 60px !important;
  }

  :deep(.ant-btn) {
    padding: 4px 8px;
    font-size: 12px;
    height: auto;
  }
}

/* 头像上传样式 */
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-preview {
  position: relative;
  width: 104px;
  height: 104px;
  border-radius: var(--border-radius-md);
  overflow: hidden;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-upload-placeholder {
  width: 104px;
  height: 104px;
  border: 2px dashed var(--border-color);
  border-radius: var(--border-radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-secondary);
}

.avatar-upload-placeholder:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 模态框样式 */
:deep(.ant-modal-header) {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
}

:deep(.ant-modal-title) {
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.ant-modal-body) {
  background: var(--bg-primary);
  padding: 24px;
}

:deep(.ant-modal-footer) {
  background: var(--bg-primary);
  border-top: 1px solid var(--border-light);
}

/* 表单样式优化 */
:deep(.ant-form-vertical .ant-form-item-label > label) {
  font-weight: 500;
  color: var(--text-primary);
}

:deep(.ant-select) {
  border-radius: var(--border-radius-md);
}

:deep(.ant-select:hover .ant-select-selector) {
  border-color: var(--primary-hover);
}

:deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

:deep(.ant-textarea) {
  border-radius: var(--border-radius-md);
}

:deep(.ant-textarea:hover) {
  border-color: var(--primary-hover);
}

:deep(.ant-textarea:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

:deep(.ant-input-password) {
  border-radius: var(--border-radius-md);
}

:deep(.ant-input-password:hover) {
  border-color: var(--primary-hover);
}

:deep(.ant-input-password:focus-within) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}
</style>
