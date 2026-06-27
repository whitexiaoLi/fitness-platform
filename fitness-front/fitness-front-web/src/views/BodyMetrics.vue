<template>
  <div class="metrics-page">
    <!-- Hero Banner -->
    <section class="metrics-hero">
      <div class="metrics-hero-overlay" />
      <div class="metrics-hero-content">
        <h1 class="metrics-hero-title reveal-item">身体数据</h1>
        <div class="hero-body reveal-item" style="transition-delay:80ms" v-if="latest">
          <div class="hero-main-stat">
            <span class="hero-big-value">{{ statDisplays.weight }}</span>
            <span class="hero-big-unit">kg</span>
            <span class="hero-big-label">体重</span>
          </div>
          <div class="hero-divider" />
          <div class="hero-main-stat">
            <span class="hero-big-value">{{ statDisplays.bmi }}</span>
            <span class="hero-big-unit"></span>
            <span class="hero-big-label">
              BMI
              <span class="bmi-cat-tag" :class="bmiClass(latest?.bmi)">{{ bmiCategory(latest?.bmi) }}</span>
            </span>
          </div>
        </div>
        <div class="hero-goal reveal-item" style="transition-delay:120ms" v-if="weightGoal && latest">
          <div class="hero-goal-bar">
            <div class="hero-goal-fill" :style="{ width: weightGoalPercent + '%', background: weightGoalColor }" />
          </div>
          <span class="hero-goal-text">目标 {{ weightGoal }}kg · {{ weightGoalPercent }}%</span>
        </div>
        <div class="hero-empty reveal-item" style="transition-delay:80ms" v-if="!latest">
          <p style="color:rgba(255,255,255,0.6);margin:0 0 16px">还没有身体数据，立即开始记录</p>
        </div>
        <el-button v-if="!weightGoal" class="hero-cta reveal-item" style="transition-delay:160ms" size="small" @click="showGoalInput = !showGoalInput">设定目标体重</el-button>
        <div class="hero-goal-input reveal-item" style="transition-delay:160ms" v-if="showGoalInput && !weightGoal">
          <el-input-number v-model="goalInput" :min="30" :max="200" :precision="1" placeholder="目标体重 kg" size="small" style="width:140px" />
          <el-button size="small" type="primary" @click="setWeightGoal">保存</el-button>
        </div>
      </div>
    </section>

    <!-- Content -->
    <div class="metrics-container">
      <!-- Trend Chart -->
      <div class="trend-card scroll-fade" v-if="records.length > 1">
        <div class="trend-header">
          <h3 class="card-section-title" style="margin:0">趋势</h3>
          <el-radio-group v-model="trendMetric" size="small" @change="updateTrend">
            <el-radio-button value="weight">体重</el-radio-button>
            <el-radio-button value="bmi">BMI</el-radio-button>
            <el-radio-button value="bodyFat">体脂</el-radio-button>
            <el-radio-button value="waist">腰围</el-radio-button>
          </el-radio-group>
        </div>
        <TrendSparkline :data="trendData" :color="trendColor" class="trend-chart" />
      </div>

      <!-- Entry + Summary Row -->
      <div class="entry-summary-row scroll-fade" style="transition-delay:60ms">
        <!-- Data Entry -->
        <div class="entry-card">
          <h3 class="card-section-title">{{ latest ? '更新数据' : '首次录入' }}</h3>
          <el-form :inline="true" :model="form" size="default">
            <el-form-item label="日期"><el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width:140px" size="small" /></el-form-item>
            <el-form-item label="体重"><el-input-number v-model="form.weight" :min="30" :max="300" :precision="1" :step="0.5" style="width:120px" size="small" /></el-form-item>
            <el-form-item label="体脂"><el-input-number v-model="form.bodyFat" :min="1" :max="60" :precision="1" :step="0.5" style="width:110px" size="small" /></el-form-item>
            <el-form-item label="腰围"><el-input-number v-model="form.waist" :min="30" :max="200" :precision="1" style="width:110px" size="small" /></el-form-item>
            <el-form-item label="臀围"><el-input-number v-model="form.hip" :min="30" :max="200" :precision="1" style="width:110px" size="small" /></el-form-item>
            <el-form-item><el-button type="primary" @click="handleSave" :loading="saving" size="small">记录</el-button></el-form-item>
          </el-form>
          <div class="form-hint" v-if="!hasHeight">提示：前往 <router-link to="/profile">个人资料</router-link> 设置身高后，BMI 将自动计算</div>
        </div>

        <!-- Summary Mini Cards -->
        <div class="summary-mini" v-if="latest">
          <div class="mini-card" v-for="s in summaryCards.slice(2)" :key="s.label">
            <span class="mini-value" :style="{ color: s.color }">{{ s.display }}</span>
            <span class="mini-unit">{{ s.unit }}</span>
            <span class="mini-label">{{ s.label }}</span>
          </div>
        </div>
      </div>

      <!-- History Table -->
      <div class="history-card scroll-fade" style="transition-delay:100ms">
        <div class="history-header">
          <h3 class="card-section-title" style="margin:0">历史记录</h3>
          <el-date-picker v-model="dateRange" type="daterange" range-separator="~" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" size="small" style="width:230px" @change="fetchData" unlink-panels />
        </div>
        <el-table :data="pagedRecords" stripe v-loading="loading" size="default">
          <template #empty><el-empty description="暂无数据" :image-size="80" /></template>
          <el-table-column label="日期" prop="recordDate" width="110" />
          <el-table-column label="体重(kg)" width="100"><template #default="{ row }"><span :class="row.weight ? '' : 'text-weak'">{{ row.weight ?? '—' }}</span></template></el-table-column>
          <el-table-column label="BMI" width="70">
            <template #default="{ row }"><span v-if="row.bmi" :class="bmiClass(row.bmi)">{{ row.bmi }}</span><span v-else class="text-weak">—</span></template>
          </el-table-column>
          <el-table-column label="体脂(%)" width="90"><template #default="{ row }">{{ row.bodyFat ?? '—' }}</template></el-table-column>
          <el-table-column label="腰围(cm)" width="90"><template #default="{ row }">{{ row.waist ?? '—' }}</template></el-table-column>
          <el-table-column label="臀围(cm)" width="90"><template #default="{ row }">{{ row.hip ?? '—' }}</template></el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }"><div class="action-cell"><el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button><el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)"><template #reference><el-button text type="danger" size="small">删除</el-button></template></el-popconfirm></div></template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination v-model:current-page="page" :page-size="pageSize" :page-sizes="[10,20,50]" :total="records.length" layout="total, sizes, prev, pager, next" background @size-change="page=1" />
        </div>
      </div>
    </div>

    <!-- Edit Dialog -->
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
const showGoalInput = ref(false)

