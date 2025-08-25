<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <!--        <h1 class="hero-title">墨刻 AI 应用生成平台</h1>-->
        <h1 class="hero-title">一句话 呈你所想</h1>
        <p class="hero-description">与AI对话创建应用和网站</p>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="input-actions">
          <a-button type="primary" size="large" @click="createApp" :loading="creating">
            <template #icon>
              <span>↑</span>
            </template>
          </a-button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
          >企业官网</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
            )
          "
          >在线商城</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
          >作品展示网站</a-button
        >
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd 0%, #f5f5f5 50%, #eceff1 100%);
  position: relative;
  overflow: hidden;
}

/* 简洁的背景装饰 - 参考妙搭风格 */
#homePage::before {
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

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* 英雄区域 - 妙搭风格 */
.hero-section {
  text-align: center;
  padding: 80px 0 60px;
  margin-bottom: 48px;
  position: relative;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 24px;
  line-height: 1.2;
  color: var(--text-primary);
  letter-spacing: -0.5px;
  position: relative;
}

.hero-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color), #722ed1);
  border-radius: 2px;
}

.hero-description {
  font-size: 18px;
  margin: 0;
  color: var(--text-secondary);
  font-weight: 400;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 输入区域 - 妙搭风格 */
.input-section {
  position: relative;
  margin: 0 auto 32px;
  max-width: 800px;
  background: var(--bg-primary);
  border-radius: var(--border-radius-xl);
  padding: 8px;
  box-shadow: var(--shadow-medium);
  border: 1px solid var(--border-light);
}

.prompt-input {
  border: none !important;
  box-shadow: none !important;
  background: transparent;
  font-size: 16px;
  padding: 20px 60px 20px 20px;
  border-radius: var(--border-radius-lg);
  resize: none;
  line-height: 1.5;
}

.prompt-input:focus {
  background: transparent;
  box-shadow: none !important;
  border: none !important;
}

.prompt-input::placeholder {
  color: var(--text-quaternary);
  font-weight: 400;
}

.input-actions {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.input-actions .ant-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  box-shadow: var(--shadow-light);
}

.input-actions .ant-btn:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-medium);
}

/* 快捷按钮 - 妙搭风格 */
.quick-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 80px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: var(--border-radius-lg);
  padding: 12px 24px;
  height: auto;
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: var(--shadow-light);
  font-size: 14px;
}

.quick-actions .ant-btn:hover {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

/* 区域样式 */
.section {
  margin-bottom: 80px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 32px;
  color: var(--text-primary);
  position: relative;
  padding-left: 16px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(135deg, var(--primary-color), #722ed1);
  border-radius: 2px;
}

/* 网格布局 */
.app-grid,
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.pagination-wrapper :deep(.ant-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrapper :deep(.ant-pagination-item) {
  border-radius: var(--border-radius-md);
  border-color: var(--border-light);
  transition: all 0.3s;
}

.pagination-wrapper :deep(.ant-pagination-item:hover) {
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.pagination-wrapper :deep(.ant-pagination-item-active) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.pagination-wrapper :deep(.ant-pagination-prev),
.pagination-wrapper :deep(.ant-pagination-next) {
  border-radius: var(--border-radius-md);
  border-color: var(--border-light);
  transition: all 0.3s;
}

.pagination-wrapper :deep(.ant-pagination-prev:hover),
.pagination-wrapper :deep(.ant-pagination-next:hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .container {
    padding: 24px 20px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 60px 0 40px;
    margin-bottom: 32px;
  }

  .hero-title {
    font-size: 36px;
    margin-bottom: 16px;
  }

  .hero-description {
    font-size: 16px;
  }

  .input-section {
    margin-bottom: 24px;
  }

  .prompt-input {
    padding: 16px 50px 16px 16px;
    font-size: 15px;
  }

  .input-actions {
    bottom: 12px;
    right: 12px;
  }

  .input-actions .ant-btn {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .quick-actions {
    gap: 12px;
    margin-bottom: 60px;
  }

  .quick-actions .ant-btn {
    padding: 10px 20px;
    font-size: 13px;
  }

  .section {
    margin-bottom: 60px;
  }

  .section-title {
    font-size: 24px;
    margin-bottom: 24px;
    padding-left: 12px;
  }

  .section-title::before {
    width: 3px;
    height: 20px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    margin-bottom: 32px;
  }

  .container {
    padding: 20px 16px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-description {
    font-size: 15px;
  }

  .section-title {
    font-size: 22px;
  }

  .quick-actions .ant-btn {
    padding: 8px 16px;
    font-size: 12px;
  }
}
</style>
