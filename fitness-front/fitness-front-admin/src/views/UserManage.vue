<template>
  <div class="user-manage">
    <el-card>
      <template #header><h3>用户管理</h3></template>
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="角色" width="180">
          <template #default="{ row }">
            <el-select
              v-model="row.role"
              size="small"
              @change="(val) => handleRoleChange(row, val)"
            >
              <el-option label="普通用户" value="USER" />
              <el-option label="教练" value="COACH" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="() => handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" />
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get('/users', { params: { page: page.value, size: size.value } })
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

async function handleRoleChange(row, newRole) {
  try {
    await request.put(`/users/${row.id}/role`, { role: newRole })
    ElMessage.success(`${row.username} 角色已更新为 ${newRole}`)
  } catch (e) {
    ElMessage.error('更新失败')
    fetchUsers()
  }
}

async function handleStatusChange(row) {
  try {
    await request.put(`/users/${row.id}/status`, { status: row.status })
    ElMessage.success(`${row.username} ${row.status === 1 ? '已启用' : '已禁用'}`)
  } catch (e) {
    ElMessage.error('更新失败')
    fetchUsers()
  }
}
</script>

<style scoped>
.user-manage { padding: 20px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
