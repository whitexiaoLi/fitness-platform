<template>
  <div class="diet-page">
    <!-- Hero Banner -->
    <section class="diet-hero">
      <div class="diet-hero-overlay" />
      <div class="diet-hero-content">
        <h1 class="diet-hero-title reveal-item">饮食记录</h1>
        <div class="hero-rings reveal-item" style="transition-delay:80ms">
          <div class="hero-ring" v-for="s in statCards" :key="s.label">
            <MacroRing :value="s.rawValue" :max="goals[s.goalKey] || s.rawValue || 1" :label="s.label" :unit="s.unit" :color="s.color" :decimals="s.decimals" />
            <span class="hero-ring-goal" v-if="goals[s.goalKey]">目标 {{ goals[s.goalKey] }}{{ s.unit }}</span>
            <span class="hero-ring-goal none" v-else>未设目标</span>
          </div>
        </div>
        <div class="hero-date reveal-item" style="transition-delay:120ms">
          <el-date-picker v-model="selectedDate" type="date" value-format="YYYY-MM-DD" @change="fetchRecords" class="hero-date-picker" />
        </div>
      </div>
    </section>

    <!-- Content -->
    <div class="diet-container">
      <div class="diet-layout">
        <!-- Left: Meal Tabs + Food List -->
        <div class="diet-main scroll-fade">
          <div class="meal-card">
            <div class="meal-card-header">
              <h3 class="card-section-title" style="margin:0">餐食记录</h3>
              <el-button size="small" type="primary" @click="openAddDialog">+ 添加食物</el-button>
            </div>
            <el-tabs v-model="activeMealType" type="card" @tab-change="onTabChange" v-loading="loading">
              <el-tab-pane v-for="mt in allMealTypes" :key="mt.code" :label="mt.label + ' (' + mealCount(mt.code) + ')'" :name="mt.code">
                <el-empty v-if="currentMealItems.length === 0 && !loading" description="该餐型暂无记录" :image-size="60" />
                <div v-else class="food-list">
                  <div class="food-item" v-for="item in currentMealItems" :key="item.id" @click="openEditDialog(item)">
                    <div class="food-left">
                      <div class="food-name-row">
                        <span class="food-name">{{ item.foodName || '未命名' }}</span>
                        <el-tag size="small" type="info" v-if="item.weightGrams">{{ item.weightGrams }}g</el-tag>
                      </div>
                      <div class="food-meta">
                        {{ item.calories || 0 }}kcal · 蛋白{{ item.protein || 0 }}g · 碳水{{ item.carbs || 0 }}g · 脂肪{{ item.fat || 0 }}g
                      </div>
                    </div>
                    <el-popconfirm title="确定删除？" @confirm.stop="handleDelete(item)">
                      <template #reference><el-button text type="danger" size="small" class="del-btn" @click.stop>删除</el-button></template>
                    </el-popconfirm>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>

        <!-- Right: Goals + Manage -->
        <div class="diet-side scroll-fade" style="transition-delay:80ms">
          <!-- Goals -->
          <div class="side-card">
            <div class="side-card-header" @click="showGoals = !showGoals">
              <span class="side-card-title">🎯 每日目标</span>
              <el-icon><ArrowUp v-if="showGoals" /><ArrowDown v-else /></el-icon>
            </div>
            <div class="side-goal-form" v-show="showGoals">
              <el-form size="small">
                <el-form-item label="热量"><el-input-number v-model="goals.calories" :min="0" :step="100" style="width:100%" size="small" /></el-form-item>
                <el-form-item label="蛋白质(g)"><el-input-number v-model="goals.protein" :min="0" :precision="1" :step="10" style="width:100%" size="small" /></el-form-item>
                <el-form-item label="碳水(g)"><el-input-number v-model="goals.carbs" :min="0" :precision="1" :step="10" style="width:100%" size="small" /></el-form-item>
                <el-form-item label="脂肪(g)"><el-input-number v-model="goals.fat" :min="0" :precision="1" :step="5" style="width:100%" size="small" /></el-form-item>
                <el-button type="primary" size="small" @click="saveGoals" style="width:100%">保存目标</el-button>
              </el-form>
            </div>
          </div>

          <!-- Meal Types -->
          <div class="side-card">
            <div class="side-card-header">
              <span class="side-card-title">📋 餐型管理</span>
              <el-button text size="small" @click="showMealTypeDialog">管理</el-button>
            </div>
          </div>

          <!-- Quick calories summary -->
          <div class="side-card" v-if="records.length > 0">
            <div class="side-card-header">
              <span class="side-card-title">📊 今日汇总</span>
            </div>
            <div class="side-summary">
              <div class="side-sum-item"><span>热量</span><strong>{{ summary.calories }}kcal</strong></div>
              <div class="side-sum-item"><span>蛋白</span><strong>{{ summary.protein.toFixed(1) }}g</strong></div>
              <div class="side-sum-item"><span>碳水</span><strong>{{ summary.carbs.toFixed(1) }}g</strong></div>
              <div class="side-sum-item"><span>脂肪</span><strong>{{ summary.fat.toFixed(1) }}g</strong></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Food Dialog -->
    <el-dialog v-model="foodDialogVisible" :title="editingId ? '编辑食物' : '添加食物'" width="600px" @closed="resetFoodForm">
      <el-tabs v-if="!editingId" v-model="foodDialogTab">
        <el-tab-pane label="🔍 搜索食物" name="search">
          <div class="search-box">
            <el-input v-model="foodSearchKeyword" placeholder="搜索食物名称..." @keyup.enter="doFoodSearch" clearable @clear="foodSearchResults=[];selectedFood=null" :prefix-icon="null"><template #append><el-button @click="doFoodSearch" :loading="foodSearching" icon="Search" /></template></el-input>
          </div>
          <div class="quick-tags" v-if="foodSearchResults.length === 0 && !foodSearching">
            <span class="quick-tag" v-for="qf in quickFoods" :key="qf" @click="foodSearchKeyword=qf;doFoodSearch()">{{ qf }}</span>
          </div>
          <div class="food-results" v-if="foodSearchResults.length > 0">
            <div v-for="f in foodSearchResults" :key="f.id" class="food-result-item" :class="{ selected: selectedFood?.id === f.id }" @click="selectFood(f)">
              <div class="result-main">
                <span class="result-name">{{ f.name }}</span>
                <el-tag size="small" type="info" effect="plain">{{ f.source === 'USDA' ? 'USDA' : f.source === 'CUSTOM' ? '自定义' : '本地' }}</el-tag>
              </div>
              <div class="result-nutrition">
                <span>热量 {{ f.calories }}kcal</span>
                <span>蛋白 {{ f.protein }}g</span>
                <span>碳水 {{ f.carbs }}g</span>
                <span>脂肪 {{ f.fat }}g</span>
                <span class="per100">(每100g)</span>
              </div>
            </div>
          </div>
          <div v-if="selectedFood" class="weight-section">
            <el-divider />
            <div class="selected-food-label">已选：{{ selectedFood.name }}</div>
            <el-form :inline="true" size="default">
              <el-form-item label="食用重量"><el-input-number v-model="foodWeight" :min="1" :max="5000" :step="10" style="width:150px" /><span style="margin-left:6px;color:#999">g</span></el-form-item>
            </el-form>
            <div class="macro-preview" v-if="foodWeight > 0">
              <div class="preview-item" style="color:#667eea"><span class="preview-label">预估热量</span><span class="preview-value">{{ previewCalories }} kcal</span></div>
              <div class="preview-item" style="color:#67c23a"><span class="preview-label">蛋白质</span><span class="preview-value">{{ previewProtein }}g</span></div>
              <div class="preview-item" style="color:#e6a23c"><span class="preview-label">碳水</span><span class="preview-value">{{ previewCarbs }}g</span></div>
              <div class="preview-item" style="color:#f56c6c"><span class="preview-label">脂肪</span><span class="preview-value">{{ previewFat }}g</span></div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="✏️ 快速录入" name="quick">
          <el-form :model="quickForm" :rules="quickRules" ref="quickFormRef" label-width="90px">
            <el-form-item label="食物名称" prop="foodName"><el-input v-model="quickForm.foodName" placeholder="如：鸡胸肉、米饭" /></el-form-item>
            <el-row :gutter="12">
              <el-col :span="12"><el-form-item label="热量(kcal)"><el-input-number v-model="quickForm.calories" :min="0" :step="50" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="蛋白质(g)"><el-input-number v-model="quickForm.protein" :min="0" :precision="1" :step="5" style="width:100%" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12"><el-form-item label="碳水(g)"><el-input-number v-model="quickForm.carbs" :min="0" :precision="1" :step="10" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="脂肪(g)"><el-input-number v-model="quickForm.fat" :min="0" :precision="1" :step="5" style="width:100%" /></el-form-item></el-col>
            </el-row>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <el-form v-if="editingId" :model="quickForm" :rules="quickRules" ref="quickFormRef" label-width="90px">
        <el-form-item label="食物名称" prop="foodName"><el-input v-model="quickForm.foodName" placeholder="如：鸡胸肉、米饭" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="热量(kcal)"><el-input-number v-model="quickForm.calories" :min="0" :step="50" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="蛋白质(g)"><el-input-number v-model="quickForm.protein" :min="0" :precision="1" :step="5" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="碳水(g)"><el-input-number v-model="quickForm.carbs" :min="0" :precision="1" :step="10" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="脂肪(g)"><el-input-number v-model="quickForm.fat" :min="0" :precision="1" :step="5" style="width:100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="foodDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveFood" :loading="saving">{{ editingId ? '保存修改' : '添加到 ' + allMealTypes.find(m=>m.code===activeMealType)?.label }}</el-button>
      </template>
    </el-dialog>

    <!-- Meal Type Dialog -->
    <el-dialog v-model="mealTypeDialogVisible" title="管理餐型" width="480px">
      <div class="meal-type-list">
        <div v-for="mt in allMealTypes" :key="mt.code" class="meal-type-row">
          <span class="meal-type-name">{{ mt.label }}</span>
          <el-switch v-if="!mt.isDefault" v-model="mt.isActive" active-text="启用" inactive-text="停用" @change="toggleMealType(mt)" />
          <el-tag v-else size="small" type="info">系统默认</el-tag>
          <el-button v-if="!mt.isDefault" text type="danger" size="small" @click="removeMealType(mt)">删除</el-button>
        </div>
      </div>
      <div class="add-meal-type">
        <el-input v-model="newMealTypeName" placeholder="新餐型名称，如：练后餐" size="small" style="width:180px" />
        <el-button size="small" type="primary" @click="addMealType" :disabled="!newMealTypeName.trim()">添加</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import MacroRing from '@/components/MacroRing.vue'
