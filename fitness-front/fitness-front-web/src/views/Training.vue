<template>
  <div class="training-page">
    <!-- Hero Banner -->
    <section class="training-hero">
      <div class="training-hero-overlay" />
      <div class="training-hero-content">
        <h1 class="training-hero-title reveal-item">训练记录</h1>
        <div class="hero-stats reveal-item" style="transition-delay:80ms">
          <div class="hero-stat" v-for="s in stats" :key="s.label">
            <span class="hero-stat-value" :style="{ color: s.color }">{{ s.displayValue }}</span>
            <span class="hero-stat-label">{{ s.label }}</span>
          </div>
        </div>
        <el-button class="hero-cta reveal-item" style="transition-delay:160ms" size="large" @click="openSetup">▶ 开始今日训练</el-button>
      </div>
    </section>

    <!-- Content -->
    <div class="training-container">
      <!-- Heatmap -->
      <div class="heatmap-card scroll-fade" v-if="heatmapDays.length > 0">
        <div class="heatmap-header" @click="heatmapExpanded = !heatmapExpanded">
          <span class="heatmap-title">训练日历</span>
          <span class="heatmap-streak" v-if="streak > 0">🔥 连续 {{ streak }} 天</span>
          <span class="heatmap-toggle">{{ heatmapExpanded ? '收起' : '展开' }}</span>
        </div>
        <div class="heatmap-body" v-show="heatmapExpanded">
          <div class="heatmap-grid">
            <div v-for="d in heatmapDays" :key="d.date" class="heatmap-cell" :class="{ trained: d.trained, today: d.isToday }" :title="d.date + (d.trained ? ' · 已训练' : '')" />
          </div>
          <div class="heatmap-legend">
            <span class="legend-dot empty" /><span>未训练</span>
            <span class="legend-dot filled" /><span>已训练</span>
            <span class="legend-dot today-dot" /><span>今日</span>
          </div>
        </div>
      </div>

      <!-- Quick Record + Timer -->
      <div class="quick-card scroll-fade" style="transition-delay:60ms" id="add-form-card">
        <div class="quick-grid">
          <div class="quick-form">
            <h3 class="card-section-title">快速记录</h3>
            <el-form :inline="true" :model="addForm" size="default" class="add-form">
              <el-form-item label="日期"><el-date-picker v-model="addForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width:140px" size="small" /></el-form-item>
              <el-form-item label="类型"><el-select v-model="addForm.trainingType" placeholder="训练类型" size="small" style="width:150px" clearable><el-option v-for="t in trainingTypes" :key="t.value" :label="t.icon+' '+t.label" :value="t.value" /></el-select></el-form-item>
              <el-form-item label="强度"><el-radio-group v-model="addForm.intensity" size="small"><el-radio-button value="EASY">轻松</el-radio-button><el-radio-button value="MEDIUM">中等</el-radio-button><el-radio-button value="HARD">高强度</el-radio-button></el-radio-group></el-form-item>
              <el-form-item label="时长"><el-input-number v-model="addForm.duration" :min="1" :max="300" style="width:90px" size="small" /></el-form-item>
              <el-form-item label="热量"><el-input-number v-model="addForm.calories" :min="0" :max="5000" style="width:100px" size="small" /></el-form-item>
              <el-form-item label="备注"><el-input v-model="addForm.notes" placeholder="训练内容" style="width:140px" size="small" /></el-form-item>
              <el-form-item><el-button type="primary" @click="handleAdd" :loading="adding" size="small">记录</el-button></el-form-item>
            </el-form>
          </div>
          <div class="quick-timer">
            <h3 class="card-section-title">计时器</h3>
            <div class="timer-box">
              <div class="timer-display">{{ timerDisplay }}</div>
              <div class="timer-btns">
                <el-button size="small" type="success" @click="timerStart" :disabled="timerRunning" v-if="!timerRunning">开始</el-button>
                <el-button size="small" type="warning" @click="timerPause" v-if="timerRunning">暂停</el-button>
                <el-button size="small" @click="timerStop" v-if="timerRunning || timerSeconds > 0">停止</el-button>
                <el-button size="small" @click="timerReset" v-if="!timerRunning && timerSeconds > 0">重置</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- History Table -->
      <div class="history-card scroll-fade" style="transition-delay:100ms">
        <div class="history-header">
          <h3 class="card-section-title" style="margin:0">历史记录</h3>
          <div class="history-filter">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="~" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" size="small" style="width:230px" @change="page=1;fetchRecords()" unlink-panels />
            <span class="history-count" v-if="total">共 {{ total }} 条</span>
          </div>
        </div>
        <el-table :data="records" stripe v-loading="loading" size="default">
          <template #empty><el-empty description="暂无训练记录" :image-size="80"><el-button type="primary" size="small" @click="scrollToAdd">快速记录</el-button></el-empty></template>
          <el-table-column label="日期" width="110"><template #default="{ row }">{{ row.recordDate }}</template></el-table-column>
          <el-table-column label="类型" width="130"><template #default="{ row }"><el-tag v-if="row.trainingType" :color="typeColor(row.trainingType)" effect="dark" size="small" class="type-tag">{{ row.trainingType }}</el-tag><span v-else style="color:var(--color-text-weak)">—</span></template></el-table-column>
          <el-table-column label="强度" width="90"><template #default="{ row }"><span v-if="row.intensity" class="intensity-tag" :class="'int-'+row.intensity">{{ intensityLabel(row.intensity) }}</span><span v-else>—</span></template></el-table-column>
          <el-table-column label="时长" width="100"><template #default="{ row }">{{ row.duration||0 }} 分钟</template></el-table-column>
          <el-table-column label="热量" width="90"><template #default="{ row }">{{ row.calories||0 }} kcal</template></el-table-column>
          <el-table-column prop="notes" label="备注" min-width="120" show-overflow-tooltip><template #default="{ row }">{{ row.notes||'—' }}</template></el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }"><div class="action-cell"><el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button><el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)"><template #reference><el-button text type="danger" size="small">删除</el-button></template></el-popconfirm></div></template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap"><el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next" background @current-change="fetchRecords" @size-change="fetchRecords" /></div>
      </div>
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="editVisible" title="编辑训练记录" width="420px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="日期"><el-date-picker v-model="editForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="editForm.trainingType" style="width:100%" clearable><el-option v-for="t in trainingTypes" :key="t.value" :label="t.icon+' '+t.label" :value="t.value" /></el-select></el-form-item>
        <el-form-item label="强度"><el-radio-group v-model="editForm.intensity"><el-radio-button value="EASY">轻松</el-radio-button><el-radio-button value="MEDIUM">中等</el-radio-button><el-radio-button value="HARD">高强度</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="时长(分)"><el-input-number v-model="editForm.duration" :min="1" :max="300" style="width:100%" /></el-form-item>
        <el-form-item label="热量(kcal)"><el-input-number v-model="editForm.calories" :min="0" :max="5000" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="editForm.notes" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" @click="handleEdit" :loading="saving">保存</el-button></template>
    </el-dialog>

    <!-- Setup Dialog -->
    <el-dialog v-model="setupVisible" title="训练设置" width="680px" @closed="resetSetup">
      <el-form label-width="90px">
        <el-form-item label="训练类型"><el-select v-model="setup.trainingType" style="width:100%" @change="onTypeChange"><el-option v-for="t in trainingTypes" :key="t.value" :label="t.icon+' '+t.label" :value="t.value" /></el-select></el-form-item>
      </el-form>
      <el-divider>选择训练动作</el-divider>
      <div class="setup-exercise-search">
        <el-input v-model="exerciseSearch" placeholder="输入关键词搜索动作..." size="small" style="width:240px" @keyup.enter="doExerciseSearch" clearable @clear="exerciseResults=[]" />
        <el-button size="small" type="primary" @click="doExerciseSearch" :loading="exerciseSearching">搜索</el-button>
      </div>
      <div class="setup-exercise-results" v-if="exerciseResults.length > 0">
        <div v-for="ex in exerciseResults" :key="ex.id" class="exercise-pick-item" @click="addExerciseToSetup(ex)">
          <span class="ex-pick-title">{{ ex.title }}</span>
          <el-tag size="small" type="info" effect="plain">{{ ex.muscleHint || '' }}</el-tag>
          <span class="ex-pick-add">+ 添加</span>
        </div>
      </div>
      <div class="setup-selected" v-if="setup.exercises.length > 0">
        <div class="setup-selected-header">已选动作 ({{ setup.exercises.length }}) — 可单独调整组数和休息时间</div>
        <div v-for="(ex, i) in setup.exercises" :key="i" class="setup-ex-row">
          <span class="setup-ex-name">{{ ex.title }}</span>
          <span class="setup-ex-controls">
            <el-input-number v-model="ex.sets" :min="1" :max="12" size="small" style="width:55px" />
            <span class="ctrl-label">组</span>
            <el-input-number v-model="ex.restSeconds" :min="10" :max="300" :step="5" size="small" style="width:80px" />
            <span class="ctrl-label">秒休息</span>
          </span>
          <el-button text type="danger" size="small" @click="setup.exercises.splice(i,1)">移除</el-button>
        </div>
      </div>
      <el-divider />
      <div class="setup-save">
        <el-checkbox v-model="setup.saveAsPlan" label="保存为训练计划" />
        <el-input v-if="setup.saveAsPlan" v-model="setup.planName" placeholder="计划名称" size="small" style="width:160px;margin-left:8px" />
      </div>
      <template #footer>
        <el-button @click="loadPlanVisible=true" v-if="savedPlans.length > 0">📋 加载计划</el-button>
        <el-button @click="setupVisible=false">取消</el-button>
        <el-button type="primary" @click="startWorkout" :disabled="!setup.trainingType||setup.exercises.length===0">开始训练 ▶</el-button>
      </template>
    </el-dialog>

    <!-- Load Plan Dialog -->
    <el-dialog v-model="loadPlanVisible" title="加载训练计划" width="400px">
      <div v-for="p in savedPlans" :key="p.id" class="plan-load-item" @click="loadPlan(p)">
        <span class="plan-name">{{ p.name }}</span>
        <el-tag size="small">{{ p.trainingType }}</el-tag>
      </div>
      <el-empty v-if="savedPlans.length===0" description="暂无已保存计划" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addTrainingRecord, getTrainingRecords, updateTrainingRecord, deleteTrainingRecord, getWorkoutPlans, createWorkoutPlan, searchExercises } from '@/api/training'
