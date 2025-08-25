<template>
  <div id="userRegisterPage">
    <div class="register-container">
      <div class="register-header">
        <div class="logo-section">
          <img src="@/assets/logo.png" alt="Logo" class="register-logo" />
          <h1 class="brand-title">墨刻</h1>
        </div>
        <h2 class="title">用户注册</h2>
        <div class="desc">不写一行代码，生成完整应用</div>
      </div>
      
      <div class="register-form">
        <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit" layout="vertical">
          <a-form-item 
            label="账号" 
            name="userAccount" 
            :rules="[{ required: true, message: '请输入账号' }]"
          >
            <a-input 
              v-model:value="formState.userAccount" 
              placeholder="请输入账号" 
              size="large"
              prefix="👤"
            />
          </a-form-item>

          <a-form-item 
            label="邮箱" 
            name="email" 
            :rules="[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '请输入正确的邮箱格式' }
            ]"
          >
            <a-input 
              v-model:value="formState.email" 
              placeholder="请输入邮箱" 
              size="large"
              prefix="📧"
            />
          </a-form-item>

          <a-form-item 
            label="验证码" 
            name="code" 
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <div class="captcha-input-group">
              <a-input 
                v-model:value="formState.code" 
                placeholder="请输入验证码" 
                size="large"
                prefix="🔢"
                class="captcha-input"
              />
              <a-button 
                size="large" 
                :disabled="isCountingDown"
                @click="handleGetCaptcha"
                class="captcha-btn"
              >
                {{ isCountingDown ? `${countdown}s后重新获取` : '获取验证码' }}
              </a-button>
            </div>
          </a-form-item>
          
          <a-form-item
            label="密码"
            name="userPassword"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码不能小于 8 位' },
            ]"
          >
            <a-input-password 
              v-model:value="formState.userPassword" 
              placeholder="请输入密码" 
              size="large"
              prefix="🔒"
            />
          </a-form-item>
          
          <a-form-item
            label="确认密码"
            name="checkPassword"
            :rules="[
              { required: true, message: '请确认密码' },
              { min: 8, message: '密码不能小于 8 位' },
              { validator: validateCheckPassword },
            ]"
          >
            <a-input-password 
              v-model:value="formState.checkPassword" 
              placeholder="请确认密码" 
              size="large"
              prefix="🔐"
            />
          </a-form-item>
          
          <div class="form-footer">
            <div class="tips">
              已有账号？
              <RouterLink to="/user/login" class="login-link">立即登录</RouterLink>
            </div>
            
            <a-form-item class="submit-item">
              <a-button type="primary" html-type="submit" size="large" class="register-btn">
                注册
              </a-button>
            </a-form-item>
          </div>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister, getCaptcha } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  email: '',
  code: '',
})

// 验证码倒计时相关
const countdown = ref(0)
const isCountingDown = ref(false)

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 获取验证码
 */
const handleGetCaptcha = async () => {
  if (!formState.email) {
    message.error('请先输入邮箱')
    return
  }
  
  // 简单的邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(formState.email)) {
    message.error('请输入正确的邮箱格式')
    return
  }
  
  try {
    const res = await getCaptcha(formState.email)
    if (res.data.code === 0) {
      message.success('验证码已发送到您的邮箱')
      // 开始倒计时
      startCountdown()
    } else {
      message.error('获取验证码失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取验证码失败')
  }
}

/**
 * 开始倒计时
 */
const startCountdown = () => {
  countdown.value = 60
  isCountingDown.value = true
  
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      isCountingDown.value = false
    }
  }, 1000)
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd 0%, #f5f5f5 50%, #eceff1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

#userRegisterPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    linear-gradient(135deg, rgba(33, 150, 243, 0.08) 0%, transparent 40%),
    linear-gradient(-135deg, rgba(96, 125, 139, 0.05) 0%, transparent 40%),
    radial-gradient(circle at 20% 80%, rgba(33, 150, 243, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(96, 125, 139, 0.04) 0%, transparent 50%);
  pointer-events: none;
}

.register-container {
  background: var(--bg-primary);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-heavy);
  border: 1px solid var(--border-light);
  padding: 48px;
  width: 100%;
  max-width: 480px;
  position: relative;
  z-index: 1;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.register-logo {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-light);
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, #722ed1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 12px;
  color: var(--text-primary);
}

.desc {
  color: var(--text-tertiary);
  font-size: 16px;
  margin: 0;
  font-weight: 400;
}

.register-form {
  width: 100%;
}

.register-form :deep(.ant-form-item-label) {
  padding-bottom: 8px;
}

.register-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.register-form :deep(.ant-input-affix-wrapper) {
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  padding: 12px 16px;
  transition: all 0.3s;
  background: var(--bg-secondary);
}

.register-form :deep(.ant-input-affix-wrapper:hover) {
  border-color: var(--primary-hover);
  background: var(--bg-primary);
}

.register-form :deep(.ant-input-affix-wrapper-focused) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
  background: var(--bg-primary);
}

.register-form :deep(.ant-input) {
  background: transparent;
  border: none;
  font-size: 15px;
  padding: 0;
}

.register-form :deep(.ant-input::placeholder) {
  color: var(--text-quaternary);
}

.register-form :deep(.ant-input-prefix) {
  margin-right: 12px;
  font-size: 16px;
}

.captcha-input-group {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-btn {
  flex-shrink: 0;
  min-width: 120px;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 500;
  transition: all 0.3s;
}

.captcha-btn:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--bg-primary);
}

.captcha-btn:disabled {
  background: var(--bg-tertiary);
  border-color: var(--border-light);
  color: var(--text-quaternary);
  cursor: not-allowed;
}

.form-footer {
  margin-top: 24px;
}

.tips {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
  margin-bottom: 24px;
  font-weight: 400;
}

.login-link {
  color: var(--primary-color);
  font-weight: 500;
  text-decoration: none;
  transition: color 0.3s;
}

.login-link:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

.submit-item {
  margin-bottom: 0;
}

.register-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--border-radius-lg);
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transition: all 0.3s;
}

.register-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  #userRegisterPage {
    padding: 16px;
  }
  
  .register-container {
    padding: 32px 24px;
    max-width: 100%;
  }
  
  .title {
    font-size: 24px;
  }
  
  .desc {
    font-size: 15px;
  }
  
  .register-form :deep(.ant-input-affix-wrapper) {
    padding: 10px 14px;
  }
  
  .register-btn {
    height: 44px;
    font-size: 15px;
  }

  .captcha-input-group {
    flex-direction: column;
    gap: 8px;
  }

  .captcha-btn {
    min-width: auto;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 24px 20px;
  }
  
  .logo-section {
    gap: 8px;
    margin-bottom: 20px;
  }
  
  .register-logo {
    width: 40px;
    height: 40px;
  }
  
  .brand-title {
    font-size: 20px;
  }
  
  .title {
    font-size: 22px;
  }
}
</style>