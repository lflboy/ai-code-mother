<template>
  <div id="appEditPage">
    <div class="page-header">
      <h1>编辑应用信息</h1>
    </div>

    <div class="edit-container">
      <a-card title="基本信息" :loading="loading">
        <a-form
          :model="formData"
          :rules="rules"
          layout="vertical"
          @finish="handleSubmit"
          ref="formRef"
        >
          <a-form-item label="应用名称" name="appName">
            <a-input
              v-model:value="formData.appName"
              placeholder="请输入应用名称"
              :maxlength="50"
              show-count
            />
          </a-form-item>

          <a-form-item
            v-if="isAdmin"
            label="应用封面"
            name="cover"
            extra="支持图片链接，建议尺寸：400x300"
          >
            <a-input v-model:value="formData.cover" placeholder="请输入封面图片链接" />
            <div v-if="formData.cover" class="cover-preview">
              <a-image
                :src="formData.cover"
                :width="200"
                :height="150"
                fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
              />
            </div>
          </a-form-item>

          <a-form-item v-if="isAdmin" label="优先级" name="priority" extra="设置为99表示精选应用">
            <a-input-number
              v-model:value="formData.priority"
              :min="0"
              :max="99"
              style="width: 200px"
            />
          </a-form-item>

          <a-form-item label="初始提示词" name="initPrompt">
            <a-textarea
              v-model:value="formData.initPrompt"
              placeholder="请输入初始提示词"
              :rows="4"
              :maxlength="1000"
              show-count
              disabled
            />
            <div class="form-tip">初始提示词不可修改</div>
          </a-form-item>

          <a-form-item label="生成类型" name="codeGenType">
            <a-input
              :value="formatCodeGenType(formData.codeGenType)"
              placeholder="生成类型"
              disabled
            />
            <div class="form-tip">生成类型不可修改</div>
          </a-form-item>

          <a-form-item v-if="formData.deployKey" label="部署密钥" name="deployKey">
            <a-input v-model:value="formData.deployKey" placeholder="部署密钥" disabled />
            <div class="form-tip">部署密钥不可修改</div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="submitting">
                保存修改
              </a-button>
              <a-button @click="resetForm">重置</a-button>
              <a-button type="link" @click="goToChat">进入对话</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 应用信息展示 -->
      <a-card title="应用信息" style="margin-top: 24px">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="应用ID">
            {{ appInfo?.id }}
          </a-descriptions-item>
          <a-descriptions-item label="创建者">
            <UserInfo :user="appInfo?.user" size="small" />
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatTime(appInfo?.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatTime(appInfo?.updateTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="部署时间">
            {{ appInfo?.deployedTime ? formatTime(appInfo.deployedTime) : '未部署' }}
          </a-descriptions-item>
          <a-descriptions-item label="访问链接">
            <a-button v-if="appInfo?.deployKey" type="link" @click="openPreview" size="small">
              查看预览
            </a-button>
            <span v-else>未部署</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'
import { getStaticPreviewUrl } from '@/config/env'
import type { FormInstance } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  appName: '',
  cover: '',
  priority: 0,
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
})

// 是否为管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在1-50个字符', trigger: 'blur' },
  ],
  cover: [{ type: 'url', message: '请输入有效的URL', trigger: 'blur' }],
  priority: [{ type: 'number', min: 0, max: 99, message: '优先级范围0-99', trigger: 'blur' }],
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 检查权限
      if (!isAdmin.value && appInfo.value.userId !== loginUserStore.loginUser.id) {
        message.error('您没有权限编辑此应用')
        router.push('/')
        return
      }

      // 填充表单数据
      formData.appName = appInfo.value.appName || ''
      formData.cover = appInfo.value.cover || ''
      formData.priority = appInfo.value.priority || 0
      formData.initPrompt = appInfo.value.initPrompt || ''
      formData.codeGenType = appInfo.value.codeGenType || ''
      formData.deployKey = appInfo.value.deployKey || ''
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!appInfo.value?.id) return

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以修改更多字段
      res = await updateAppByAdmin({
        id: appInfo.value.id,
        appName: formData.appName,
        cover: formData.cover,
        priority: formData.priority,
      })
    } else {
      // 普通用户只能修改应用名称
      res = await updateApp({
        id: appInfo.value.id,
        appName: formData.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('修改成功')
      // 重新获取应用信息
      await fetchAppInfo()
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (error) {
    console.error('修改失败：', error)
    message.error('修改失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (appInfo.value) {
    formData.appName = appInfo.value.appName || ''
    formData.cover = appInfo.value.cover || ''
    formData.priority = appInfo.value.priority || 0
  }
  formRef.value?.clearValidate()
}

// 进入对话页面
const goToChat = () => {
  if (appInfo.value?.id) {
    router.push(`/app/chat/${appInfo.value.id}`)
  }
}

// 打开预览
const openPreview = () => {
  if (appInfo.value?.codeGenType && appInfo.value?.id) {
    const url = getStaticPreviewUrl(appInfo.value.codeGenType, String(appInfo.value.id))
    window.open(url, '_blank')
  }
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
#appEditPage {
  padding: 32px;
  max-width: 1000px;
  margin: 0 auto;
  background: var(--bg-secondary);
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding: 24px 0;
  border-bottom: 1px solid var(--border-light);
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  position: relative;
  padding-left: 16px;
}

.page-header h1::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 28px;
  background: linear-gradient(135deg, var(--primary-color), #722ed1);
  border-radius: 2px;
}

.edit-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 卡片样式 */
:deep(.ant-card) {
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-medium);
  border: 1px solid var(--border-light);
  background: var(--bg-primary);
  transition: all 0.3s;
}

:deep(.ant-card:hover) {
  box-shadow: var(--shadow-heavy);
  transform: translateY(-2px);
}

:deep(.ant-card-head) {
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-quaternary) 100%);
  border-bottom: 1px solid var(--border-light);
  border-radius: var(--border-radius-xl) var(--border-radius-xl) 0 0;
  padding: 20px 24px;
}