import request from '@/utils/request'

const router = useRouter()

// Training types
const trainingTypes = [
  { label:'胸部训练',value:'胸部训练',icon:'🏋️',color:'#E74C3C'},{ label:'背部训练',value:'背部训练',icon:'🦅',color:'#3498DB'},{ label:'腿部训练',value:'腿部训练',icon:'🦵',color:'#2ECC71'},{ label:'肩部训练',value:'肩部训练',icon:'💪',color:'#F39C12'},{ label:'手臂训练',value:'手臂训练',icon:'🤳',color:'#9B59B6'},{ label:'核心训练',value:'核心训练',icon:'🎯',color:'#1ABC9C'},{ label:'有氧训练',value:'有氧训练',icon:'🏃',color:'#E67E22'},{ label:'瑜伽/拉伸',value:'瑜伽/拉伸',icon:'🧘',color:'#2ECC71'},{ label:'高强度间歇',value:'高强度间歇',icon:'⚡',color:'#E74C3C'},{ label:'全身训练',value:'全身训练',icon:'🔄',color:'#667EEA'},{ label:'其他',value:'其他',icon:'📋',color:'#95A5A6'},
]
const typeColor = t => trainingTypes.find(x=>x.value===t)?.color||'#999'
const intensityLabel = i => ({EASY:'轻松',MEDIUM:'中等',HARD:'高强度'}[i]||i)