const form = reactive({ recordDate: new Date().toISOString().slice(0, 10), weight: null, bodyFat: null, waist: null, hip: null })

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
  showGoalInput.value = false
  ElMessage.success('目标已保存')
}

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

const trendMetric = ref('weight')
const trendColor = ref('#667eea')
const trendColors = { weight: '#667eea', bmi: '#67c23a', bodyFat: '#e6a23c', waist: '#f56c6c' }
const trendData = computed(() => {
  const key = trendMetric.value
  return [...records.value].filter(r => r[key] != null).sort((a, b) => a.recordDate?.localeCompare(b.recordDate)).map(r => ({ date: r.recordDate, value: r[key] }))
})
function updateTrend() { trendColor.value = trendColors[trendMetric.value] || '#667eea' }

const pagedRecords = computed(() => { const s = (page.value - 1) * pageSize.value; return records.value.slice(s, s + pageSize.value) })

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
/* ---- Layout ---- */
.metrics-page {
  margin: calc(-1 * var(--space-lg)) calc(-1 * var(--page-padding-x)) calc(-1 * var(--space-2xl));
}

/* ---- Hero Banner ---- */
.metrics-hero {
  position: relative;
  left: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  width: 100vw;
  padding: 80px 0 56px;
  background: url(https://ts1.tc.mm.bing.net/th/id/R-C.ec2011fc32f16a6ffdbbd3321a08e3b1?rik=n5mhU9i2UpAT3A&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2f7823c7df89acd0c22460c3fc10e26bde97c6645c.jpg&ehk=XweqJ5ToNRtBja12%2f%2faEk%2bxmbf9fQ8IF3CqwNhsNneQ%3d&risl=&pid=ImgRaw&r=0) center / cover no-repeat;
  background-attachment: fixed;
  isolation: isolate;
  text-align: center;
}
.metrics-hero-overlay {
  position: absolute; inset: 0; z-index: 0;
  background: rgba(0, 0, 0, 0.75);
}
.metrics-hero-content {
  position: relative; z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--page-padding-x);
}
.metrics-hero-title {
  font-size: 36px;
  font-weight: var(--font-weight-bold);
  color: #FFFFFF;
  margin: 0 0 24px;
}

