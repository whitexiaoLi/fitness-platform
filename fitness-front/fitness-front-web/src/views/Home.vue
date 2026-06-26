<template>
  <div class="home">
    <div class="welcome">
      <h1>👋 欢迎回来，{{ userStore.user?.nickname }}</h1>
      <p>坚持运动，遇见更好的自己</p>
    </div>
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="6" v-for="item in actions" :key="item.path">
        <el-card shadow="hover" class="action-card" @click="$router.push(item.path)">
          <div class="action-icon">{{ item.icon }}</div>
          <div class="action-label">{{ item.label }}</div>
          <div class="action-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const actions = [
  { icon: '📚', label: '课程中心', desc: '浏览健身课程', path: '/courses' },
  { icon: '🏃', label: '训练记录', desc: '记录每次锻炼', path: '/training' },
  { icon: '🍎', label: '饮食记录', desc: '追踪每日营养', path: '/diet' },
  { icon: '📊', label: '身体数据', desc: '查看身体变化', path: '/metrics' },
]
</script>

<style scoped>
.welcome { text-align: center; padding: 32px 0 24px; }
.welcome h1 { font-size: 28px; color: #333; margin: 0 0 8px; }
.welcome p { color: #999; font-size: 16px; margin: 0; }
.quick-actions { padding: 8px 0; }
.action-card {
  text-align: center;
  cursor: pointer;
  padding: 32px 20px;
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.action-card:hover { transform: translateY(-4px); }
.action-icon { font-size: 48px; margin-bottom: 12px; }
.action-label { font-size: 17px; font-weight: 600; color: #333; }
.action-desc { margin-top: 6px; font-size: 13px; color: #999; }
</style>
