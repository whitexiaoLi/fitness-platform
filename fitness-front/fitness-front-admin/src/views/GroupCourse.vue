<template>
  <div class="group-course">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" size="default">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option label="即将开始" value="UPCOMING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="showCreateDialog">添加排课</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="classes" stripe v-loading="loading" size="default">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="courseId" label="课程ID" width="80" />
        <el-table-column prop="coachId" label="教练ID" width="80" />
        <el-table-column label="时间" min-width="280">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="容量" width="120">
          <template #default="{ row }">{{ row.currentEnrollment || 0 }} / {{ row.maxCapacity }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="140" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)" :disabled="row.status === 'CANCELLED'">编辑</el-button>
            <el-popconfirm
              title="确认取消该排课？"
              @confirm="handleCancel(row)"
              v-if="row.status === 'UPCOMING'"
            >
              <template #reference>
                <el-button size="small" type="danger">取消</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑排课' : '添加排课'" width="500px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程ID" prop="courseId">
          <el-input-number v-model="form.courseId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="教练ID" prop="coachId">
          <el-input-number v-model="form.coachId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大容量" prop="maxCapacity">
          <el-input-number v-model="form.maxCapacity" :min="1" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" placeholder="如：A区操厅" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const classes = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filters = reactive({ status: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editId = ref(null)
const formRef = ref(null)

const emptyForm = () => ({
  courseId: null, coachId: null, startTime: '', endTime: '', maxCapacity: 30, location: '',
})
const form = reactive(emptyForm())
const rules = {
  courseId: [{ required: true, message: '请输入课程ID' }],
  coachId: [{ required: true, message: '请输入教练ID' }],
  startTime: [{ required: true, message: '请选择开始时间' }],
  endTime: [{ required: true, message: '请选择结束时间' }],
  maxCapacity: [{ required: true, message: '请输入最大容量' }],
}

const statusTag = (s) => ({ UPCOMING: 'primary', IN_PROGRESS: 'success', COMPLETED: 'info', CANCELLED: 'danger' }[s] || 'info')
const statusLabel = (s) => ({ UPCOMING: '即将开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s)

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ')
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.status) params.status = filters.status
    const res = await request.get('/group-classes', { params })
    if (res.code === 200) {
      classes.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; fetchData() }
function handleReset() { filters.status = ''; page.value = 1; fetchData() }

function showCreateDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  form.courseId = row.courseId
  form.coachId = row.coachId
  form.startTime = row.startTime
  form.endTime = row.endTime
  form.maxCapacity = row.maxCapacity
  form.location = row.location || ''
  dialogVisible.value = true
}

function resetForm() {
  Object.assign(form, emptyForm())
  formRef.value?.resetFields()
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = {
      courseId: form.courseId,
      coachId: form.coachId,
      startTime: form.startTime,
      endTime: form.endTime,
      maxCapacity: form.maxCapacity,
      location: form.location,
    }
    let res
    if (isEdit.value) {
      res = await request.put(`/group-classes/${editId.value}`, data)
    } else {
      res = await request.post('/group-classes', data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

async function handleCancel(row) {
  try {
    await request.put(`/group-classes/${row.id}/cancel`)
    ElMessage.success('已取消该排课')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px 20px 0; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
