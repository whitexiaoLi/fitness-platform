<template>
  <div class="diet">
    <h2 class="page-title">饮食记录</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <MacroRing
              :value="s.rawValue"
              :max="goals[s.goalKey] || s.rawValue * 2 || 100"
              :label="s.label"
              :unit="s.unit"
              :color="s.color"
              :decimals="s.decimals"
            />
            <div class="stat-goal" v-if="goals[s.goalKey]">
              目标 {{ goals[s.goalKey] }}{{ s.unit }}
            </div>
            <div class="stat-goal" v-else style="visibility:hidden">-</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 营养目标 -->
    <el-card shadow="hover" class="goal-card">
      <div class="goal-header" @click="showGoals = !showGoals">
        <span>🎯 每日营养目标</span>
        <el-icon><component :is="showGoals ? ArrowUp : ArrowDown" /></el-icon>
      </div>
      <el-collapse-transition>
        <el-form v-show="showGoals" :inline="true" size="default" class="goal-form">
          <el-form-item label="热量(kcal)">
            <el-input-number v-model="goals.calories" :min="0" :step="100" style="width:130px" />
          </el-form-item>
          <el-form-item label="蛋白质(g)">
            <el-input-number v-model="goals.protein" :min="0" :precision="1" :step="10" style="width:130px" />
          </el-form-item>
          <el-form-item label="碳水(g)">
            <el-input-number v-model="goals.carbs" :min="0" :precision="1" :step="10" style="width:130px" />
          </el-form-item>
          <el-form-item label="脂肪(g)">
            <el-input-number v-model="goals.fat" :min="0" :precision="1" :step="5" style="width:130px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveGoals" size="small">保存目标</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-transition>
    </el-card>

    <!-- 餐型 Tab 切换 -->
    <el-card shadow="hover" style="margin-top:16px" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <el-date-picker v-model="selectedDate" type="date" value-format="YYYY-MM-DD" @change="fetchRecords" style="width:150px" />
          </div>
          <div class="card-header-right">
            <el-button @click="showMealTypeDialog">管理餐型</el-button>
            <el-button type="primary" @click="openAddDialog">+ 添加食物</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeMealType" @tab-change="onTabChange" type="card">
        <el-tab-pane v-for="mt in allMealTypes" :key="mt.code" :name="mt.code">
          <template #label>
            <span>{{ mt.label }} <el-tag size="small" type="info" style="margin-left:4px">{{ mealCount(mt.code) }}</el-tag></span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 当前餐型食物列表 -->
      <div v-if="currentMealItems.length === 0 && !loading" style="padding:20px 0">
        <el-empty description="该餐型暂无记录" :image-size="80" />
      </div>

      <div v-for="item in currentMealItems" :key="item.id" class="food-item" @click="openEditDialog(item)">
        <div class="food-left">
          <div class="food-name-row">
            <span class="food-name">{{ item.foodName }}</span>
            <el-tag v-if="item.weightGrams" size="small" type="info" effect="plain">{{ item.weightGrams }}g</el-tag>
          </div>
          <span class="food-meta">{{ item.calories || 0 }} kcal · 蛋白 {{ item.protein || 0 }}g · 碳水 {{ item.carbs || 0 }}g · 脂肪 {{ item.fat || 0 }}g</span>
        </div>
        <div class="food-right" @click.stop>
          <el-popconfirm title="确定删除该记录？" @confirm="handleDelete(item)">
            <template #reference>
              <el-button text type="danger" size="small" class="del-btn">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </el-card>

    <!-- 食物选择对话框 -->
    <el-dialog v-model="foodDialogVisible" :title="editingId ? '编辑食物' : '添加食物'" width="620px" @closed="resetFoodForm">
      <!-- 新增模式：搜索 + 快速录入双 Tab -->
      <el-tabs v-model="foodDialogTab" v-if="!editingId">
        <el-tab-pane label="🔍 搜索食物" name="search">
          <div class="search-box">
            <el-input v-model="foodSearchKeyword" placeholder="输入食物名称搜索..." clearable @keyup.enter="doFoodSearch" @clear="foodSearchResults=[]">
              <template #append>
                <el-button @click="doFoodSearch" :loading="foodSearching">搜索</el-button>
              </template>
            </el-input>
          </div>

          <div class="quick-tags" v-if="foodSearchResults.length === 0 && !foodSearching">
            <span class="quick-tag" v-for="t in quickFoods" :key="t" @click="foodSearchKeyword=t;doFoodSearch()">{{ t }}</span>
          </div>

          <div class="search-results" v-if="foodSearchResults.length > 0">
            <div
              v-for="f in foodSearchResults" :key="f.id"
              class="food-result-item"
              :class="{ selected: selectedFood?.id === f.id }"
              @click="selectFood(f)"
            >
              <div class="result-main">
                <span class="result-name">{{ f.name }}</span>
                <el-tag size="small" effect="plain">{{ f.source === 'USDA' ? 'USDA' : f.source === 'CUSTOM' ? '自定义' : '本地' }}</el-tag>
              </div>
              <div class="result-nutrition">
                <span>{{ f.calories }} kcal</span>
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
              <el-form-item label="食用重量">
                <el-input-number v-model="foodWeight" :min="1" :max="5000" :step="10" style="width:150px" />
                <span style="margin-left:6px;color:#999">g</span>
              </el-form-item>
            </el-form>
            <div class="macro-preview" v-if="foodWeight > 0">
              <div class="preview-item" style="color:#667eea">
                <span class="preview-label">预估热量</span><span class="preview-value">{{ previewCalories }} kcal</span>
              </div>
              <div class="preview-item" style="color:#67c23a">
                <span class="preview-label">蛋白质</span><span class="preview-value">{{ previewProtein }}g</span>
              </div>
              <div class="preview-item" style="color:#e6a23c">
                <span class="preview-label">碳水</span><span class="preview-value">{{ previewCarbs }}g</span>
              </div>
              <div class="preview-item" style="color:#f56c6c">
                <span class="preview-label">脂肪</span><span class="preview-value">{{ previewFat }}g</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="✏️ 快速录入" name="quick">
          <el-form :model="quickForm" :rules="quickRules" ref="quickFormRef" label-width="90px">
            <el-form-item label="食物名称" prop="foodName">
              <el-input v-model="quickForm.foodName" placeholder="如：鸡胸肉、米饭" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="热量(kcal)">
                  <el-input-number v-model="quickForm.calories" :min="0" :step="50" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="蛋白质(g)">
                  <el-input-number v-model="quickForm.protein" :min="0" :precision="1" :step="5" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="碳水(g)">
                  <el-input-number v-model="quickForm.carbs" :min="0" :precision="1" :step="10" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="脂肪(g)">
                  <el-input-number v-model="quickForm.fat" :min="0" :precision="1" :step="5" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 编辑模式：直接显示表单 -->
      <el-form v-if="editingId" :model="quickForm" :rules="quickRules" ref="quickFormRef" label-width="90px">
        <el-form-item label="食物名称" prop="foodName">
          <el-input v-model="quickForm.foodName" placeholder="如：鸡胸肉、米饭" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="热量(kcal)">
              <el-input-number v-model="quickForm.calories" :min="0" :step="50" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="蛋白质(g)">
              <el-input-number v-model="quickForm.protein" :min="0" :precision="1" :step="5" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="碳水(g)">
              <el-input-number v-model="quickForm.carbs" :min="0" :precision="1" :step="10" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="脂肪(g)">
              <el-input-number v-model="quickForm.fat" :min="0" :precision="1" :step="5" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="foodDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveFood" :loading="saving">
          {{ editingId ? '保存修改' : '添加到 ' + allMealTypes.find(m=>m.code===activeMealType)?.label }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 餐型管理对话框 -->
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

// ---- 基础状态 ----
const records = ref([])
const loading = ref(false)
const saving = ref(false)
const selectedDate = ref(new Date().toISOString().slice(0, 10))

// ---- 统计卡片 ----
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
  { label: '总热量', value: summary.value.calories, rawValue: summary.value.calories, unit: 'kcal', color: '#667eea', goalKey: 'calories', decimals: 0,
    percent: goals.calories ? Math.min(100, Math.round(summary.value.calories / goals.calories * 100)) : 0 },
  { label: '蛋白质', value: summary.value.protein.toFixed(1), rawValue: summary.value.protein, unit: 'g', color: '#67c23a', goalKey: 'protein', decimals: 1,
    percent: goals.protein ? Math.min(100, Math.round(summary.value.protein / goals.protein * 100)) : 0 },
  { label: '碳水', value: summary.value.carbs.toFixed(1), rawValue: summary.value.carbs, unit: 'g', color: '#e6a23c', goalKey: 'carbs', decimals: 1,
    percent: goals.carbs ? Math.min(100, Math.round(summary.value.carbs / goals.carbs * 100)) : 0 },
  { label: '脂肪', value: summary.value.fat.toFixed(1), rawValue: summary.value.fat, unit: 'g', color: '#f56c6c', goalKey: 'fat', decimals: 1,
    percent: goals.fat ? Math.min(100, Math.round(summary.value.fat / goals.fat * 100)) : 0 },
])

