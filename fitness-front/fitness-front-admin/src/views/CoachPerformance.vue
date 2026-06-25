<template>
  <div class="coach-performance">
    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
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

    <!-- Performance Table -->
    <el-card style="margin-top: 20px">
      <template #header><h3>教练业绩明细</h3></template>
      <el-table :data="data" stripe v-loading="loading" size="default">
        <el-table-column prop="coachId" label="教练ID" width="80" />
        <el-table-column prop="coachName" label="教练姓名" min-width="120" />
        <el-table-column label="学员数" width="100" sortable prop="studentCount">
          <template #default="{ row }">{{ row.studentCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="课程数" width="100" sortable prop="courseCount">
          <template #default="{ row }">{{ row.courseCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="总时长(min)" width="130" sortable prop="totalDuration">
          <template #default="{ row }">{{ row.totalDuration || 0 }}</template>
        </el-table-column>
        <el-table-column label="总收入" width="120" sortable prop="totalRevenue">
          <template #default="{ row }">¥{{ row.totalRevenue || 0 }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, Reading, TrendCharts, Money } from '@element-plus/icons-vue'
import request from '@/utils/request'

const data = ref([])
const loading = ref(false)
const iconMap = { User, Reading, TrendCharts, Money }

const stats = computed(() => ({
  totalCoaches: data.value.length,
  totalStudents: data.value.reduce((s, c) => s + (c.studentCount || 0), 0),
  totalCourses: data.value.reduce((s, c) => s + (c.courseCount || 0), 0),
  totalRevenue: data.value.reduce((s, c) => s + (c.totalRevenue || 0), 0),
}))

const statCards = computed(() => [
  { label: '教练总数', value: stats.value.totalCoaches, icon: 'User', color: '#409eff', bg: '#ecf5ff' },
  { label: '总学员数', value: stats.value.totalStudents, icon: 'Reading', color: '#67c23a', bg: '#f0f9eb' },
  { label: '总课程数', value: stats.value.totalCourses, icon: 'TrendCharts', color: '#e6a23c', bg: '#fdf6ec' },
  { label: '总收入(¥)', value: stats.value.totalRevenue, icon: 'Money', color: '#f56c6c', bg: '#fef0f0' },
])

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/coach-performance')
    if (res.code === 200) {
      data.value = res.data || []
    }
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-row { margin-bottom: 0; }
.stat-card :deep(.el-card__body) { padding: 20px; }
.stat-body { display: flex; align-items: center; justify-content: space-between; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 14px; color: #999; margin-top: 6px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
</style>
