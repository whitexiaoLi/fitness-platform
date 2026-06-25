<template>
  <div class="private-coach">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" size="default">
        <el-form-item label="教练ID">
          <el-input-number v-model="filters.coachId" :min="1" placeholder="全部" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="学员ID">
          <el-input-number v-model="filters.studentId" :min="1" placeholder="全部" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="showCreateDialog">新建绑定</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="bindings" stripe v-loading="loading" size="default">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="coachId" label="教练ID" width="100" />
        <el-table-column prop="studentId" label="学员ID" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始日期" width="130">
          <template #default="{ row }">{{ row.startDate }}</template>
        </el-table-column>
        <el-table-column label="结束日期" width="130">
          <template #default="{ row }">{{ row.endDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-popconfirm
              v-if="row.status === 'ACTIVE'"
              title="确认结束该绑定？"
              @confirm="handleEnd(row)"
            >
              <template #reference>
                <el-button size="small" type="danger">结束</el-button>
              </template>
            </el-popconfirm>
          </template>
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

    <el-dialog v-model="dialogVisible" title="新建教练-学员绑定" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="教练ID" prop="coachId">
          <el-input-number v-model="form.coachId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学员ID" prop="studentId">
          <el-input-number v-model="form.studentId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const bindings = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const filters = reactive({ coachId: null, studentId: null, status: '' })

const dialogVisible = ref(false)
const creating = ref(false)
const formRef = ref(null)
const form = reactive({ coachId: null, studentId: null })
const rules = {
  coachId: [{ required: true, message: '请输入教练ID' }],
  studentId: [{ required: true, message: '请输入学员ID' }],
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.coachId) params.coachId = filters.coachId
    if (filters.studentId) params.studentId = filters.studentId
    if (filters.status) params.status = filters.status
    const res = await request.get('/coach-students', { params })
    if (res.code === 200) {
      bindings.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; fetchData() }
function handleReset() {
  filters.coachId = null
  filters.studentId = null
  filters.status = ''
  page.value = 1
  fetchData()
}

function showCreateDialog() {
  form.coachId = null
  form.studentId = null
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleCreate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    const res = await request.post('/coach-students', { ...form })
    if (res.code === 200) {
      ElMessage.success('绑定创建成功')
      dialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    creating.value = false
  }
}

async function handleEnd(row) {
  try {
    await request.put(`/coach-students/${row.id}/end`)
    ElMessage.success('已结束绑定')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px 20px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