:deep(.ant-card-head-title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.ant-card-body) {
  padding: 24px;
}

/* 表单样式 */
:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

:deep(.ant-form-item-label > label.ant-form-item-required::before) {
  color: var(--error-color);
}

:deep(.ant-input),
:deep(.ant-input-number),
:deep(.ant-textarea) {
  border-radius: var(--border-radius-lg);
  border-color: var(--border-color);
  transition: all 0.3s;
  font-size: 14px;
}

:deep(.ant-input:hover),
:deep(.ant-input-number:hover),
:deep(.ant-textarea:hover) {
  border-color: var(--primary-hover);
}

:deep(.ant-input:focus),
:deep(.ant-input-number:focus),
:deep(.ant-textarea:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

:deep(.ant-input:disabled),
:deep(.ant-textarea:disabled) {
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  border-color: var(--border-light);
}

:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-input-number-input) {
  border-radius: var(--border-radius-lg);
}

/* 按钮样式 */
:deep(.ant-btn) {
  border-radius: var(--border-radius-lg);
  font-weight: 500;
  height: 40px;
  padding: 0 20px;
  transition: all 0.3s;
}

:deep(.ant-btn-primary) {
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

:deep(.ant-btn-primary:hover) {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
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

:deep(.ant-btn-link) {
  color: var(--primary-color);
  font-weight: 500;
}

:deep(.ant-btn-link:hover) {
  color: var(--primary-hover);
  background: var(--primary-light);
  transform: translateY(-1px);
}

/* 封面预览样式 */
.cover-preview {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-lg);
  background: var(--bg-secondary);
  display: flex;
  justify-content: center;
}

.cover-preview :deep(.ant-image) {
  border-radius: var(--border-radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-light);
}

/* 表单提示样式 */
.form-tip {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 6px;
  font-style: italic;
}

/* 描述列表样式 */
:deep(.ant-descriptions) {
  background: var(--bg-primary);
}

:deep(.ant-descriptions-item-label) {
  background: var(--bg-tertiary);
  font-weight: 500;
  color: var(--text-primary);
  border-color: var(--border-light);
}

:deep(.ant-descriptions-item-content) {
  background: var(--bg-primary);
  color: var(--text-secondary);
  border-color: var(--border-light);
}

:deep(.ant-descriptions-bordered .ant-descriptions-item-label),
:deep(.ant-descriptions-bordered .ant-descriptions-item-content) {
  padding: 16px;
}

/* 用户信息组件样式 */
:deep(.ant-avatar) {
  border: 2px solid var(--border-light);
  box-shadow: var(--shadow-light);
}

/* 图片组件样式 */
:deep(.ant-image-img) {
  border-radius: var(--border-radius-md);
}

/* 输入数字组件样式 */
:deep(.ant-input-number-handler-wrap) {
  border-radius: 0 var(--border-radius-lg) var(--border-radius-lg) 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  #appEditPage {
    padding: 24px 20px;
  }
  
  .page-header {
    margin-bottom: 24px;
    padding: 20px 0;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  :deep(.ant-card-body) {
    padding: 20px;
  }
  
  :deep(.ant-descriptions) {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  #appEditPage {
    padding: 20px 16px;
  }
  
  .page-header {
    margin-bottom: 20px;
    padding: 16px 0;
  }
  
  .page-header h1 {
    font-size: 22px;
    padding-left: 12px;
  }
  
  .page-header h1::before {
    width: 3px;
    height: 22px;
  }
  
  .edit-container {
    gap: 20px;
  }
  
  :deep(.ant-card-head) {
    padding: 16px 20px;
  }
  
  :deep(.ant-card-head-title) {
    font-size: 16px;
  }
  
  :deep(.ant-card-body) {
    padding: 16px;
  }
  
  :deep(.ant-btn) {
    height: 36px;
    padding: 0 16px;
    font-size: 14px;
  }
  
  :deep(.ant-descriptions-bordered .ant-descriptions-item-label),
  :deep(.ant-descriptions-bordered .ant-descriptions-item-content) {
    padding: 12px;
    font-size: 13px;
  }
  
  .cover-preview {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  #appEditPage {
    padding: 16px 12px;
  }
  
  .page-header h1 {
    font-size: 20px;
  }
  
  :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  :deep(.ant-card-body) {
    padding: 12px;
  }
  
  :deep(.ant-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.ant-btn) {
    height: 32px;
    padding: 0 12px;
    font-size: 13px;
  }
  
  :deep(.ant-space) {
    flex-wrap: wrap;
  }
  
  :deep(.ant-descriptions) {
    font-size: 12px;
  }
  
  :deep(.ant-descriptions-bordered .ant-descriptions-item-label),
  :deep(.ant-descriptions-bordered .ant-descriptions-item-content) {
    padding: 8px;
  }
}
</style>
