<template>
  <div id="userLoginPage">
    <div class="login-container">
      <div class="login-header">
        <div class="logo-section">
          <img src="@/assets/logo.png" alt="Logo" class="login-logo" />
          <h1 class="brand-title">墨刻</h1>
        </div>
        <h2 class="title">用户登录</h2>
        <div class="desc">不写一行代码，生成完整应用</div>
      </div>
      
      <div class="login-form">
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
            label="密码"
            name="userPassword"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码长度不能小于 8 位' },
            ]"
          >
            <a-input-password 
              v-model:value="formState.userPassword" 
              placeholder="请输入密码" 
              size="large"
              prefix="🔒"
            />
          </a-form-item>
          
          <div class="form-footer">
            <div class="tips">
              没有账号？
              <RouterLink to="/user/register" class="register-link">立即注册</RouterLink>
            </div>
            
            <a-form-item class="submit-item">
              <a-button type="primary" html-type="submit" size="large" class="login-btn">
                登录
              </a-button>
            </a-form-item>
          </div>
        </a-form>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd 0%, #f5f5f5 50%, #eceff1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

#userLoginPage::before {
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

.login-container {
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

.login-header {
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

.login-logo {
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

.login-form {
  width: 100%;
}

.login-form :deep(.ant-form-item-label) {
  padding-bottom: 8px;
}

.login-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.login-form :deep(.ant-input-affix-wrapper) {
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  padding: 12px 16px;
  transition: all 0.3s;
  background: var(--bg-secondary);
}

.login-form :deep(.ant-input-affix-wrapper:hover) {
  border-color: var(--primary-hover);
  background: var(--bg-primary);
}

.login-form :deep(.ant-input-affix-wrapper-focused) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
  background: var(--bg-primary);
}

.login-form :deep(.ant-input) {
  background: transparent;
  border: none;
  font-size: 15px;
  padding: 0;
}

.login-form :deep(.ant-input::placeholder) {
  color: var(--text-quaternary);
}

.login-form :deep(.ant-input-prefix) {
  margin-right: 12px;
  font-size: 16px;
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

.register-link {
  color: var(--primary-color);
  font-weight: 500;
  text-decoration: none;
  transition: color 0.3s;
}

.register-link:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

.submit-item {
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--border-radius-lg);
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transition: all 0.3s;
  color: #ffffff;
}

.login-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
  color: #000000;
}

.login-btn:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  #userLoginPage {
    padding: 16px;
  }
  
  .login-container {
    padding: 32px 24px;
    max-width: 100%;
  }
  
  .title {
    font-size: 24px;
  }
  
  .desc {
    font-size: 15px;
  }
  
  .login-form :deep(.ant-input-affix-wrapper) {
    padding: 10px 14px;
  }
  
  .login-btn {
    height: 44px;
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 24px 20px;
  }
  
  .logo-section {
    gap: 8px;
    margin-bottom: 20px;
  }
  
  .login-logo {
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
