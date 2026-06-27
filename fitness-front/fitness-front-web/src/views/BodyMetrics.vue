<template>
  <div class="body-metrics">
    <h2 class="page-title">身体数据</h2>

    <!-- 体重目标 -->
    <el-card shadow="hover" class="goal-card" v-if="weightGoal">
      <div class="goal-row">
        <span class="goal-text">🎯 目标体重：{{ weightGoal }} kg</span>
        <el-progress :percentage="weightGoalPercent" :color="weightGoalColor" :stroke-width="8" style="flex:1;max-width:300px" />
        <span class="goal-text">{{ latest?.weight || '—' }} / {{ weightGoal }} kg</span>
        <el-button text size="small" @click="weightGoal=null">清除</el-button>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row" :style="{ marginTop: weightGoal ? '16px' : '0' }" v-if="latest">
      <el-col :xs="12" :sm="6" v-for="s in summaryCards" :key="s.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" :style="{ color: s.color }">{{ s.display }}</div>
          <div class="stat-label">{{ s.label }}</div>
          <div class="stat-unit">{{ s.unit }}</div>
          <el-tag v-if="s.bmiTag" :type="s.bmiTagType" size="small" class="bmi-tag">{{ s.bmiTag }}</el-tag>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 -->
    <div class="trend-section" v-if="trendData.length > 0">
      <div class="trend-header">
        <span class="trend-title">趋势</span>
        <el-radio-group v-model="trendMetric" size="small" @change="updateTrend">
          <el-radio-button value="weight">体重</el-radio-button>
          <el-radio-button value="bmi">BMI</el-radio-button>
          <el-radio-button value="bodyFat">体脂</el-radio-button>
          <el-radio-button value="waist">腰围</el-radio-button>
        </el-radio-group>
      </div>
      <TrendSparkline :data="trendData" :color="trendColor" />
    </div>

    <!-- 录入表单 -->
    <el-card shadow="hover" class="form-card">
      <template #header><span>{{ latest ? '更新数据' : '首次录入' }}</span></template>
      <el-form :inline="true" :model="form" size="default">
        <el-form-item label="日期">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width:150px" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :min="30" :max="300" :precision="1" :step="0.5" style="width:130px" />
        </el-form-item>
        <el-form-item label="体脂(%)">
          <el-input-number v-model="form.bodyFat" :min="1" :max="60" :precision="1" :step="0.5" style="width:130px" />
        </el-form-item>
        <el-form-item label="腰围(cm)">
          <el-input-number v-model="form.waist" :min="30" :max="200" :precision="1" style="width:130px" />
        </el-form-item>
        <el-form-item label="臀围(cm)">
          <el-input-number v-model="form.hip" :min="30" :max="200" :precision="1" style="width:130px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">记录</el-button>
        </el-form-item>
      </el-form>
      <div class="form-hint" v-if="!hasHeight">💡 前往 <router-link to="/profile">个人资料</router-link> 设置身高，即可自动计算 BMI</div>
    </el-card>

    <!-- 体重目标设置 -->
    <el-card shadow="hover" class="goal-set-card" v-if="!weightGoal">
      <el-form :inline="true" size="default">
        <el-form-item label="设定目标体重(kg)">
          <el-input-number v-model="goalInput" :min="30" :max="300" :precision="1" :step="0.5" style="width:130px" />
        </el-form-item>
        <el-form-item><el-button size="small" @click="setWeightGoal" :disabled="!goalInput">保存目标</el-button></el-form-item>
      </el-form>
    </el-card>

    <!-- 历史记录 -->
    <el-card shadow="hover" class="history-card">
      <template #header>
        <div class="table-header">
          <span>历史记录</span>
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" size="small" style="width:240px" @change="fetchData" unlink-panels />
        </div>
      </template>
      <el-table :data="pagedRecords" stripe v-loading="loading" size="default">
        <template #empty><el-empty description="暂无数据" :image-size="80" /></template>
        <el-table-column label="日期" width="110"><template #default="{ row }">{{ row.recordDate }}</template></el-table-column>
        <el-table-column label="体重" width="90"><template #default="{ row }">{{ row.weight ?? '—' }}</template></el-table-column>
        <el-table-column label="BMI" width="80"><template #default="{ row }"><span :class="bmiClass(row.bmi)">{{ row.bmi ?? '—' }}</span></template></el-table-column>
        <el-table-column label="体脂" width="80"><template #default="{ row }">{{ row.bodyFat ?? '—' }}%</template></el-table-column>
        <el-table-column label="腰围" width="80"><template #default="{ row }">{{ row.waist ?? '—' }}</template></el-table-column>
        <el-table-column label="臀围" width="80"><template #default="{ row }">{{ row.hip ?? '—' }}</template></el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
                <template #reference><el-button text type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" :page-size="pageSize" :page-sizes="[10,20,50]" :total="records.length"
          layout="total, sizes, prev, pager, next" @size-change="page=1" />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editVisible" title="编辑身体数据" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="日期"><el-date-picker v-model="editForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="editForm.weight" :min="30" :max="300" :precision="1" :step="0.5" style="width:100%" /></el-form-item>
        <el-form-item label="体脂(%)"><el-input-number v-model="editForm.bodyFat" :min="1" :max="60" :precision="1" :step="0.5" style="width:100%" /></el-form-item>
        <el-form-item label="腰围(cm)"><el-input-number v-model="editForm.waist" :min="30" :max="200" :precision="1" style="width:100%" /></el-form-item>
        <el-form-item label="臀围(cm)"><el-input-number v-model="editForm.hip" :min="30" :max="200" :precision="1" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" @click="handleEdit" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { addBodyMetrics, getBodyMetrics, getLatestMetrics, updateBodyMetrics, deleteBodyMetrics } from '@/api/metrics'
