<template>
  <div class="workout-session" :class="{ 'resting': isResting, 'rest-ending': restEnding }">
    <!-- Top Bar -->
    <div class="ws-topbar">
      <el-button text @click="confirmExit" class="ws-back">← 退出</el-button>
      <span class="ws-training-type">{{ setup.trainingType }}</span>
      <span class="ws-progress">{{ exerciseIndex + 1 }}/{{ setup.exercises.length }} 动作</span>
    </div>

    <!-- Exercise Display -->
    <transition name="fade" mode="out-in">
      <div class="ws-exercise" :key="exerciseIndex">
        <!-- Image -->
        <div class="ws-flip-box" v-if="currentExercise?.imageUrl && isExercisePath(currentExercise.imageUrl)">
          <img :src="currentExercise.imageUrl + '0.jpg'" class="ws-flip-img img-start" />
          <img :src="currentExercise.imageUrl + '1.jpg'" class="ws-flip-img img-end" />
        </div>
        <div class="ws-img-placeholder" v-else>🏋️</div>

        <h2 class="ws-ex-title">{{ currentExercise?.title || '动作' }}</h2>
        <p class="ws-ex-desc">第 {{ currentSet }} / {{ currentExercise?.sets || 8 }} 组</p>

        <!-- Set dots -->
        <div class="ws-set-dots">
          <div v-for="i in (currentExercise?.sets || 8)" :key="i"
            class="ws-dot"
            :class="{ done: i <= currentSet, current: i === currentSet, next: i === currentSet + 1 }"
          />
        </div>
      </div>
    </transition>

    <!-- Action Area -->
    <div class="ws-action">
      <!-- Rest Timer -->
      <transition name="fade">
        <div v-if="isResting" class="ws-rest">
          <div class="ws-rest-ring" :class="{ pulse: restEnding }">
            <svg viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="44" fill="none" stroke="var(--color-border-light)" stroke-width="4" />
              <circle cx="50" cy="50" r="44" fill="none" :stroke="restEnding ? '#FF4D4F' : '#D93831'" stroke-width="4"
                stroke-linecap="round" :stroke-dasharray="restCircumference" :stroke-dashoffset="restDashOffset"
                transform="rotate(-90 50 50)" class="ws-rest-progress" />
            </svg>
            <div class="ws-rest-center">
              <span class="ws-rest-num">{{ restDisplay }}</span>
              <span class="ws-rest-label">秒</span>
            </div>
          </div>
          <span class="ws-rest-hint" v-if="!restEnding">组间休息</span>
          <span class="ws-rest-hint pulse-text" v-else>准备开始！</span>
          <el-button size="small" @click="skipRest" class="ws-skip">跳过休息</el-button>
        </div>
      </transition>

      <!-- Complete Set Button -->
      <el-button v-if="!isResting" type="primary" size="large" class="ws-complete-btn" @click="completeSet">
        {{ currentSet >= (currentExercise?.sets || 8) ? '完成动作 ✓' : `完成第 ${currentSet} 组 ✓` }}
      </el-button>
    </div>

    <!-- Next Exercise Preview -->
    <div class="ws-next" v-if="exerciseIndex + 1 < setup.exercises.length && !isResting">
      <span class="ws-next-label">下一个动作</span>
      <span class="ws-next-name">{{ setup.exercises[exerciseIndex + 1]?.title }}</span>
    </div>
  </div>

  <!-- Completion Dialog -->
  <el-dialog v-model="completeVisible" title="🎉 训练完成！" width="440px" :close-on-click-modal="false" :close-on-press-escape="false">
    <div class="complete-summary">
      <div class="complete-row"><span>训练类型</span><span>{{ setup.trainingType }}</span></div>
      <div class="complete-row"><span>完成动作</span><span>{{ setup.exercises.length }} 个</span></div>
      <div class="complete-row"><span>完成组数</span><span>{{ totalSetsCompleted }} 组</span></div>
      <div class="complete-row"><span>总时长</span><span>{{ elapsedMinutes }} 分 {{ elapsedSeconds % 60 }} 秒</span></div>
    </div>
    <el-divider />
    <el-form label-width="80px">
      <el-form-item label="强度">
        <el-radio-group v-model="completeForm.intensity">
          <el-radio-button value="EASY">轻松</el-radio-button>
          <el-radio-button value="MEDIUM">中等</el-radio-button>
          <el-radio-button value="HARD">高强度</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="completeForm.notes" placeholder="训练感受..." />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="discardWorkout">放弃记录</el-button>
      <el-button type="primary" @click="saveWorkout" :loading="saving">保存训练记录</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addTrainingRecord } from '@/api/training'