import {
  addDietRecord, getDietRecords, updateDietRecord, deleteDietRecord,
  searchFoods, getMealTypes, createMealType, updateMealType, deleteMealType,
} from '@/api/diet'

const records = ref([])
const loading = ref(false)
const saving = ref(false)
const selectedDate = ref(new Date().toISOString().slice(0, 10))

const goals = reactive(loadGoals())
function loadGoals() {
  try { return JSON.parse(localStorage.getItem('diet_goals')) || { calories: null, protein: null, carbs: null, fat: null } }
  catch { return { calories: null, protein: null, carbs: null, fat: null } }
}
function saveGoals() {
  localStorage.setItem('diet_goals', JSON.stringify({ calories: goals.calories, protein: goals.protein, carbs: goals.carbs, fat: goals.fat }))
  ElMessage.success('目标已保存')
}

const showGoals = ref(false)
const summary = computed(() => ({
  calories: records.value.reduce((s, r) => s + (r.calories || 0), 0),
  protein: records.value.reduce((s, r) => s + Number(r.protein || 0), 0),
  carbs: records.value.reduce((s, r) => s + Number(r.carbs || 0), 0),
  fat: records.value.reduce((s, r) => s + Number(r.fat || 0), 0),
}))
const statCards = computed(() => [
  { label: '总热量', value: summary.value.calories, rawValue: summary.value.calories, unit: 'kcal', color: '#667eea', goalKey: 'calories', decimals: 0, percent: goals.calories ? Math.min(100, Math.round(summary.value.calories / goals.calories * 100)) : 0 },
  { label: '蛋白质', value: summary.value.protein.toFixed(1), rawValue: summary.value.protein, unit: 'g', color: '#67c23a', goalKey: 'protein', decimals: 1, percent: goals.protein ? Math.min(100, Math.round(summary.value.protein / goals.protein * 100)) : 0 },
  { label: '碳水', value: summary.value.carbs.toFixed(1), rawValue: summary.value.carbs, unit: 'g', color: '#e6a23c', goalKey: 'carbs', decimals: 1, percent: goals.carbs ? Math.min(100, Math.round(summary.value.carbs / goals.carbs * 100)) : 0 },
  { label: '脂肪', value: summary.value.fat.toFixed(1), rawValue: summary.value.fat, unit: 'g', color: '#f56c6c', goalKey: 'fat', decimals: 1, percent: goals.fat ? Math.min(100, Math.round(summary.value.fat / goals.fat * 100)) : 0 },
])

