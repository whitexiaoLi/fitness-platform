<template>
  <div class="user-layout">
    <!-- Main nav (minimal white) -->
    <header class="main-nav" :class="{ 'nav-scrolled': scrolled }">
      <div class="nav-inner">
        <span class="nav-logo" @click="$router.push('/')">
          FITNESS
        </span>

        <nav class="nav-menu">
          <router-link to="/" class="nav-link" :class="{ active: isActive('/') }">首页</router-link>
          <router-link to="/courses" class="nav-link" :class="{ active: isActive('/courses') }">课程</router-link>
          <router-link to="/training" class="nav-link" :class="{ active: isActive('/training') }">训练</router-link>
          <router-link to="/diet" class="nav-link" :class="{ active: isActive('/diet') }">饮食</router-link>
          <router-link to="/metrics" class="nav-link" :class="{ active: isActive('/metrics') }">身体</router-link>
        </nav>

        <div class="nav-user">
          <el-dropdown trigger="click" popper-class="user-dropdown">
            <span class="user-trigger">
              <el-avatar :size="30" :src="userStore.user?.avatarUrl" icon="UserFilled" class="user-avatar" />
              <span class="user-name">{{ userStore.user?.nickname }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">
                  <el-icon><User /></el-icon> 个人资料
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isCoach()" @click="$router.push('/coach')">
                  <el-icon><DataAnalysis /></el-icon> 教练面板
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- Main content -->
    <main class="layout-main">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-col">
          <h4 class="footer-title">FITNESS</h4>
          <p class="footer-desc">专业健身训练平台，科学管理你的每一次训练与每一餐。</p>
        </div>
        <div class="footer-col">
          <h4 class="footer-title">功能</h4>
          <router-link to="/courses" class="footer-link">课程中心</router-link>
          <router-link to="/training" class="footer-link">训练记录</router-link>
          <router-link to="/diet" class="footer-link">饮食管理</router-link>
          <router-link to="/metrics" class="footer-link">身体数据</router-link>
        </div>
        <div class="footer-col">
          <h4 class="footer-title">关于</h4>
          <a href="#" class="footer-link">关于我们</a>
          <a href="#" class="footer-link">联系方式</a>
          <a href="#" class="footer-link">隐私政策</a>
        </div>
        <div class="footer-col">
          <h4 class="footer-title">关注</h4>
          <div class="footer-social">
            <span class="social-icon">📱</span>
            <span class="social-icon">💬</span>
            <span class="social-icon">📧</span>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 FITNESS. All rights reserved.</span>
      </div>
    </footer>

    <!-- Back to top -->
    <button class="back-to-top" :class="{ visible: showBackTop }" @click="scrollToTop">
      <el-icon><ArrowUp /></el-icon>
    </button>

    <!-- Mobile bottom TabBar -->
    <nav class="mobile-tabbar" v-if="isMobile">
      <router-link to="/" class="tabbar-item" :class="{ active: isActive('/') }">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </router-link>
      <router-link to="/courses" class="tabbar-item" :class="{ active: isActive('/courses') }">
        <el-icon><Reading /></el-icon>
        <span>课程</span>
      </router-link>
      <router-link to="/diet" class="tabbar-item" :class="{ active: isActive('/diet') }">
        <el-icon><DishDot /></el-icon>
        <span>饮食</span>
      </router-link>
      <router-link to="/training" class="tabbar-item" :class="{ active: isActive('/training') }">
        <el-icon><TrendCharts /></el-icon>
        <span>训练</span>
      </router-link>
      <router-link to="/profile" class="tabbar-item" :class="{ active: isActive('/profile') }">
        <el-icon><UserFilled /></el-icon>
        <span>我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown, ArrowUp, User, DataAnalysis, SwitchButton, HomeFilled, Reading, DishDot, TrendCharts, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

const scrolled = ref(false)
const showBackTop = ref(false)
const isMobile = ref(false)

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function handleLogout() {
  userStore.logout()
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onScroll() {
  scrolled.value = window.scrollY > 50
  showBackTop.value = window.scrollY > 400
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', checkMobile)
  checkMobile()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: var(--color-bg-page);
  display: flex;
  flex-direction: column;
}

/* ---- Main Nav (black transparent) ---- */
.main-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  transition: background var(--transition-normal), box-shadow var(--transition-normal);
}
.nav-scrolled {
  background: rgba(0, 0, 0, 0.88);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.3);
}
.nav-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--page-padding-x);
  height: 100%;
  display: flex;
  align-items: center;
}