import TrendSparkline from '@/components/TrendSparkline.vue'

const latest = ref(null)
const records = ref([])
const loading = ref(false)
const saving = ref(false)
const dateRange = ref([])
const page = ref(1)
const pageSize = ref(10)
const hasHeight = ref(false)

const form = reactive({ recordDate: new Date().toISOString().slice(0, 10), weight: null, bodyFat: null, waist: null, hip: null })

// Weight goal (localStorage)
const weightGoal = ref(null)
const goalInput = ref(null)
try { weightGoal.value = JSON.parse(localStorage.getItem('body_weight_goal')) } catch {}
const weightGoalPercent = computed(() => {
  if (!weightGoal.value || !latest.value?.weight) return 0
  return Math.min(100, Math.round(Number(latest.value.weight) / weightGoal.value * 100))
})
const weightGoalColor = computed(() => weightGoalPercent.value > 100 ? '#f56c6c' : '#67c23a')
function setWeightGoal() {
  weightGoal.value = goalInput.value
  localStorage.setItem('body_weight_goal', JSON.stringify(weightGoal.value))
  ElMessage.success('目标已保存')
}

// Stat cards with animated display
const statDisplays = reactive({ weight: '0', bmi: '0', bodyFat: '0', waist: '0', hip: '0' })
const summaryCards = computed(() => latest.value ? [
  { label: '体重', value: latest.value.weight, display: statDisplays.weight, unit: 'kg', color: '#667eea' },
  { label: 'BMI', value: latest.value.bmi, display: statDisplays.bmi, unit: '', color: '#67c23a', bmiTag: bmiCategory(latest.value.bmi), bmiTagType: bmiTagType(latest.value.bmi) },
  { label: '体脂率', value: latest.value.bodyFat, display: statDisplays.bodyFat, unit: '%', color: '#e6a23c' },
  { label: '腰围', value: latest.value.waist, display: statDisplays.waist, unit: 'cm', color: '#f56c6c' },
  { label: '臀围', value: latest.value.hip, display: statDisplays.hip, unit: 'cm', color: '#9b59b6' },
] : [])

function bmiCategory(bmi) {
  if (bmi == null) return ''
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '标准'
  if (bmi < 28) return '偏重'
  return '肥胖'
}
function bmiTagType(bmi) {
  if (bmi == null) return 'info'
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}
function bmiClass(bmi) {
  if (bmi == null) return ''
  if (bmi < 18.5) return 'bmi-under'
  if (bmi < 24) return 'bmi-normal'
  if (bmi < 28) return 'bmi-over'
  return 'bmi-obese'
}

function animateStats() {
  const keys = ['weight', 'bmi', 'bodyFat', 'waist', 'hip']
  keys.forEach((k, i) => { const t = Number(summaryCards.value[i]?.value) || 0, d = performance.now(); function s(n) { const p = Math.min(1, (n - d) / 500), e = 1 - (1 - p) * (1 - p); statDisplays[k] = k === 'weight' || k === 'bmi' ? (Math.round(t * 10 * e) / 10).toFixed(1) : String(Math.round(t * e)); if (p < 1) requestAnimationFrame(s); else { statDisplays[k] = k === 'weight' || k === 'bmi' ? (Math.round(t * 10) / 10).toFixed(1) : String(Math.round(t)) } } requestAnimationFrame(s) })
}