const DEFAULT_MEALS = [
  { code: 'BREAKFAST', label: '🌅 早餐', isDefault: true, isActive: true },
  { code: 'LUNCH', label: '☀️ 午餐', isDefault: true, isActive: true },
  { code: 'DINNER', label: '🌙 晚餐', isDefault: true, isActive: true },
  { code: 'SNACK', label: '🍪 加餐', isDefault: true, isActive: true },
]
const customMealTypes = ref([])
const activeMealType = ref('BREAKFAST')
const allMealTypes = computed(() => {
  const customs = customMealTypes.value.filter(m => m.isActive).map(m => ({ code: m.code, label: m.name, isDefault: false, id: m.id, isActive: m.isActive }))
  return [...DEFAULT_MEALS.filter(m => m.isActive), ...customs]
})

const currentMealItems = computed(() => records.value.filter(r => r.mealType === activeMealType.value))
function mealCount(code) {
  let count = records.value.filter(r => r.mealType === code).length
  if (count === 0) return 0
  const cal = records.value.filter(r => r.mealType === code).reduce((s, r) => s + (r.calories || 0), 0)
  return `${cal}kcal`
}
function onTabChange() {}

const foodDialogVisible = ref(false)
const foodDialogTab = ref('search')
const editingId = ref(null)
const foodSearchKeyword = ref('')
const foodSearching = ref(false)
const foodSearchResults = ref([])
const selectedFood = ref(null)
const foodWeight = ref(200)

