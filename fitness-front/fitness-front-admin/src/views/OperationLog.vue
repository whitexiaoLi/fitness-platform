<template>
  <div class="operation-log">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" size="default">
        <el-form-item label="搜索">
          <el-input
            v-model="filters.keyword"
            placeholder="用户名 / 操作类型 / 目标"
            clearable
            style="width: 260px"
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
        <el-table-column prop="username" label="操作人" width="130" />
        <el-table-column label="操作类型" width="130">
          <template #default="{ row }">
            <el-tag :type="actionTag(row.action)" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="操作目标" min-width="180" show-overflow-tooltip />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column label="操作时间" width="170">
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

const filters = reactive({ keyword: '' })

const actionTag = (a) => {
  if (!a) return 'info'
  if (a.includes('LOGIN')) return 'success'
  if (a.includes('UPDATE') || a.includes('EDIT')) return 'warning'
  if (a.includes('DELETE') || a.includes('REJECT')) return 'danger'
  if (a.includes('APPROVE')) return 'success'
  return 'info'
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ')
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.keyword) params.keyword = filters.keyword
    const res = await request.get('/logs', { params })
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
  filters.keyword = ''
  page.value = 1
  fetchData()
}
</script>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px 20px 0; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
