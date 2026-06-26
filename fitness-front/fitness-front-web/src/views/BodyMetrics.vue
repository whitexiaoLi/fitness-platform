<template>
  <div class="body-metrics">
    <h2 class="page-title">📊 身体数据</h2>

    <el-row :gutter="16" class="stats-row" v-if="latest">
      <el-col :span="6" v-for="s in summaryCards" :key="s.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
          <div class="stat-unit">{{ s.unit }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header><span>{{ latest ? '更新数据' : '首次录入' }}</span></template>
      <el-form :inline="true" :model="form" size="default">
        <el-form-item label="日期">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :min="30" :max="300" :precision="1" :step="0.5" style="width: 130px" />
        </el-form-item>
        <el-form-item label="体脂(%)">
          <el-input-number v-model="form.bodyFat" :min="1" :max="60" :precision="1" :step="0.5" style="width: 130px" />
        </el-form-item>
        <el-form-item label="腰围(cm)">
          <el-input-number v-model="form.waist" :min="30" :max="200" :precision="1" style="width: 130px" />
        </el-form-item>
        <el-form-item label="臀围(cm)">
          <el-input-number v-model="form.hip" :min="30" :max="200" :precision="1" style="width: 130px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="table-header">
          <span>历史记录</span>
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" size="small" style="width: 240px" @change="fetchData" unlink-panels />
        </div>
      </template>
      <el-table :data="records" stripe v-loading="loading" size="default">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column label="日期" width="120"><template #default="{ row }">{{ row.recordDate }}</template></el-table-column>
        <el-table-column label="体重(kg)" width="100"><template #default="{ row }">{{ row.weight ?? '-' }}</template></el-table-column>
        <el-table-column label="BMI" width="80"><template #default="{ row }">{{ row.bmi ?? '-' }}</template></el-table-column>
        <el-table-column label="体脂(%)" width="90"><template #default="{ row }">{{ row.bodyFat ?? '-' }}</template></el-table-column>
        <el-table-column label="腰围(cm)" width="90"><template #default="{ row }">{{ row.waist ?? '-' }}</template></el-table-column>
        <el-table-column label="臀围(cm)" width="90"><template #default="{ row }">{{ row.hip ?? '-' }}</template></el-table-column>
        <el-table-column label="记录时间" width="170"><template #default="{ row }">{{ row.createTime?.replace('T', ' ') }}</template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const latest = ref(null)
const records = ref([])
const loading = ref(false)
const saving = ref(false)
const dateRange = ref([])

const form = reactive({ recordDate: new Date().toISOString().slice(0, 10), weight: null, bodyFat: null, waist: null, hip: null })

const summaryCards = computed(() => latest.value ? [
  { label: '体重', value: latest.value.weight ?? '-', unit: 'kg', color: '#667eea' },
  { label: 'BMI', value: latest.value.bmi ?? '-', unit: '', color: '#67c23a' },
  { label: '体脂率', value: latest.value.bodyFat ?? '-', unit: '%', color: '#e6a23c' },
  { label: '腰围', value: latest.value.waist ?? '-', unit: 'cm', color: '#f56c6c' },
] : [])

onMounted(async () => {
  try { const r = await request.get('/metrics/latest'); if (r.code === 200) latest.value = r.data } catch (e) {}
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value?.length === 2) { params.start = dateRange.value[0]; params.end = dateRange.value[1] }
    const res = await request.get('/metrics', { params })
    if (res.code === 200) records.value = res.data || []
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleSave() {
  saving.value = true
  try {
    const res = await request.post('/metrics', { ...form })
    if (res.code === 200) { ElMessage.success('已记录'); latest.value = res.data; fetchData() }
  } catch (e) { ElMessage.error('记录失败') }
  finally { saving.value = false }
}
</script>

<style scoped>
.page-title { margin: 0 0 16px; font-size: 20px; }
.stats-row { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-card :deep(.el-card__body) { padding: 20px; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }
.stat-unit { font-size: 11px; color: #999; }
.table-header { display: flex; align-items: center; justify-content: space-between; }
</style>