/* Hero body stats */
.hero-body {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 48px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.hero-main-stat {
  text-align: center;
}
.hero-big-value {
  font-size: 48px;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.1;
}
.hero-big-unit {
  font-size: 18px;
  color: rgba(255,255,255,0.5);
  margin-left: 4px;
}
.hero-big-label {
  display: block;
  font-size: 14px;
  color: rgba(255,255,255,0.6);
  margin-top: 4px;
}
.hero-divider {
  width: 1px;
  height: 50px;
  background: rgba(255,255,255,0.2);
}
.bmi-cat-tag {
  display: inline-block;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: 6px;
  font-weight: 500;
}
.bmi-cat-tag.bmi-under { background: rgba(230,162,60,0.3); color: #f0c060; }
.bmi-cat-tag.bmi-normal { background: rgba(103,194,58,0.3); color: #67c23a; }
.bmi-cat-tag.bmi-over { background: rgba(230,162,60,0.3); color: #f0c060; }
.bmi-cat-tag.bmi-obese { background: rgba(245,108,108,0.3); color: #f56c6c; }

/* Hero goal bar */
.hero-goal {
  max-width: 360px;
  margin: 0 auto 0;
}
.hero-goal-bar {
  height: 4px;
  background: rgba(255,255,255,0.15);
  border-radius: 2px;
  margin-bottom: 8px;
  overflow: hidden;
}
.hero-goal-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.8s ease;
}
.hero-goal-text {
  font-size: 12px;
  color: rgba(255,255,255,0.5);
}
.hero-empty { margin-bottom: 8px; }
.hero-cta {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.3);
  color: #FFFFFF;
  border-radius: 20px;
}
.hero-goal-input {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}

/* ---- Container ---- */
.metrics-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-xl) var(--page-padding-x) var(--space-2xl);
}
.card-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-title);
  margin: 0 0 16px;
}

/* ---- Trend ---- */
.trend-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}
.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.trend-chart { display: block; }

/* ---- Entry + Summary ---- */
.entry-summary-row {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  margin-bottom: 20px;
}
@media (max-width: 767px) {
  .entry-summary-row { grid-template-columns: 1fr; }
}
.entry-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.entry-card :deep(.el-form-item) { margin-bottom: 8px; margin-right: 8px; }
.form-hint { margin-top: 8px; font-size: 12px; color: var(--color-text-weak); }
.form-hint a { color: var(--color-brand); }

.summary-mini {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.mini-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.mini-value { font-size: 24px; font-weight: 700; }
.mini-unit { font-size: 12px; color: var(--color-text-weak); }
.mini-label { font-size: 12px; color: var(--color-text-secondary); display: block; margin-top: 2px; }

/* ---- History ---- */
.history-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.text-weak { color: var(--color-text-weak); }
.bmi-under { color: #e6a23c; font-weight: 600; }
.bmi-normal { color: #67c23a; font-weight: 600; }
.bmi-over { color: #e6a23c; font-weight: 600; }
.bmi-obese { color: #f56c6c; font-weight: 600; }
.action-cell { display: flex; align-items: center; gap: 0; }

.pagination-wrap { margin-top: 16px; display: flex; justify-content: center; }
</style>