const quickFoods = ['鸡胸肉', '白米饭', '鸡蛋', '西兰花', '全麦面包', '三文鱼', '牛肉', '牛奶']

const quickForm = reactive({ foodName: '', calories: null, protein: null, carbs: null, fat: null })
const quickFormRef = ref(null)
const quickRules = { foodName: [{ required: true, message: '请输入食物名称' }] }

const previewCalories = computed(() => { if (!selectedFood.value?.calories || !foodWeight.value) return 0; return Math.round(selectedFood.value.calories * foodWeight.value / 100) })
const previewProtein = computed(() => { if (!selectedFood.value?.protein || !foodWeight.value) return '0.0'; return (selectedFood.value.protein * foodWeight.value / 100).toFixed(1) })
const previewCarbs = computed(() => { if (!selectedFood.value?.carbs || !foodWeight.value) return '0.0'; return (selectedFood.value.carbs * foodWeight.value / 100).toFixed(1) })
const previewFat = computed(() => { if (!selectedFood.value?.fat || !foodWeight.value) return '0.0'; return (selectedFood.value.fat * foodWeight.value / 100).toFixed(1) })

async function doFoodSearch() {
  if (!foodSearchKeyword.value.trim()) return
  foodSearching.value = true
  try { const res = await searchFoods(foodSearchKeyword.value.trim()); if (res.code === 200) foodSearchResults.value = res.data || [] }
  catch { ElMessage.error('搜索失败') }
  finally { foodSearching.value = false }
}

function selectFood(f) { selectedFood.value = f }

function openAddDialog() {
  editingId.value = null; foodDialogTab.value = 'search'; foodSearchKeyword.value = ''; foodSearchResults.value = []; selectedFood.value = null; foodWeight.value = 200
  quickForm.foodName = ''; quickForm.calories = null; quickForm.protein = null; quickForm.carbs = null; quickForm.fat = null
  foodDialogVisible.value = true
}