// ---- 餐型 Tab ----
const DEFAULT_MEALS = [
  { code: 'BREAKFAST', label: '🌅 早餐', isDefault: true, isActive: true },
  { code: 'LUNCH', label: '☀️ 午餐', isDefault: true, isActive: true },
  { code: 'DINNER', label: '🌙 晚餐', isDefault: true, isActive: true },
  { code: 'SNACK', label: '🍪 加餐', isDefault: true, isActive: true },
]
const customMealTypes = ref([])
const activeMealType = ref('BREAKFAST')
const allMealTypes = computed(() => {
  const customs = customMealTypes.value.filter(m => m.isActive)
    .map(m => ({ code: m.code, label: m.name, isDefault: false, id: m.id, isActive: m.isActive }))
  return [...DEFAULT_MEALS.filter(m => m.isActive), ...customs]
})

const currentMealItems = computed(() => records.value.filter(r => r.mealType === activeMealType.value))
function mealCount(code) {
  let count = records.value.filter(r => r.mealType === code).length
  // Sum calories for the meal
  if (count === 0) return 0
  const cal = records.value.filter(r => r.mealType === code).reduce((s, r) => s + (r.calories || 0), 0)
  return `${cal}kcal`
}
function onTabChange() {}

// ---- 食物选择对话框 ----
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