// Trend
const trendMetric = ref('weight')
const trendColor = ref('#667eea')
const trendColors = { weight: '#667eea', bmi: '#67c23a', bodyFat: '#e6a23c', waist: '#f56c6c' }
const trendData = computed(() => {
  const key = trendMetric.value
  return [...records.value]
    .filter(r => r[key] != null)
    .sort((a, b) => a.recordDate?.localeCompare(b.recordDate))
    .map(r => ({ date: r.recordDate, value: r[key] }))
})
function updateTrend() { trendColor.value = trendColors[trendMetric.value] || '#667eea' }

// Client-side pagination
const pagedRecords = computed(() => {
  const s = (page.value - 1) * pageSize.value
  return records.value.slice(s, s + pageSize.value)
})

// Edit
const editVisible = ref(false), editingId = ref(null)
const editForm = reactive({ recordDate: '', weight: null, bodyFat: null, waist: null, hip: null })

onMounted(async () => {
  try { const r = await getLatestMetrics(); if (r.code === 200) latest.value = r.data } catch {}
  fetchData()
  try { const u = JSON.parse(localStorage.getItem('user') || '{}'); hasHeight.value = !!u.height } catch {}
})

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value?.length === 2) { params.start = dateRange.value[0]; params.end = dateRange.value[1] }
    const res = await getBodyMetrics(params)
    if (res.code === 200) { records.value = res.data || []; page.value = 1; await nextTick(); animateStats() }
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleSave() {
  saving.value = true
  try {
    const res = await addBodyMetrics({ ...form })
    if (res.code === 200) { ElMessage.success('已记录'); latest.value = res.data; fetchData() }
  } catch { ElMessage.error('记录失败') }
  finally { saving.value = false }
}

function openEdit(row) {
  editingId.value = row.id
  editForm.recordDate = row.recordDate; editForm.weight = row.weight; editForm.bodyFat = row.bodyFat; editForm.waist = row.waist; editForm.hip = row.hip
  editVisible.value = true
}

async function handleEdit() {
  saving.value = true
  try {
    const res = await updateBodyMetrics(editingId.value, { ...editForm })
    if (res.code === 200) { ElMessage.success('已更新'); editVisible.value = false; fetchData(); try { const l = await getLatestMetrics(); if (l.code === 200) latest.value = l.data } catch {} }
  } catch { ElMessage.error('更新失败') }
  finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    const res = await deleteBodyMetrics(id)
    if (res.code === 200) { ElMessage.success('已删除'); fetchData(); try { const l = await getLatestMetrics(); if (l.code === 200) latest.value = l.data } catch {} }
  } catch { ElMessage.error('删除失败') }
}
</script>

<style scoped>
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-card :deep(.el-card__body) { padding: 20px; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 13px; color: var(--color-text-secondary); margin-top: 4px; }
.stat-unit { font-size: 11px; color: var(--color-text-weak); }
.bmi-tag { margin-top: 6px; }

/* Goal */
.goal-card :deep(.el-card__body) { padding: 12px 20px; }
.goal-row { display: flex; align-items: center; gap: 12px; }
.goal-text { font-size: 13px; color: var(--color-text-secondary); white-space: nowrap; }
.goal-set-card { margin-top: 16px; }
.goal-set-card :deep(.el-card__body) { padding: 12px 20px; }

/* Trend */
.trend-section { margin-top: 16px; }
.trend-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.trend-title { font-weight: 600; font-size: 14px; }

/* Form */
.form-card { margin-top: 16px; }
.form-hint { margin-top: 8px; font-size: 12px; color: var(--color-text-weak); }
.history-card { margin-top: 16px; }

.table-header { display: flex; align-items: center; justify-content: space-between; }

/* BMI colors */
.bmi-under { color: #e6a23c; font-weight: 600; }
.bmi-normal { color: #67c23a; font-weight: 600; }
.bmi-over { color: #e6a23c; font-weight: 600; }
.bmi-obese { color: #f56c6c; font-weight: 600; }

.action-cell { display: flex; align-items: center; gap: 0; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }

@media (max-width: 767px) {
  .stat-value { font-size: 22px; }
}
</style>