// Records
const records = ref([]), loading = ref(false), adding = ref(false), saving = ref(false)
const page = ref(1), size = ref(10), total = ref(0), dateRange = ref([])
const streak = ref(0), heatmapDays = ref([]), heatmapExpanded = ref(true)
const addForm = reactive({ recordDate:new Date().toISOString().slice(0,10), trainingType:'', intensity:'', duration:null, calories:null, notes:'' })
const editVisible = ref(false), editingId = ref(null)
const editForm = reactive({ recordDate:'', trainingType:'', intensity:'', duration:null, calories:null, notes:'' })

// Timer
const timerRunning = ref(false), timerSeconds = ref(0)
let timerInterval = null
const timerDisplay = computed(() => { const m=String(Math.floor(timerSeconds.value/60)).padStart(2,'0'), s=String(timerSeconds.value%60).padStart(2,'0'); return `${m}:${s}` })
const timerStart=()=>{ timerRunning.value=true; timerInterval=setInterval(()=>timerSeconds.value++,1000) }
const timerPause=()=>{ timerRunning.value=false; clearInterval(timerInterval) }
const timerStop=()=>{ timerRunning.value=false; clearInterval(timerInterval); addForm.duration=Math.max(1,Math.round(timerSeconds.value/60)) }
const timerReset=()=>{ timerSeconds.value=0 }

