<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <!-- 友情链接 -->
      <div class="friend-links">
        <h3 class="links-title">友情链接</h3>
        <div class="links-grid">
          <a href="https://www.longwingstech.cn" target="_blank" class="link-item">
            <span class="link-icon">🏥</span>
            <span class="link-text">医疗智能体平台</span>
          </a>
          <a href="https://bi.longwingstech.cn" target="_blank" class="link-item">
            <span class="link-icon">📊</span>
            <span class="link-text">智能BI平台</span>
          </a>
          <a href="https://vue.js.org" target="_blank" class="link-item">
            <span class="link-icon">💚</span>
            <span class="link-text">Vue.js</span>
          </a>
          <a href="https://ant.design" target="_blank" class="link-item">
            <span class="link-icon">🐜</span>
            <span class="link-text">Ant Design</span>
          </a>
          <a href="https://nodejs.org" target="_blank" class="link-item">
            <span class="link-icon">🟢</span>
            <span class="link-text">Node.js</span>
          </a>
          <a href="https://spring.io" target="_blank" class="link-item">
            <span class="link-icon">🍃</span>
            <span class="link-text">Spring Boot</span>
          </a>
        </div>
      </div>
      
      <!-- 版权信息 -->
      <p class="copyright">© 2025 墨刻 by 君墨</p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: var(--bg-primary);
  backdrop-filter: blur(12px);
  text-align: center;
  padding: 48px 20px 32px;
  margin-top: 60px;
  border-top: 1px solid var(--border-light);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 友情链接样式 */
.friend-links {
  margin-bottom: 40px;
}

.links-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 24px;
  position: relative;
}

.links-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-color), #722ed1);
  border-radius: 1px;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-lg);
  text-decoration: none;
  transition: all 0.3s;
  box-shadow: var(--shadow-light);
}

.link-item:hover {
  background: var(--bg-primary);
  border-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.link-icon {
  font-size: 20px;
  display: block;
}

.link-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: color 0.3s;
}

.link-item:hover .link-text {
  color: var(--primary-color);
}

.copyright {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 14px;
  font-weight: 400;
  letter-spacing: 0.5px;
  position: relative;
}

.copyright::before {
  content: '';
  position: absolute;
  top: -16px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--primary-color), transparent);
  border-radius: 1px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer {
    padding: 32px 16px 24px;
  }
  
  .links-grid {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
    gap: 12px;
  }
  
  .link-item {
    padding: 12px 8px;
  }
  
  .link-icon {
    font-size: 18px;
  }
  
  .link-text {
    font-size: 12px;
  }
  
  .links-title {
    font-size: 16px;
    margin-bottom: 20px;
  }
  
  .friend-links {
    margin-bottom: 32px;
  }
}

@media (max-width: 480px) {
  .links-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }
  
  .link-item {
    padding: 10px 6px;
  }
  
  .link-icon {
    font-size: 16px;
  }
  
  .link-text {
    font-size: 11px;
  }
}
</style>
