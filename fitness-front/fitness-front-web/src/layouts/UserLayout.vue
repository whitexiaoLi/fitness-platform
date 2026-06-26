<template>
  <el-container class="user-layout">
    <el-header class="nav">
      <span class="logo" @click="$router.push('/')">🏋️ Fitness</span>
      <el-menu mode="horizontal" :router="true" :default-active="activeMenu" class="menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/courses">课程</el-menu-item>
        <el-menu-item index="/training">训练</el-menu-item>
        <el-menu-item index="/diet">饮食</el-menu-item>
        <el-menu-item index="/metrics">身体数据</el-menu-item>
        <el-menu-item index="/ai">AI 教练</el-menu-item>
      </el-menu>
      <div class="user-area">
        <el-dropdown trigger="click">
          <span class="user-name">
            <el-avatar :size="28" icon="UserFilled" class="avatar" />
            {{ userStore.user?.nickname }}
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
    </el-header>
    <el-main class="layout-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/courses')) return '/courses'
  if (p.startsWith('/coach')) return '/'
  return p
})

function handleLogout() {
  userStore.logout()
}
</script>

<style scoped>
.user-layout { min-height: 100vh; background: #f5f7fa; }

.nav {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  height: 56px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.logo {
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-right: 24px;
  white-space: nowrap;
}
.menu { flex: 1; border-bottom: none !important; }
.menu :deep(.el-menu-item) { font-size: 14px; }

.user-area { cursor: pointer; }
.user-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333;
}
.avatar { margin-right: 2px; }

.layout-main {
  padding: 24px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  min-height: calc(100vh - 56px);
}
</style>