// Stats
const statDisplays = reactive({ count:'0', dur:'0', cal:'0', days:'0', streak:'0' })
const stats = computed(()=>[
  { label:'训练次数',value:total.value,displayValue:statDisplays.count,color:'#667eea'},
  { label:'总时长(分)',value:records.value.reduce((s,r)=>s+(r.duration||0),0),displayValue:statDisplays.dur,color:'#67c23a'},
  { label:'总消耗(kcal)',value:records.value.reduce((s,r)=>s+(r.calories||0),0),displayValue:statDisplays.cal,color:'#e6a23c'},
  { label:'本周天数',value:streak.value,displayValue:statDisplays.days,color:'#F39C12'},
  { label:'连续训练',value:streak.value,displayValue:statDisplays.streak,color:'#E74C3C'},
])
function animateStats(){ stats.value.forEach((s,i)=>{const t=Number(s.value)||0,k=['count','dur','cal','days','streak'][i],d=performance.now();function step(n){const p=Math.min(1,(n-d)/500),e=1-(1-p)*(1-p);statDisplays[k]=String(Math.round(t*e));if(p<1)requestAnimationFrame(step);else statDisplays[k]=String(t)}requestAnimationFrame(step)})}
function scrollToAdd(){ document.getElementById('add-form-card')?.scrollIntoView({behavior:'smooth'}) }

// Setup dialog
const setupVisible = ref(false), loadPlanVisible = ref(false)
const exerciseSearch = ref(''), exerciseSearching = ref(false), exerciseResults = ref([]), savedPlans = ref([])
const setup = reactive({ trainingType:'', exercises:[], saveAsPlan:false, planName:'' })

const muscleKeywords = {
  '胸部训练': 'Bench Press Chest Fly Pec Floor Guillotine World',
  '背部训练': 'Pull Up Row Lat Cable Pulldown Overhead Bent-Arm',
  '腿部训练': 'Squat Leg Lunge Quad Backward Drag Split',
  '肩部训练': 'Shoulder Deltoid Press Raise Arnold Kettlebell Circus',
  '手臂训练': 'Curl Biceps Triceps Hammer Incline Skull Dips',
  '核心训练': 'Ab Crunch Sit-Up Air Bike Heel Roller Plank',
  '有氧训练': '',
  '瑜伽/拉伸': 'Stretch SMR',
  '高强度间歇': 'Clean Snatch Burpee',
  '全身训练': 'Squat Deadlift Clean',
}

function resetSetup(){ setup.trainingType=''; setup.exercises=[]; setup.saveAsPlan=false; setup.planName=''; exerciseSearch.value=''; exerciseResults.value=[] }

async function openSetup(){ setupVisible.value=true; try{ const r=await getWorkoutPlans(); if(r.code===200)savedPlans.value=r.data||[] }catch{} }

async function onTypeChange(type){
  exerciseResults.value = []
  if (!type) return
  exerciseSearch.value = ''
  exerciseSearching.value = true
  try {
    const kw = muscleKeywords[type] || type
    const r = await searchExercises(kw)
    if (r.code === 200) exerciseResults.value = (r.data || []).map(ex => ({ ...ex, muscleHint: type }))
  } catch { ElMessage.error('加载动作失败') }
  finally { exerciseSearching.value = false }
}

async function doExerciseSearch(){
  if(!exerciseSearch.value.trim()) return
  exerciseSearching.value = true
  try {
    const r = await searchExercises(exerciseSearch.value.trim())
    if (r.code === 200) exerciseResults.value = (r.data || [])
  } catch { ElMessage.error('搜索失败') }
  finally { exerciseSearching.value = false }
}

function addExerciseToSetup(ex){
  if(!setup.exercises.find(e=>e.exerciseId===ex.id))
    setup.exercises.push({exerciseId:ex.id, title:ex.title, imageUrl:ex.videoUrl, sets:4, restSeconds:60})
}

function loadPlan(p){
  setup.trainingType=p.trainingType||''
  try { setup.exercises=JSON.parse(p.exercises)||[] } catch {}
  loadPlanVisible.value=false
}

