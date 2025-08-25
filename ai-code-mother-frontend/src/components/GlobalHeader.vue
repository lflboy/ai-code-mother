<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">墨刻</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar
                  :src="
                    loginUserStore.loginUser.userAvatar == null
                      ? '/src/assets/defaultAvatar.png'
                      : loginUserStore.loginUser.userAvatar
                  "
                />
                {{ loginUserStore.loginUser.userName ?? '墨刻用户' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="goToProfile">
                    <UserOutlined />
                    个人中心
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined, UserOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()

// 跳转到个人中心
const goToProfile = () => {
  router.push('/user/profile')
}
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: var(--bg-primary);
  padding: 0 32px;
  box-shadow: var(--shadow-light);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(8px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 0;
}

.header-left:hover {
  transform: scale(1.02);
}

.logo {
  height: 40px;
  width: 40px;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-light);
  transition: all 0.3s;
}

.logo:hover {
  box-shadow: var(--shadow-medium);
  transform: rotate(5deg);
}

.site-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary-color) 0%, #722ed1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

.ant-menu-horizontal {
  border-bottom: none !important;
  background: transparent;
  line-height: 64px;
}

.ant-menu-horizontal .ant-menu-item {
  border-radius: var(--border-radius-md);
  margin: 0 4px;
  transition: all 0.3s;
  font-weight: 500;
}

.ant-menu-horizontal .ant-menu-item:hover {
  background: var(--primary-light);
  color: var(--primary-color);
  transform: translateY(-1px);
}

.ant-menu-horizontal .ant-menu-item-selected {
  background: var(--primary-light);
  color: var(--primary-color);
  border-bottom: 2px solid var(--primary-color);
}

.user-login-status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ant-avatar {
  border: 2px solid var(--border-light);
  transition: all 0.3s;
}

.ant-avatar:hover {
  border-color: var(--primary-color);
  transform: scale(1.1);
}

.ant-dropdown-trigger {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--border-radius-md);
  transition: all 0.3s;
  color: var(--text-primary);
  font-weight: 500;
}

.ant-dropdown-trigger:hover {
  background: var(--bg-tertiary);
  color: var(--primary-color);
}

.ant-btn-primary {
  height: 36px;
  padding: 0 20px;
  font-weight: 500;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.ant-btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
}
</style>
