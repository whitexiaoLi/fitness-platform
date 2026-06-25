<template>
  <div class="member-metrics">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" size="default">
        <el-form-item label="用户ID">
          <el-input
            v-model="filters.userId"
            placeholder="输入用户ID"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="records" stripe v-loading="loading" size="default">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="体重 (kg)" width="100">
          <template #default="{ row }">{{ row.weight ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="体脂 (%)" width="100">
          <template #default="{ row }">{{ row.bodyFat ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="BMI" width="80">
          <template #default="{ row }">{{ row.bmi ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="腰围 (cm)" width="100">
          <template #default="{ row }">{{ row.waist ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="臀围 (cm)" width="100">
          <template #default="{ row }">{{ row.hip ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="记录日期" width="130">
          <template #default="{ row }">{{ row.recordDate }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const records = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const filters = reactive({ userId: '' })

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ')
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.userId) params.userId = filters.userId
    const res = await request.get('/metrics', { params })
    if (res.code === 200) {
      records.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  filters.userId = ''
  page.value = 1
  fetchData()
}
</script>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px 20px 0; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