async function startWorkout(){
  if(setup.saveAsPlan && setup.planName.trim()) await createWorkoutPlan({ name:setup.planName.trim(), trainingType:setup.trainingType, exercises:JSON.stringify(setup.exercises) })
  sessionStorage.setItem('workout_setup', JSON.stringify({ trainingType:setup.trainingType, exercises:setup.exercises }))
  setupVisible.value=false; router.push('/workout')
}

// CRUD
onMounted(()=>{ fetchRecords(); fetchStats() })
async function fetchRecords(){
  loading.value=true
  try{ const p={ page:page.value, size:size.value }; if(dateRange.value?.length===2){ p.start=dateRange.value[0]; p.end=dateRange.value[1] } const r=await getTrainingRecords(p); if(r.code===200){ records.value=r.data.records; total.value=r.data.total; await nextTick(); animateStats() } }catch{ ElMessage.error('加载失败') }finally{loading.value=false}
}
async function fetchStats(){
  try{ const s=new Date(); s.setDate(s.getDate()-28); const r=await request.get('/training/records/stats',{params:{start:s.toISOString().slice(0,10),end:new Date().toISOString().slice(0,10)}}); if(r.code===200){ streak.value=r.data.currentStreak||0; const ds=new Set(Object.keys(r.data.dailyRecords||{})); const c=[]; for(let i=27;i>=0;i--){ const d=new Date(); d.setDate(d.getDate()-i); const ds2=d.toISOString().slice(0,10); c.push({date:ds2,trained:ds.has(ds2),isToday:i===0}) } heatmapDays.value=c } }catch{}
}
async function handleAdd(){ adding.value=true; try{ const r=await addTrainingRecord({...addForm}); if(r.code===200){ ElMessage.success('已记录'); addForm.notes=''; fetchRecords(); fetchStats() } }catch{ ElMessage.error('记录失败') }finally{adding.value=false} }
function openEdit(row){ editingId.value=row.id; editForm.recordDate=row.recordDate; editForm.trainingType=row.trainingType||''; editForm.intensity=row.intensity||''; editForm.duration=row.duration; editForm.calories=row.calories; editForm.notes=row.notes||''; editVisible.value=true }
async function handleEdit(){ saving.value=true; try{ const r=await updateTrainingRecord(editingId.value,{...editForm}); if(r.code===200){ ElMessage.success('已更新'); editVisible.value=false; fetchRecords(); fetchStats() } }catch{ ElMessage.error('更新失败') }finally{saving.value=false} }
async function handleDelete(id){ try{ const r=await deleteTrainingRecord(id); if(r.code===200){ ElMessage.success('已删除'); fetchRecords(); fetchStats() } }catch{ ElMessage.error('删除失败') } }
onUnmounted(()=>{ clearInterval(timerInterval) })
</script>

<style scoped>
/* ---- Layout ---- */
.training-page {
  margin: calc(-1 * var(--space-lg)) calc(-1 * var(--page-padding-x)) calc(-1 * var(--space-2xl));
}

/* ---- Hero Banner ---- */
.training-hero {
  position: relative;
  left: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  width: 100vw;
  padding: 80px 0 60px;
  background: url(https://ts2.tc.mm.bing.net/th/id/OIP-C.gQaE8vL7J7Zrm2EqzIPjcAHaE7?rs=1&pid=ImgDetMain&o=7&rm=3) center / cover no-repeat;
  background-attachment: fixed;
  isolation: isolate;
  text-align: center;
}
.training-hero-overlay {
  position: absolute; inset: 0; z-index: 0;
  background: rgba(0, 0, 0, 0.75);
}
.training-hero-content {
  position: relative; z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--page-padding-x);
}
.training-hero-title {
  font-size: 36px;
  font-weight: var(--font-weight-bold);
  color: #FFFFFF;
  margin: 0 0 28px;
}

/* Hero Stats */
.hero-stats {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 28px;
}
.hero-stat {
  background: rgba(255,255,255,0.12);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 12px;
  padding: 14px 24px;
  min-width: 100px;
  text-align: center;
}
.hero-stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.hero-stat-label {
  font-size: 12px;
  color: rgba(255,255,255,0.65);
  margin-top: 4px;
  display: block;
}
.hero-cta {
  background: var(--color-brand);
  border-color: var(--color-brand);
  font-size: 16px;
  padding: 14px 36px;
  font-weight: 600;
  border-radius: 50px;
}
.hero-cta:hover {
  background: var(--color-brand-hover);
  border-color: var(--color-brand-hover);
}