const previewCalories = computed(() => {
  if (!selectedFood.value?.calories || !foodWeight.value) return 0
  return Math.round(selectedFood.value.calories * foodWeight.value / 100)
})
const previewProtein = computed(() => {
  if (!selectedFood.value?.protein || !foodWeight.value) return '0.0'
  return (selectedFood.value.protein * foodWeight.value / 100).toFixed(1)
})
const previewCarbs = computed(() => {
  if (!selectedFood.value?.carbs || !foodWeight.value) return '0.0'
  return (selectedFood.value.carbs * foodWeight.value / 100).toFixed(1)
})
const previewFat = computed(() => {
  if (!selectedFood.value?.fat || !foodWeight.value) return '0.0'
  return (selectedFood.value.fat * foodWeight.value / 100).toFixed(1)
})

async function doFoodSearch() {
  if (!foodSearchKeyword.value.trim()) return
  foodSearching.value = true
  try {
    const res = await searchFoods(foodSearchKeyword.value.trim())
    if (res.code === 200) foodSearchResults.value = res.data || []
  } catch { ElMessage.error('搜索失败') }
  finally { foodSearching.value = false }
}

function selectFood(f) {
  selectedFood.value = f
}

function openAddDialog() {
  editingId.value = null
  foodDialogTab.value = 'search'
  foodSearchKeyword.value = ''
  foodSearchResults.value = []
  selectedFood.value = null
  foodWeight.value = 200
  quickForm.foodName = ''; quickForm.calories = null; quickForm.protein = null; quickForm.carbs = null; quickForm.fat = null
  foodDialogVisible.value = true
}

function openEditDialog(item) {
  editingId.value = item.id
  foodDialogTab.value = 'quick'
  quickForm.foodName = item.foodName || ''
  quickForm.calories = item.calories
  quickForm.protein = item.protein
  quickForm.carbs = item.carbs
  quickForm.fat = item.fat
  foodDialogVisible.value = true
}

function resetFoodForm() {
  editingId.value = null
  selectedFood.value = null
  foodSearchResults.value = []
}

