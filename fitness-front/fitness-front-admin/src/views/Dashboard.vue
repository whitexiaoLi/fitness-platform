<template>
  <div class="dashboard">
    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card" v-loading="loading">
          <div class="stat-body">
            <div class="stat-left">
              <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="28"><component :is="iconMap[card.icon]" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Pending alert -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待处理事项</span>
            </div>
          </template>
          <div class="pending-item" @click="$router.push('/courses')">
            <el-icon color="#e6a23c" :size="20"><WarningFilled /></el-icon>
            <span>待审批课程</span>
            <el-tag type="warning" round>{{ pendingCourses }}</el-tag>
          </div>
          <div v-if="pendingCourses === 0" class="empty-hint">暂无待处理事项</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Recent Users -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近注册用户</span>
              <el-button text type="primary" @click="$router.push('/users')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentUsers" stripe v-loading="usersLoading" size="small">
            <el-table-column label="用户" min-width="200">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="32" icon="UserFilled" />
                  <div class="user-info">
                    <span class="user-name">{{ row.nickname }}</span>
                    <span class="user-account">@{{ row.username }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="roleTag(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import {
  WarningFilled, User, Reading, TrendCharts, Warning,
} from '@element-plus/icons-vue'

const loading = ref(false)
const usersLoading = ref(false)
const pendingCourses = ref(0)
const recentUsers = ref([])

const iconMap = { User, Reading, TrendCharts, Warning }

const statCards = ref([
  { label: '总用户数', value: 0, icon: 'User', color: '#409eff', bg: '#ecf5ff' },
  { label: '总课程数', value: 0, icon: 'Reading', color: '#67c23a', bg: '#f0f9eb' },
  { label: '今日活跃', value: 0, icon: 'TrendCharts', color: '#e6a23c', bg: '#fdf6ec' },
  { label: '待审批课程', value: 0, icon: 'Warning', color: '#f56c6c', bg: '#fef0f0' },
])

const roleTag = (role) => ({ ADMIN: 'danger', COACH: 'warning', USER: 'info' }[role] || 'info')
const roleLabel = (role) => ({ ADMIN: '管理员', COACH: '教练', USER: '用户' }[role] || role)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/dashboard')
    if (res.code === 200) {
      const d = res.data
      statCards.value[0].value = d.totalUsers || 0
      statCards.value[1].value = d.totalCourses || 0
      statCards.value[2].value = d.todayActiveUsers || 0
    }
  } catch (e) { /* ignore */ }
  loading.value = false

  // Pending courses count
  try {
    const cr = await request.get('/courses', { params: { page: 1, size: 1 } })
    if (cr.code === 200) {
      pendingCourses.value = cr.data.total || 0
      statCards.value[3].value = pendingCourses.value
    }
  } catch (e) { /* ignore */ }

  // Recent users
  usersLoading.value = true
  try {
    const ur = await request.get('/users', { params: { page: 1, size: 5 } })
    if (ur.code === 200) {
      recentUsers.value = ur.data.records || []
    }
  } catch (e) { /* ignore */ }
  usersLoading.value = false
})
</script>

<style scoped>
.stat-row { margin-bottom: 0; }
.stat-card { cursor: default; }
.stat-card :deep(.el-card__body) { padding: 20px; }
.stat-body { display: flex; align-items: center; justify-content: space-between; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 14px; color: #999; margin-top: 6px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}

.card-header { display: flex; align-items: center; justify-content: space-between; }

.pending-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px; border-radius: 8px; background: #fdf6ec;
  cursor: pointer; transition: background 0.2s;
}
.pending-item:hover { background: #fae6c2; }
.pending-item span { flex: 1; font-size: 14px; }

.empty-hint { text-align: center; color: #c0c4cc; padding: 12px; font-size: 14px; }

.user-cell { display: flex; align-items: center; gap: 10px; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: 14px; }
.user-account { font-size: 12px; color: #999; }
</style>
