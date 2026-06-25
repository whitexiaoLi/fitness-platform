<template>
  <div class="member-cards">
    <el-card class="filter-card" shadow="never">
      <el-button type="primary" @click="showCreateDialog">添加卡种</el-button>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="cards" stripe v-loading="loading" size="default">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="卡种名称" min-width="150" />
        <el-table-column label="时长" width="120">
          <template #default="{ row }">{{ row.durationDays }} 天</template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleToggleStatus(row, val ? 1 : 0)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除该卡种？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑卡种' : '添加卡种'" width="480px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="卡种名称" prop="name">
          <el-input v-model="form.name" placeholder="如：月卡、季卡、年卡" />
        </el-form-item>
        <el-form-item label="有效天数" prop="durationDays">
          <el-input-number v-model="form.durationDays" :min="1" :max="3650" style="width: 100%" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="选填" />
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

const cards = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editId = ref(null)
const formRef = ref(null)
const form = reactive({ name: '', durationDays: 30, price: 0, description: '' })
const rules = {
  name: [{ required: true, message: '请输入卡种名称', trigger: 'blur' }],
  durationDays: [{ required: true, message: '请输入有效天数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/cards', { params: { page: page.value, size: size.value } })
    if (res.code === 200) {
      cards.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name
  form.durationDays = row.durationDays
  form.price = row.price
  form.description = row.description || ''
  dialogVisible.value = true
}

function resetForm() {
  form.name = ''
  form.durationDays = 30
  form.price = 0
  form.description = ''
  formRef.value?.resetFields()
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = { ...form }
    let res
    if (isEdit.value) {
      res = await request.put(`/cards/${editId.value}`, data)
    } else {
      res = await request.post('/cards', data)
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

async function handleToggleStatus(row, newStatus) {
  try {
    await request.put(`/cards/${row.id}`, { ...row, status: newStatus })
    ElMessage.success(`${row.name} ${newStatus === 1 ? '已启用' : '已禁用'}`)
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  try {
    await request.delete(`/cards/${row.id}`)
    ElMessage.success(`已删除 ${row.name}`)
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px 20px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