const router = useRouter()

// Load setup from sessionStorage
const setup = reactive({ trainingType: '', exercises: [] })
try {
  const raw = sessionStorage.getItem('workout_setup')
  if (raw) Object.assign(setup, JSON.parse(raw))
} catch {}
if (!setup.exercises.length) router.replace('/training')

// State
const exerciseIndex = ref(0)
const currentSet = ref(1)
const isResting = ref(false)
const restSeconds = ref(0)
const restTotal = ref(0)
const restEnding = ref(false)
const completeVisible = ref(false)
const saving = ref(false)
const sessionStart = Date.now()
const totalSetsCompleted = ref(0)
let restTimer = null

const currentExercise = computed(() => setup.exercises[exerciseIndex.value] || null)
const restDisplay = computed(() => String(Math.max(0, restSeconds.value)))
const restCircumference = 2 * Math.PI * 44
const restDashOffset = computed(() => {
  const pct = restTotal.value > 0 ? restSeconds.value / restTotal.value : 0
  return restCircumference * (1 - pct)
})

const completeForm = reactive({ intensity: 'MEDIUM', notes: '' })

function isExercisePath(url) { return /\/exercises\/[^/]+\/$/.test(url) }

function completeSet() {
  const maxSets = currentExercise.value?.sets || 8
  if (currentSet.value < maxSets) {
    currentSet.value++
    totalSetsCompleted.value++
    startRest()
  } else {
    // Move to next exercise
    totalSetsCompleted.value++
    if (exerciseIndex.value + 1 < setup.exercises.length) {
      exerciseIndex.value++
      currentSet.value = 1
      startRest()
    } else {
      // All done
      finishWorkout()
    }
  }
}

function startRest() {
  isResting.value = true
  restEnding.value = false
  restTotal.value = currentExercise.value?.restSeconds || 60
  restSeconds.value = restTotal.value
  restTimer = setInterval(() => {
    restSeconds.value--
    if (restSeconds.value <= 5 && restSeconds.value > 0) restEnding.value = true
    if (restSeconds.value <= 0) {
      clearInterval(restTimer)
      isResting.value = false
      restEnding.value = false
    }
  }, 1000)
}

function skipRest() {
  clearInterval(restTimer)
  isResting.value = false
  restEnding.value = false
  restSeconds.value = 0
}

function finishWorkout() {
  completeVisible.value = true
}

