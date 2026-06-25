<template>
  <div class="staff-manage">
    <!-- Toolbar -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" size="default">
        <el-form-item label="搜索">
          <el-input
            v-model="filters.keyword"
            placeholder="用户名 / 昵称 / 手机号"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filters.role" placeholder="全部" clearable style="width: 130px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教练" value="COACH" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="showCreateDialog">添加员工</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card style="margin-top: 16px">
      <el-table :data="users" stripe v-loading="loading" size="default">
        <el-table-column label="员工" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.avatarUrl" icon="UserFilled" />
              <div class="user-info">
                <span class="user-name">{{ row.nickname }}</span>
                <span class="user-account">@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-popconfirm
              :title="`确认将 ${row.username} 的角色改为 ${nextRoleLabel(row.role)} 吗？`"
              @confirm="handleRoleChange(row, nextRole(row.role))"
            >
              <template #reference>
                <el-tag :type="roleTag(row.role)" class="role-tag">
                  {{ roleLabel(row.role) }}
                  <el-icon style="margin-left: 2px"><Switch /></el-icon>
                </el-tag>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val ? 1 : 0)"
            />
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="170">
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

    <!-- Create Dialog -->
    <el-dialog v-model="createVisible" title="添加员工" width="480px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教练" value="COACH" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Switch } from '@element-plus/icons-vue'

const users = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const filters = reactive({ keyword: '', role: '', status: '' })

const roleMap = { ADMIN: '管理员', COACH: '教练' }
const roleTag = (r) => ({ ADMIN: 'danger', COACH: 'warning' }[r] || 'info')
const roleLabel = (r) => roleMap[r] || r
const roleCycle = { ADMIN: 'COACH', COACH: 'ADMIN' }
const nextRole = (r) => roleCycle[r] || 'COACH'
const nextRoleLabel = (r) => roleMap[roleCycle[r]] || '教练'

// Create form
const createVisible = ref(false)
const creating = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '', nickname: '', role: 'COACH', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
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
    if (filters.role) params.role = filters.role
    else params.role = 'ADMIN,COACH'
    if (filters.status !== '' && filters.status !== null) params.status = filters.status
    const res = await request.get('/users', { params })
    if (res.code === 200) {
      users.value = res.data.records
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
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  page.value = 1
  fetchData()
}

function showCreateDialog() {
  resetForm()
  createVisible.value = true
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.role = 'COACH'
  form.phone = ''
  formRef.value?.resetFields()
}

async function handleCreate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    const res = await request.post('/users', { ...form })
    if (res.code === 200) {
      ElMessage.success(`员工 ${form.username} 创建成功`)
      createVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  } finally {
    creating.value = false
  }
}

async function handleRoleChange(row, newRole) {
  try {
    await request.put(`/users/${row.id}/role`, { role: newRole })
    ElMessage.success(`${row.username} 角色已更新为 ${roleLabel(newRole)}`)
    fetchData()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

async function handleStatusChange(row, newStatus) {
  try {
    await request.put(`/users/${row.id}/status`, { status: newStatus })
    ElMessage.success(`${row.username} ${newStatus === 1 ? '已启用' : '已禁用'}`)
    fetchData()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}
</script>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px 20px 0; }

.user-cell { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: 14px; }
.user-account { font-size: 12px; color: #999; }

.role-tag { cursor: pointer; }
.role-tag:hover { opacity: 0.8; }

.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