async function handleSaveFood() {
  saving.value = true
  try {
    let data = { mealType: activeMealType.value, recordDate: selectedDate.value }

    if (!editingId.value && foodDialogTab.value === 'search' && selectedFood.value) {
      // 搜索模式：传 foodId + weightGrams，后端自动算
      data.foodId = selectedFood.value.id
      data.foodName = selectedFood.value.name
      data.weightGrams = foodWeight.value
    } else if (!editingId.value && foodDialogTab.value === 'quick') {
      // 快速录入模式
      const valid = await quickFormRef.value?.validate().catch(() => false)
      if (!valid) { saving.value = false; return }
      data.foodName = quickForm.foodName
      data.calories = quickForm.calories
      data.protein = quickForm.protein
      data.carbs = quickForm.carbs
      data.fat = quickForm.fat
    } else if (editingId.value) {
      // 编辑模式
      const valid = await quickFormRef.value?.validate().catch(() => false)
      if (!valid) { saving.value = false; return }
      data.foodName = quickForm.foodName
      data.calories = quickForm.calories
      data.protein = quickForm.protein
      data.carbs = quickForm.carbs
      data.fat = quickForm.fat
    }

    let res
    if (editingId.value) res = await updateDietRecord(editingId.value, data)
    else res = await addDietRecord(data)

    if (res.code === 200) {
      ElMessage.success(editingId.value ? '已更新' : '已添加')
      foodDialogVisible.value = false
      fetchRecords()
    }
  } catch (e) { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

// ---- 餐型管理 ----
const mealTypeDialogVisible = ref(false)
const newMealTypeName = ref('')

function showMealTypeDialog() { mealTypeDialogVisible.value = true }

async function fetchMealTypes() {
  try {
    const res = await getMealTypes()
    if (res.code === 200) customMealTypes.value = res.data || []
  } catch { /* ignore */ }
}

async function addMealType() {
  const name = newMealTypeName.value.trim()
  if (!name) return
  try {
    const code = 'MEAL_' + Date.now()
    const res = await createMealType({ name, code, isActive: 1, sortOrder: customMealTypes.value.length })
    if (res.code === 200) {
      ElMessage.success(`餐型「${name}」已添加`)
      newMealTypeName.value = ''
      await fetchMealTypes()
    }
  } catch { ElMessage.error('添加失败') }
}

async function toggleMealType(mt) {
  try {
    await updateMealType(mt.id, { name: mt.label, code: mt.code, isActive: mt.isActive ? 1 : 0, sortOrder: 0 })
  } catch { ElMessage.error('更新失败') }
}

async function removeMealType(mt) {
  try {
    await deleteMealType(mt.id)
    ElMessage.success(`已删除「${mt.label}」`)
    await fetchMealTypes()
    if (activeMealType.value === mt.code) activeMealType.value = 'BREAKFAST'
  } catch { ElMessage.error('删除失败') }
}

// ---- CRUD ----
onMounted(() => { fetchRecords(); fetchMealTypes() })

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getDietRecords(selectedDate.value)
    if (res.code === 200) records.value = res.data.records || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleDelete(item) {
  try {
    const res = await deleteDietRecord(item.id)
    if (res.code === 200) { ElMessage.success('已删除'); fetchRecords() }
  } catch { ElMessage.error('删除失败') }
}
</script>

<style scoped>
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; padding: 0; }
.stat-card :deep(.el-card__body) { padding: 16px 8px; }
.stat-card-inner { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.stat-goal { font-size: 11px; color: var(--color-text-weak); }

/* goal card */
.goal-card { margin-bottom: 0; }
.goal-card :deep(.el-card__body) { padding: 12px 20px; }
.goal-header { display: flex; align-items: center; justify-content: space-between; cursor: pointer; font-size: 14px; font-weight: 500; color: var(--color-text-title); user-select: none; }
.goal-form { margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--color-border-light); }

/* main card */
.card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
.card-header-left { display: flex; align-items: center; gap: 8px; }
.card-header-right { display: flex; align-items: center; gap: 8px; }

/* food list */
.food-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px; margin-bottom: 4px; border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition-fast); border: 1px solid transparent;
}
.food-item:hover { background: var(--color-bg-light); border-color: var(--color-border); }
.food-left { display: flex; flex-direction: column; gap: 4px; flex: 1; }
.food-name-row { display: flex; align-items: center; gap: 8px; }
.food-name { font-size: 14px; font-weight: 500; color: var(--color-text-title); }
.food-meta { font-size: 12px; color: var(--color-text-secondary); }
.del-btn { opacity: 0; transition: opacity var(--transition-fast); }
.food-item:hover .del-btn { opacity: 1; }

/* food search dialog */
.search-box { margin-bottom: 12px; }
.quick-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.quick-tag {
  padding: 4px 14px; background: var(--color-bg-light); border-radius: 16px; font-size: 13px;
  color: var(--color-text-secondary); cursor: pointer; transition: all var(--transition-fast);
}
.quick-tag:hover { background: var(--color-brand); color: #fff; }

.food-result-item {
  padding: 12px; border-radius: var(--radius-md); cursor: pointer; margin-bottom: 4px;
  transition: all var(--transition-fast); border: 1px solid transparent;
}
.food-result-item:hover { background: var(--color-bg-light); }
.food-result-item.selected { background: var(--color-brand-light); border-color: var(--color-brand); }
.result-main { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.result-name { font-weight: 500; }
.result-nutrition { display: flex; gap: 12px; font-size: 12px; color: var(--color-text-secondary); flex-wrap: wrap; }
.per100 { color: var(--color-text-weak); }

.weight-section { margin-top: 4px; }
.selected-food-label { font-size: 15px; font-weight: 500; color: var(--color-text-title); margin-bottom: 12px; }

.macro-preview { display: flex; gap: 16px; flex-wrap: wrap; }
.preview-item { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 8px 16px; background: var(--color-bg-light); border-radius: var(--radius-md); min-width: 90px; }
.preview-label { font-size: 12px; color: var(--color-text-secondary); }
.preview-value { font-size: 16px; font-weight: 600; }

/* meal type management */
.meal-type-list { margin-bottom: 16px; }
.meal-type-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid var(--color-border-light); }
.meal-type-name { flex: 1; font-size: 14px; font-weight: 500; }
.add-meal-type { display: flex; align-items: center; gap: 8px; }
</style>