async function saveWorkout() {
  saving.value = true
  try {
    const elapsed = Math.floor((Date.now() - sessionStart) / 1000)
    const r = await addTrainingRecord({
      trainingType: setup.trainingType,
      intensity: completeForm.intensity,
      duration: Math.max(1, Math.round(elapsed / 60)),
      calories: Math.round(elapsed / 60 * 7), // ~7 kcal/min estimate
      notes: completeForm.notes || `${setup.exercises.map(e=>e.title).join('、')} · ${totalSetsCompleted.value}组`,
      recordDate: new Date().toISOString().slice(0, 10),
    })
    if (r.code === 200) {
      ElMessage.success('训练记录已保存！')
      sessionStorage.removeItem('workout_setup')
      router.replace('/training')
    }
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

function discardWorkout() {
  ElMessageBox.confirm('确定放弃本次训练记录？', '确认', { confirmButtonText: '放弃', cancelButtonText: '继续训练', type: 'warning' })
    .then(() => { sessionStorage.removeItem('workout_setup'); router.replace('/training') })
    .catch(() => {})
}

function confirmExit() {
  if (totalSetsCompleted.value > 0) {
    ElMessageBox.confirm('训练还未完成，确定退出？已完成的数据将会丢失。', '确认退出', { confirmButtonText: '退出', cancelButtonText: '继续训练', type: 'warning' })
      .then(() => { clearInterval(restTimer); sessionStorage.removeItem('workout_setup'); router.replace('/training') })
      .catch(() => {})
  } else {
    sessionStorage.removeItem('workout_setup'); router.replace('/training')
  }
}

const elapsedMinutes = computed(() => Math.floor((Date.now() - sessionStart) / 60000))
const elapsedSeconds = computed(() => Math.floor((Date.now() - sessionStart) / 1000))

onUnmounted(() => { clearInterval(restTimer) })
</script>

<style scoped>
.workout-session {
  min-height: calc(100vh - var(--nav-height) - 48px);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px 48px;
  max-width: 600px;
  margin: 0 auto;
  background: var(--color-bg-page);
  transition: background 0.3s;
}
.workout-session.resting { background: #fafafa; }
.workout-session.rest-ending { animation: border-pulse 0.5s ease-in-out 4; }

@keyframes border-pulse {
  0%, 100% { box-shadow: inset 0 0 0 0 rgba(255, 77, 79, 0); }
  50% { box-shadow: inset 0 0 0 4px rgba(255, 77, 79, 0.3); }
}

/* Top Bar */
.ws-topbar { width: 100%; display: flex; align-items: center; gap: 16px; margin-bottom: 32px; }
.ws-back { flex-shrink: 0; }
.ws-training-type { font-weight: 600; color: var(--color-text-title); }
.ws-progress { margin-left: auto; font-size: 13px; color: var(--color-text-secondary); }

/* Exercise Display */
.ws-exercise { text-align: center; flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; width: 100%; }
.ws-flip-box { position: relative; width: 100%; max-width: 400px; aspect-ratio: 16/10; border-radius: var(--radius-md); overflow: hidden; background: var(--color-bg-light); }
.ws-flip-img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: contain; transition: opacity 0.8s ease-in-out; }
.img-start { opacity: 1; animation: kenBurns 4s ease-in-out infinite; }
.img-end   { opacity: 0; animation: kenBurns 4s ease-in-out 2s infinite; }
.ws-flip-box:hover .img-start { opacity: 0; }
.ws-flip-box:hover .img-end   { opacity: 1; }
@keyframes kenBurns { 0%,100% { transform: scale(1); } 50% { transform: scale(1.04); } }
.ws-img-placeholder { width: 100%; max-width: 400px; aspect-ratio: 16/10; border-radius: var(--radius-md); background: var(--color-bg-light); display: flex; align-items: center; justify-content: center; font-size: 64px; }
.ws-ex-title { font-size: 24px; font-weight: 700; color: var(--color-text-title); margin: 0; }
.ws-ex-desc { font-size: 15px; color: var(--color-text-secondary); margin: 0; }

/* Set Dots */
.ws-set-dots { display: flex; gap: 10px; }
.ws-dot { width: 16px; height: 16px; border-radius: 50%; background: var(--color-border-light); transition: all var(--transition-fast); }
.ws-dot.done { background: var(--color-brand); }
.ws-dot.current { background: var(--color-brand); box-shadow: 0 0 0 4px rgba(217, 56, 49, 0.25); }
.ws-dot.next { background: var(--color-border); }

/* Action Area */
.ws-action { width: 100%; display: flex; flex-direction: column; align-items: center; padding: 32px 0; }
.ws-complete-btn { width: 100%; max-width: 360px; height: 56px; font-size: 18px; font-weight: 600; border-radius: var(--radius-md); transition: all var(--transition-fast); }
.ws-complete-btn:active { transform: scale(0.97); }

/* Rest Timer */
.ws-rest { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.ws-rest-ring { position: relative; width: 120px; height: 120px; }
.ws-rest-ring.pulse { animation: ring-pulse 0.5s ease-in-out infinite; }
@keyframes ring-pulse { 50% { transform: scale(1.04); } }
.ws-rest-ring svg { width: 100%; height: 100%; }
.ws-rest-progress { transition: stroke-dashoffset 1s linear; }
.ws-rest-center { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0; }
.ws-rest-num { font-size: 36px; font-weight: 700; color: var(--color-text-title); font-variant-numeric: tabular-nums; font-family: 'SF Mono','Cascadia Code','Consolas',monospace; }
.ws-rest-label { font-size: 12px; color: var(--color-text-secondary); }
.ws-rest-hint { font-size: 14px; color: var(--color-text-secondary); }
.ws-rest-hint.pulse-text { color: #FF4D4F; font-weight: 600; animation: text-pulse 0.5s ease-in-out infinite; }
@keyframes text-pulse { 50% { opacity: 0.6; } }
.ws-skip { margin-top: 4px; }

/* Next exercise */
.ws-next { display: flex; flex-direction: column; align-items: center; gap: 4px; opacity: 0.5; padding-bottom: 24px; }
.ws-next-label { font-size: 12px; color: var(--color-text-weak); }
.ws-next-name { font-size: 14px; color: var(--color-text-secondary); }

/* Completion */
.complete-summary { display: flex; flex-direction: column; gap: 12px; }
.complete-row { display: flex; justify-content: space-between; font-size: 15px; }
.complete-row span:first-child { color: var(--color-text-secondary); }
.complete-row span:last-child { font-weight: 600; }

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