function openEditDialog(item) {
  editingId.value = item.id; foodDialogTab.value = 'quick'
  quickForm.foodName = item.foodName || ''; quickForm.calories = item.calories; quickForm.protein = item.protein; quickForm.carbs = item.carbs; quickForm.fat = item.fat
  foodDialogVisible.value = true
}

function resetFoodForm() { editingId.value = null; selectedFood.value = null; foodSearchResults.value = [] }

async function handleSaveFood() {
  saving.value = true
  try {
    let data = { mealType: activeMealType.value, recordDate: selectedDate.value }
    if (!editingId.value && foodDialogTab.value === 'search' && selectedFood.value) { data.foodId = selectedFood.value.id; data.foodName = selectedFood.value.name; data.weightGrams = foodWeight.value }
    else if (!editingId.value && foodDialogTab.value === 'quick') { const valid = await quickFormRef.value?.validate().catch(() => false); if (!valid) { saving.value = false; return }; data.foodName = quickForm.foodName; data.calories = quickForm.calories; data.protein = quickForm.protein; data.carbs = quickForm.carbs; data.fat = quickForm.fat }
    else if (editingId.value) { const valid = await quickFormRef.value?.validate().catch(() => false); if (!valid) { saving.value = false; return }; data.foodName = quickForm.foodName; data.calories = quickForm.calories; data.protein = quickForm.protein; data.carbs = quickForm.carbs; data.fat = quickForm.fat }
    let res; if (editingId.value) res = await updateDietRecord(editingId.value, data); else res = await addDietRecord(data)
    if (res.code === 200) { ElMessage.success(editingId.value ? '已更新' : '已添加'); foodDialogVisible.value = false; fetchRecords() }
  } catch (e) { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

const mealTypeDialogVisible = ref(false)
const newMealTypeName = ref('')

function showMealTypeDialog() { mealTypeDialogVisible.value = true }

async function fetchMealTypes() {
  try { const res = await getMealTypes(); if (res.code === 200) customMealTypes.value = res.data || [] }
  catch { /* ignore */ }
}

async function addMealType() {
  const name = newMealTypeName.value.trim(); if (!name) return
  try { const code = 'MEAL_' + Date.now(); const res = await createMealType({ name, code, isActive: 1, sortOrder: customMealTypes.value.length }); if (res.code === 200) { ElMessage.success(`餐型「${name}」已添加`); newMealTypeName.value = ''; await fetchMealTypes() } }
  catch { ElMessage.error('添加失败') }
}

async function toggleMealType(mt) {
  try { await updateMealType(mt.id, { name: mt.label, code: mt.code, isActive: mt.isActive ? 1 : 0, sortOrder: 0 }) }
  catch { ElMessage.error('更新失败') }
}

async function removeMealType(mt) {
  try { await deleteMealType(mt.id); ElMessage.success(`已删除「${mt.label}」`); await fetchMealTypes(); if (activeMealType.value === mt.code) activeMealType.value = 'BREAKFAST' }
  catch { ElMessage.error('删除失败') }
}

onMounted(() => { fetchRecords(); fetchMealTypes() })

async function fetchRecords() {
  loading.value = true
  try { const res = await getDietRecords(selectedDate.value); if (res.code === 200) records.value = res.data.records || [] }
  catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleDelete(item) {
  try { const res = await deleteDietRecord(item.id); if (res.code === 200) { ElMessage.success('已删除'); fetchRecords() } }
  catch { ElMessage.error('删除失败') }
}
</script>

<style scoped>
/* ---- Layout ---- */
.diet-page {
  margin: calc(-1 * var(--space-lg)) calc(-1 * var(--page-padding-x)) calc(-1 * var(--space-2xl));
}

/* ---- Hero Banner ---- */
.diet-hero {
  position: relative;
  left: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  width: 100vw;
  padding: 72px 0 48px;
  background: url(https://pic2.zhimg.com/v2-62de6e66e20d8acb194eceaef2d78a7b_1440w.jpg?source=172ae18b) center / cover no-repeat;
  background-attachment: fixed;
  isolation: isolate;
  text-align: center;
}
.diet-hero-overlay {
  position: absolute; inset: 0; z-index: 0;
  background: rgba(0, 0, 0, 0.75);
}
.diet-hero-content {
  position: relative; z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--page-padding-x);
}
.diet-hero-title {
  font-size: 36px;
  font-weight: var(--font-weight-bold);
  color: #FFFFFF;
  margin: 0 0 24px;
}

/* Hero rings */
.hero-rings {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
.hero-ring {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.hero-ring :deep(.ring-value) { color: #FFFFFF; }
.hero-ring :deep(.ring-unit)  { color: rgba(255,255,255,0.65); }
.hero-ring :deep(.ring-label) { color: rgba(255,255,255,0.5); }
.hero-ring :deep(.ring-svg circle:first-child) { stroke: rgba(255,255,255,0.12); }
.hero-ring-goal {
  font-size: 11px;
  color: rgba(255,255,255,0.5);
}
.hero-ring-goal.none {
  color: rgba(255,255,255,0.25);
}

/* Hero date picker */
.hero-date :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 20px;
  box-shadow: none;
}
.hero-date :deep(.el-input__inner) { color: rgba(255,255,255,0.8); }
.hero-date :deep(.el-input__wrapper:hover) { border-color: rgba(255,255,255,0.4); }

/* ---- Container ---- */
.diet-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-xl) var(--page-padding-x) var(--space-2xl);
}
.card-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-title);
}

/* ---- Layout ---- */
.diet-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}
@media (max-width: 767px) {
  .diet-layout { grid-template-columns: 1fr; }
}

