<template>
  <div class="home">
    <el-container>
      <el-header class="nav">
        <span class="logo">🏋️ Fitness</span>
        <el-menu mode="horizontal" :router="true" class="menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/courses">课程</el-menu-item>
          <el-menu-item index="/training">训练</el-menu-item>
          <el-menu-item index="/diet">饮食</el-menu-item>
          <el-menu-item index="/metrics">身体数据</el-menu-item>
          <el-menu-item index="/ai">AI 教练</el-menu-item>
        </el-menu>
        <div class="user-area">
          <el-dropdown>
            <span class="user-name">{{ userStore.user?.nickname }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人资料</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isCoach()" @click="$router.push('/coach')">教练面板</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <div class="welcome">
          <h1>👋 欢迎回来，{{ userStore.user?.nickname }}</h1>
          <p>坚持运动，遇见更好的自己</p>
        </div>
        <el-row :gutter="20" class="quick-actions">
          <el-col :span="6" v-for="item in actions" :key="item.path">
            <el-card shadow="hover" class="action-card" @click="$router.push(item.path)">
              <div class="action-icon">{{ item.icon }}</div>
              <div class="action-label">{{ item.label }}</div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const actions = [
  { icon: '📚', label: '课程中心', path: '/courses' },
  { icon: '🏃', label: '训练记录', path: '/training' },
  { icon: '🍎', label: '饮食记录', path: '/diet' },
  { icon: '📊', label: '身体数据', path: '/metrics' },
]

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.nav {
  display: flex; align-items: center; border-bottom: 1px solid #eee;
  background: white; padding: 0 20px;
}
.logo { font-size: 20px; font-weight: bold; margin-right: 30px; }
.menu { flex: 1; border-bottom: none; }
.user-area { cursor: pointer; }
.welcome { text-align: center; padding: 40px 0 20px; }
.welcome h1 { font-size: 28px; color: #333; }
.quick-actions { padding: 20px 0; }
.action-card { text-align: center; cursor: pointer; padding: 20px; }
.action-icon { font-size: 40px; }
.action-label { margin-top: 10px; font-size: 16px; color: #666; }
</style>
