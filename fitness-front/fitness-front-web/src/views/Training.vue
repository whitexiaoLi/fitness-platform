<template>
  <div class="training">
    <h2 class="page-title">🏃 训练记录</h2>
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8" v-for="s in stats" :key="s.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header><span>添加训练记录</span></template>
      <el-form :inline="true" :model="addForm" size="default">
        <el-form-item label="日期">
          <el-date-picker v-model="addForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item label="时长(分)">
          <el-input-number v-model="addForm.duration" :min="1" :max="300" style="width: 130px" />
        </el-form-item>
        <el-form-item label="消耗热量">
          <el-input-number v-model="addForm.calories" :min="0" :max="5000" style="width: 130px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.notes" placeholder="训练内容" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAdd" :loading="adding">记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top: 16px" class="filter-card">
      <el-form :inline="true" size="default">
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 260px" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchRecords">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top: 16px">
      <el-table :data="records" stripe v-loading="loading" size="default">
        <template #empty><el-empty description="暂无训练记录" /></template>
        <el-table-column label="日期" width="120"><template #default="{ row }">{{ row.recordDate }}</template></el-table-column>
        <el-table-column label="时长" width="100"><template #default="{ row }">{{ row.duration || '-' }} 分钟</template></el-table-column>
        <el-table-column label="热量" width="100"><template #default="{ row }">{{ row.calories || '-' }} kcal</template></el-table-column>
        <el-table-column prop="notes" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="记录时间" width="170"><template #default="{ row }">{{ row.createTime?.replace('T', ' ') }}</template></el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetchRecords" @size-change="fetchRecords" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const records = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dateRange = ref([])
const adding = ref(false)

const addForm = reactive({ recordDate: new Date().toISOString().slice(0, 10), duration: null, calories: null, notes: '' })

const stats = computed(() => [
  { label: '训练次数', value: total.value, color: '#667eea' },
  { label: '总时长(分)', value: records.value.reduce((s, r) => s + (r.duration || 0), 0), color: '#67c23a' },
  { label: '总消耗(kcal)', value: records.value.reduce((s, r) => s + (r.calories || 0), 0), color: '#e6a23c' },
])

onMounted(() => fetchRecords())

async function fetchRecords() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (dateRange.value?.length === 2) { params.start = dateRange.value[0]; params.end = dateRange.value[1] }
    const res = await request.get('/training/records', { params })
    if (res.code === 200) { records.value = res.data.records; total.value = res.data.total }
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleAdd() {
  adding.value = true
  try {
    const res = await request.post('/training/records', { ...addForm })
    if (res.code === 200) { ElMessage.success('已记录'); addForm.notes = ''; fetchRecords() }
  } catch (e) { ElMessage.error('记录失败') }
  finally { adding.value = false }
}
</script>

<style scoped>
.page-title { margin: 0 0 16px; font-size: 20px; }
.stats-row { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-card :deep(.el-card__body) { padding: 20px; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.filter-card :deep(.el-card__body) { padding: 16px 20px 0; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
