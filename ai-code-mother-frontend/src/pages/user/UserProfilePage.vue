<template>
  <div id="userProfilePage">
    <div class="profile-container">
      <div class="profile-header">
        <h1>个人中心</h1>
        <p>管理您的个人信息和账户设置</p>
      </div>

      <div class="profile-content">
        <!-- 用户信息卡片 -->
        <a-card class="user-info-card" title="基本信息">
          <template #extra>
            <a-button type="primary" @click="showEditModal">
              <EditOutlined />
              编辑信息
            </a-button>
          </template>

          <div class="user-info-display">
            <div class="avatar-section">
              <a-avatar
                :size="120"
                :src="userInfo.userAvatar || '/src/assets/defaultAvatar.png'"
                class="user-avatar"
              />
              <h2>{{ userInfo.userName || '未设置用户名' }}</h2>
              <a-tag :color="userInfo.userRole === 'admin' ? 'green' : 'blue'">
                {{ userInfo.userRole === 'admin' ? '管理员' : '普通用户' }}
              </a-tag>
            </div>

            <div class="info-section">
              <div class="info-item">
                <label>账号：</label>
                <span>{{ userInfo.userAccount || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ userInfo.userName || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>个人简介：</label>
                <span>{{ userInfo.userProfile || '这个人很懒，什么都没有留下' }}</span>
              </div>
              <div class="info-item">
                <label>注册时间：</label>
                <span>{{ dayjs(userInfo.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
              </div>
              <!--              <div class="info-item">-->
              <!--                <label>最后更新：</label>-->
              <!--                <span>{{ dayjs(userInfo.updateTime).format('YYYY-MM-DD HH:mm:ss') }}</span>-->
              <!--              </div>-->
              <div class="info-item">
                <label>注册时长：</label>
                <span
                  >{{
                    userInfo.createTime ? dayjs().diff(userInfo.createTime, 'day') : '0'
                  }}
                  天</span
                >
              </div>
            </div>
            <div class="user-score">
              <a-tag color="blue">积分：{{ userInfo.integral || 0 }}分</a-tag>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- 编辑个人信息模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑个人信息"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
      :confirm-loading="editLoading"
      width="600px"
    >
      <a-form ref="editFormRef" :model="editForm" :rules="editRules" layout="vertical">
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

        <a-form-item label="个人简介" name="userProfile">
          <a-textarea v-model:value="editForm.userProfile" placeholder="请输入个人简介" :rows="4" />
        </a-form-item>

        <a-form-item label="新密码" name="userPassword">
          <a-input-password v-model:value="editForm.userPassword" placeholder="留空则不修改密码" />
        </a-form-item>

        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password
            v-model:value="editForm.confirmPassword"
            placeholder="请再次输入新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { EditOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { getLoginUser, userUpdate } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import dayjs from 'dayjs'
import type { FormInstance, UploadFile } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

// 用户信息
const userInfo = ref<API.UserLoginVO>({})

// 编辑相关状态
const editModalVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const avatarFileList = ref<UploadFile[]>([])

// 编辑表单数据
const editForm = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
  userPassword: '',
  confirmPassword: '',
})

// 编辑表单验证规则
const editRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 20, message: '用户名不能超过20个字符', trigger: 'blur' },
  ],
  userPassword: [{ min: 6, message: '密码长度至少6位', trigger: 'blur' }],
  confirmPassword: [
    {
      validator: (rule: any, value: string) => {
        if (editForm.userPassword && value !== editForm.userPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      userInfo.value = res.data.data
    } else {
      message.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    message.error('获取用户信息失败')
  }
}

// 显示编辑模态框
const showEditModal = () => {
  editForm.userName = userInfo.value.userName || ''
  editForm.userAvatar = userInfo.value.userAvatar || ''
  editForm.userProfile = userInfo.value.userProfile || ''
  editForm.userPassword = ''
  editForm.confirmPassword = ''

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
      id: userInfo.value.id,
      userName: editForm.userName,
      userAvatar: editForm.userAvatar,
      userProfile: editForm.userProfile,
    }

    // 如果密码不为空，则包含密码字段
    if (editForm.userPassword && editForm.userPassword.trim()) {
      updateParams.userPassword = editForm.userPassword
    }

    const res = await userUpdate(updateParams)

    if (res.data.code === 0) {
      message.success('个人信息更新成功')
      editModalVisible.value = false

      // 更新本地用户信息
      await fetchUserInfo()

      // 更新全局用户状态
      loginUserStore.setLoginUser({
        ...loginUserStore.loginUser,
        userName: editForm.userName,
        userAvatar: editForm.userAvatar,
        userProfile: editForm.userProfile,
      })
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

    // 使用fetch调用上传接口
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

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
#userProfilePage {
  min-height: calc(100vh - 64px);
  background: var(--bg-secondary);
  padding: 32px;
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  text-align: center;
  margin-bottom: 32px;
}

.profile-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #722ed1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.profile-header p {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

.profile-content {
  display: flex;
  justify-content: center;
}

.user-info-card {
  width: 100%;
  max-width: 800px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
}

:deep(.ant-card-head) {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
}

:deep(.ant-card-head-title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.ant-card-body) {
  background: var(--bg-primary);
  padding: 32px;
}

.user-info-display {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  min-width: 200px;
}

.user-avatar {
  border: 4px solid var(--border-light);
  transition: all 0.3s;
}

.user-avatar:hover {
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.avatar-section h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 600;
  color: var(--text-primary);
  min-width: 80px;
  flex-shrink: 0;
}

.info-item span {
  color: var(--text-secondary);
  word-break: break-all;
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

:deep(.ant-input) {
  border-radius: var(--border-radius-md);
  border-color: var(--border-color);
  transition: all 0.3s;
}

:deep(.ant-input:hover) {
  border-color: var(--primary-hover);
}

:deep(.ant-input:focus) {
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

/* 响应式设计 */
@media (max-width: 768px) {
  #userProfilePage {
    padding: 16px;
  }

  .user-info-display {
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }

  .avatar-section {
    min-width: auto;
  }

  .info-section {
    width: 100%;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .info-item label {
    min-width: auto;
  }
}
</style>