/* Logo */
.nav-logo {
  cursor: pointer;
  font-size: 18px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 2px;
  flex-shrink: 0;
  margin-right: var(--space-xl);
}

/* Nav links (centered) */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}
.nav-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding: 8px 20px;
  font-size: var(--font-body);
  font-weight: var(--font-weight-medium);
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  transition: color var(--transition-fast);
  white-space: nowrap;
}
.nav-link::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--color-brand);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}
.nav-link:hover { color: #FFFFFF; }
.nav-link:hover::after { width: calc(100% - 40px); }
.nav-link.active { color: var(--color-brand); }
.nav-link.active::after { width: calc(100% - 40px); }

/* User area */
.nav-user { flex-shrink: 0; margin-left: var(--space-xl); }
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.75);
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: color var(--transition-fast);
}
.user-trigger:hover { color: #FFFFFF; }
.user-avatar { flex-shrink: 0; }
.user-name { font-size: var(--font-body); max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-arrow { font-size: 12px; transition: transform var(--transition-fast); }

/* ---- Main Content ---- */
.layout-main {
  flex: 1;
  max-width: var(--max-width);
  width: 100%;
  margin: 0 auto;
  padding: var(--space-lg) var(--page-padding-x) var(--space-2xl);
}

/* ---- Footer ---- */
.site-footer {
  background: var(--color-bg-dark);
  color: var(--color-text-on-dark-secondary);
  padding-top: var(--space-2xl);
  margin-top: auto;
}
.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--page-padding-x);
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: var(--space-xl);
  padding-bottom: var(--space-xl);
}
.footer-title {
  font-size: 15px;
  font-weight: var(--font-weight-semibold);
  color: #FFFFFF;
  margin: 0 0 var(--space-md);
}
.footer-desc {
  font-size: 13px;
  line-height: 1.8;
  max-width: 260px;
}
.footer-link {
  display: block;
  font-size: 13px;
  color: var(--color-text-on-dark-secondary);
  text-decoration: none;
  padding: 4px 0;
  transition: color var(--transition-fast);
}
.footer-link:hover { color: var(--color-brand); }
.footer-social { display: flex; gap: var(--space-sm); }
.social-icon { font-size: 20px; cursor: pointer; transition: opacity var(--transition-fast); }
.social-icon:hover { opacity: 0.8; }
.footer-bottom {
  border-top: 1px solid rgba(255,255,255,0.08);
  text-align: center;
  padding: var(--space-md);
  font-size: 12px;
  color: rgba(255,255,255,0.4);
}

/* ---- Back to top ---- */
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 40px;
  height: 40px;
  background: var(--color-brand);
  color: #FFFFFF;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 12px rgba(217, 56, 49, 0.35);
  transition: all var(--transition-normal);
  z-index: 1000;
  opacity: 0;
  transform: translateY(16px);
  pointer-events: none;
  padding: 0;
}
.back-to-top.visible {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}
.back-to-top:hover {
  background: var(--color-brand-hover);
  transform: translateY(-2px);
}

/* ---- Mobile TabBar ---- */
.mobile-tabbar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(--tabbar-height);
  background: #FFFFFF;
  border-top: 1px solid var(--color-border);
  z-index: 100;
  justify-content: space-around;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
}
.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: var(--color-text-weak);
  text-decoration: none;
  padding: 4px 0;
  transition: color var(--transition-fast);
}
.tabbar-item .el-icon { font-size: 20px; }
.tabbar-item.active { color: var(--color-brand); }

/* ---- Responsive ---- */
@media (max-width: 991px) {
  .nav-link { padding: 8px 12px; font-size: 13px; }
  .footer-inner { grid-template-columns: 1fr 1fr; gap: var(--space-lg); }
}

@media (max-width: 767px) {
  .nav-menu { display: none; }
  .nav-inner { justify-content: space-between; }
  .layout-main { padding-bottom: calc(var(--space-lg) + var(--tabbar-height)); }
  .mobile-tabbar { display: flex; }
  .site-footer { display: none; }
  .back-to-top { bottom: calc(var(--tabbar-height) + 16px); }
}
</style>