/* ---- Main Card ---- */
.meal-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.meal-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

/* Food list */
.food-list { min-height: 60px; }
.food-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px; margin-bottom: 4px; border-radius: 8px;
  cursor: pointer; transition: all var(--transition-fast); border: 1px solid transparent;
}
.food-item:hover { background: var(--color-bg-light); border-color: var(--color-border); }
.food-left { display: flex; flex-direction: column; gap: 4px; flex: 1; }
.food-name-row { display: flex; align-items: center; gap: 8px; }
.food-name { font-size: 14px; font-weight: 500; color: var(--color-text-title); }
.food-meta { font-size: 12px; color: var(--color-text-secondary); }
.del-btn { opacity: 0; transition: opacity var(--transition-fast); }
.food-item:hover .del-btn { opacity: 1; }

/* ---- Side Cards ---- */
.side-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  margin-bottom: 16px;
}
.side-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
}
.side-card-title { font-size: 14px; font-weight: 600; color: var(--color-text-title); }
.side-goal-form { margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--color-border); }
.side-summary { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 8px; }
.side-sum-item { font-size: 13px; color: var(--color-text-secondary); display: flex; justify-content: space-between; }

/* ---- Food Dialog ---- */
.search-box { margin-bottom: 12px; }
.quick-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.quick-tag { padding: 4px 14px; background: var(--color-bg-light); border-radius: 16px; font-size: 13px; color: var(--color-text-secondary); cursor: pointer; transition: all var(--transition-fast); }
.quick-tag:hover { background: var(--color-brand); color: #fff; }
.food-results { margin-bottom: 8px; }
.food-result-item { padding: 12px; border-radius: 8px; cursor: pointer; margin-bottom: 4px; transition: all var(--transition-fast); border: 1px solid transparent; }
.food-result-item:hover { background: var(--color-bg-light); }
.food-result-item.selected { background: var(--color-brand-light); border-color: var(--color-brand); }
.result-main { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.result-name { font-weight: 500; }
.result-nutrition { display: flex; gap: 12px; font-size: 12px; color: var(--color-text-secondary); flex-wrap: wrap; }
.per100 { color: var(--color-text-weak); }
.weight-section { margin-top: 4px; }
.selected-food-label { font-size: 15px; font-weight: 500; color: var(--color-text-title); margin-bottom: 12px; }
.macro-preview { display: flex; gap: 16px; flex-wrap: wrap; }
.preview-item { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 8px 16px; background: var(--color-bg-light); border-radius: 8px; min-width: 90px; }
.preview-label { font-size: 12px; color: var(--color-text-secondary); }
.preview-value { font-size: 16px; font-weight: 600; }

/* Meal type dialog */
.meal-type-list { margin-bottom: 16px; }
.meal-type-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid var(--color-border-light); }
.meal-type-name { flex: 1; font-size: 14px; font-weight: 500; }
.add-meal-type { display: flex; align-items: center; gap: 8px; }
</style>
