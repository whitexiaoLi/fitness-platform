<template>
  <div class="role-permission">
    <el-row :gutter="20">
      <!-- Left: Role List -->
      <el-col :span="6">
        <el-card>
          <template #header><span>角色列表</span></template>
          <div v-loading="rolesLoading">
            <div
              v-for="role in roles"
              :key="role.id"
              class="role-item"
              :class="{ active: selectedRole?.id === role.id }"
              @click="selectRole(role)"
            >
              <span class="role-name">{{ role.name }}</span>
              <span class="role-code">{{ role.code }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right: Permission Matrix -->
      <el-col :span="18">
        <el-card>
          <template #header>
            <span v-if="selectedRole">{{ selectedRole.name }} — 权限配置</span>
            <span v-else>请选择左侧角色</span>
          </template>

          <div v-if="selectedRole" v-loading="permsLoading">
            <div v-for="group in permissionGroups" :key="group.name" class="perm-group">
              <h4 class="group-title">{{ group.name }}</h4>
              <el-checkbox-group v-model="checkedPermIds" @change="handlePermChange">
                <div class="perm-list">
                  <el-checkbox
                    v-for="perm in group.permissions"
                    :key="perm.id"
                    :label="perm.id"
                    :value="perm.id"
                  >
                    {{ perm.name }}
                    <span class="perm-code">{{ perm.code }}</span>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
            <div style="margin-top: 20px">
              <el-button type="primary" @click="handleSave" :loading="saving">保存权限</el-button>
            </div>
          </div>

          <el-empty v-else description="请选择左侧角色查看权限" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const roles = ref([])
const permissions = ref([])
const rolesLoading = ref(false)
const permsLoading = ref(false)
const selectedRole = ref(null)
const checkedPermIds = ref([])
const saving = ref(false)

const permissionGroups = computed(() => {
  const groups = {}
  permissions.value.forEach(p => {
    const g = p.groupName || '其他'
    if (!groups[g]) groups[g] = { name: g, permissions: [] }
    groups[g].permissions.push(p)
  })
  return Object.values(groups)
})

onMounted(async () => {
  rolesLoading.value = true
  try {
    const res = await request.get('/roles')
    if (res.code === 200) roles.value = res.data || []
  } catch (e) { /* ignore */ }
  rolesLoading.value = false

  try {
    const res = await request.get('/roles/permissions')
    if (res.code === 200) permissions.value = res.data || []
  } catch (e) { /* ignore */ }
})

async function selectRole(role) {
  selectedRole.value = role
  permsLoading.value = true
  try {
    const res = await request.get(`/roles/${role.id}/permissions`)
    if (res.code === 200) checkedPermIds.value = res.data || []
  } catch (e) {
    checkedPermIds.value = []
  }
  permsLoading.value = false
}

function handlePermChange() {
  // handled by v-model
}

async function handleSave() {
  if (!selectedRole.value) return
  saving.value = true
  try {
    await request.put(`/roles/${selectedRole.value.id}/permissions`, {
      permissionIds: checkedPermIds.value,
    })
    ElMessage.success('权限已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.role-item {
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  transition: background 0.2s;
  margin-bottom: 4px;
}
.role-item:hover { background: #f0f2f5; }
.role-item.active { background: #ecf5ff; border-left: 3px solid #409eff; }
.role-name { font-size: 14px; font-weight: 500; }
.role-code { font-size: 12px; color: #999; }

.perm-group { margin-bottom: 20px; }
.group-title { font-size: 15px; color: #333; margin-bottom: 10px; padding-bottom: 6px; border-bottom: 1px solid #eee; }
.perm-list { display: flex; flex-wrap: wrap; gap: 12px; }
.perm-code { font-size: 11px; color: #999; margin-left: 4px; }
</style>