/* ---- Content Container ---- */
.training-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-xl) var(--page-padding-x) var(--space-2xl);
}

/* ---- Card Base ---- */
.card-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-title);
  margin: 0 0 16px;
}

/* ---- Heatmap ---- */
.heatmap-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}
.heatmap-header {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
}
.heatmap-title { font-weight: 600; font-size: 15px; }
.heatmap-streak { font-size: 13px; color: var(--color-brand); font-weight: 600; }
.heatmap-toggle { font-size: 12px; color: var(--color-text-weak); margin-left: auto; }
.heatmap-body { margin-top: 14px; }
.heatmap-grid { display: flex; gap: 4px; flex-wrap: wrap; }
.heatmap-cell {
  width: 30px; height: 30px;
  border-radius: 4px;
  background: #F0F0F0;
  border: 1px solid #E5E5E5;
  transition: all var(--transition-fast);
}
.heatmap-cell:hover { transform: scale(1.15); }
.heatmap-cell.trained { background: #67c23a; border-color: #52b818; }
.heatmap-cell.today { box-shadow: 0 0 0 2px var(--color-brand); }
.heatmap-legend { display: flex; align-items: center; gap: 6px; margin-top: 10px; font-size: 12px; color: var(--color-text-secondary); }
.legend-dot { width: 12px; height: 12px; border-radius: 2px; }
.legend-dot.empty { background: #F0F0F0; border: 1px solid #E5E5E5; }
.legend-dot.filled { background: #67c23a; }
.legend-dot.today-dot { background: #FFF; border: 2px solid var(--color-brand); border-radius: 2px; width: 12px; height: 12px; }

/* ---- Quick Record + Timer ---- */
.quick-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}
.quick-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 32px;
}
@media (max-width: 767px) {
  .quick-grid { grid-template-columns: 1fr; gap: 20px; }
}
.quick-form { min-width: 0; }
.add-form :deep(.el-form-item) { margin-bottom: 8px; margin-right: 10px; }
.add-form :deep(.el-radio-button__inner) { padding: 4px 10px; font-size: 12px; }

.quick-timer {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-left: 1px solid var(--color-border);
  padding-left: 32px;
}
@media (max-width: 767px) {
  .quick-timer { border-left: none; border-top: 1px solid var(--color-border); padding-left: 0; padding-top: 20px; }
}
.timer-box { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.timer-display {
  font-size: 48px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--color-text-title);
  letter-spacing: 3px;
  font-family: 'SF Mono','Cascadia Code','Consolas',monospace;
}
.timer-btns { display: flex; gap: 8px; }

/* ---- History Table ---- */
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
.history-filter { display: flex; align-items: center; gap: 12px; }
.history-count { font-size: 12px; color: var(--color-text-weak); }

.type-tag { font-weight: 500; border: none; }
.intensity-tag { font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 3px; }
.int-EASY { background: #e8f5e9; color: #388e3c; }
.int-MEDIUM { background: #fff8e1; color: #f57c00; }
.int-HARD { background: #ffebee; color: #d32f2f; }
.action-cell { display: flex; align-items: center; gap: 0; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: center; }

/* ---- Setup Dialog ---- */
.setup-exercise-search { display: flex; gap: 8px; margin-bottom: 12px; }
.exercise-pick-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-radius: var(--radius-md); cursor: pointer; transition: all var(--transition-fast); }
.exercise-pick-item:hover { background: var(--color-bg-light); }
.ex-pick-title { font-weight: 500; }
.ex-pick-add { font-size: 12px; color: var(--color-brand); font-weight: 600; }
.setup-selected { margin-top: 12px; }
.setup-selected-header { font-weight: 600; margin-bottom: 8px; }
.setup-ex-row { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid var(--color-border-light); flex-wrap: wrap; }
.setup-ex-name { flex: 1; font-weight: 500; min-width: 120px; font-size: 14px; }
.setup-ex-controls { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.setup-ex-controls :deep(.el-input-number .el-input__wrapper) { padding: 0 5px; }
.ctrl-label { font-size: 12px; color: var(--color-text-secondary); white-space: nowrap; margin: 0 8px 0 2px; }
.plan-load-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; border-radius: var(--radius-md); cursor: pointer; transition: all var(--transition-fast); }
.plan-load-item:hover { background: var(--color-bg-light); }
.plan-name { font-weight: 500; }
.setup-save { display: flex; align-items: center; padding: 4px 0; }
</style>
