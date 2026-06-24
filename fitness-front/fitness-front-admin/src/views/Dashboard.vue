<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <h2 class="logo">📊 Fitness Admin</h2>
      <el-menu :router="true" default-active="/">
        <el-menu-item index="/"><el-icon><DataAnalysis /></el-icon> 数据概览</el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon> 用户管理</el-menu-item>
        <el-menu-item index="/courses"><el-icon><Document /></el-icon> 课程审核</el-menu-item>
      </el-menu>
      <div class="logout" @click="handleLogout">退出登录</div>
    </el-aside>
    <el-main>
      <h3>数据概览</h3>
      <el-row :gutter="20">
        <el-col :span="8" v-for="stat in stats" :key="stat.label">
          <el-card shadow="hover">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAdminStore } from '@/stores/admin'
import request from '@/utils/request'

const store = useAdminStore()
const stats = ref([
  { label: '总用户数', value: '...' },
  { label: '总课程数', value: '...' },
  { label: '今日活跃', value: '...' },
])

onMounted(async () => {
  try {
    const res = await request.get('/dashboard')
    if (res.code === 200) {
      const d = res.data
      stats.value[0].value = d.totalUsers || 0
      stats.value[1].value = d.totalCourses || 0
      stats.value[2].value = d.todayActiveUsers || 0
    }
  } catch (e) { /* ignore */ }
})

function handleLogout() {
  store.logout()
}
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside { background: #1a1a2e; color: white; display: flex; flex-direction: column; }
.logo { padding: 20px; font-size: 18px; text-align: center; border-bottom: 1px solid #333; margin: 0; }
.logout { margin-top: auto; padding: 16px; text-align: center; cursor: pointer; color: #999; }
.stat-value { font-size: 32px; font-weight: bold; color: #667eea; }
.stat-label { color: #999; margin-top: 8px; }
</style>
