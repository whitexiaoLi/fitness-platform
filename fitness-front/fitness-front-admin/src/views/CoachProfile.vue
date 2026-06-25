<template>
  <div class="coach-profile">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" size="default">
        <el-form-item label="搜索">
          <el-input
            v-model="filters.keyword"
            placeholder="姓名 / 用户名 / 手机号"
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

    <div class="coach-grid">
      <el-card v-for="coach in coaches" :key="coach.id" shadow="hover" class="coach-card" v-loading="loading">
        <div class="coach-header">
          <el-avatar :size="64" :src="coach.avatarUrl" icon="UserFilled" />
          <div class="coach-basic">
            <h4>{{ coach.nickname }}</h4>
            <span class="coach-username">@{{ coach.username }}</span>
            <span v-if="coach.phone" class="coach-phone">{{ coach.phone }}</span>
          </div>
          <el-tag :type="coach.status === 1 ? 'success' : 'danger'" size="small">
            {{ coach.status === 1 ? '在职' : '禁用' }}
          </el-tag>
        </div>

        <el-divider />

        <div class="coach-details">
          <div class="detail-row">
            <span class="label">教龄</span>
            <span class="value">{{ coach.experience ? coach.experience + ' 年' : '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">课时费</span>
            <span class="value">{{ coach.hourlyRate ? '¥' + coach.hourlyRate + '/时' : '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">专长</span>
            <span class="value">{{ coach.specialties || '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">认证</span>
            <span class="value">{{ coach.certifications || '未填写' }}</span>
          </div>
          <div class="detail-row" v-if="coach.bio">
            <span class="label">简介</span>
            <span class="value bio">{{ coach.bio }}</span>
          </div>
        </div>

        <el-button type="primary" size="small" style="width: 100%; margin-top: 12px" @click="showEditDialog(coach)">
          编辑档案
        </el-button>
      </el-card>
    </div>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[8, 16, 32]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑教练档案" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="个人简介">
          <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="教练个人介绍" />
        </el-form-item>
        <el-form-item label="专长">
          <el-input v-model="form.specialties" placeholder="如：增肌、减脂、瑜伽、康复" />
        </el-form-item>
        <el-form-item label="认证资质">
          <el-input v-model="form.certifications" type="textarea" :rows="2" placeholder="如：ACE-CPT、NSCA-CSCS" />
        </el-form-item>
        <el-form-item label="教龄(年)">
          <el-input-number v-model="form.experience" :min="0" :max="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课时费(¥)">
          <el-input-number v-model="form.hourlyRate" :min="0" :precision="2" :step="50" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const coaches = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(8)
const total = ref(0)
const filters = reactive({ keyword: '' })

const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const form = reactive({
  bio: '', certifications: '', specialties: '', experience: 0, hourlyRate: 0,
})

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.keyword) params.keyword = filters.keyword
    const res = await request.get('/coaches', { params })
    if (res.code === 200) {
      coaches.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; fetchData() }
function handleReset() { filters.keyword = ''; page.value = 1; fetchData() }

function showEditDialog(coach) {
  editId.value = coach.id
  form.bio = coach.bio || ''
  form.certifications = coach.certifications || ''
  form.specialties = coach.specialties || ''
  form.experience = coach.experience || 0
  form.hourlyRate = coach.hourlyRate || 0
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const res = await request.put(`/coaches/${editId.value}`, { ...form })
    if (res.code === 200) {
      ElMessage.success('档案已更新')
      dialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px 20px 0; }

.coach-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.coach-card :deep(.el-card__body) { padding: 20px; }

.coach-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.coach-basic {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.coach-basic h4 { margin: 0 0 4px 0; font-size: 16px; }
.coach-username { font-size: 12px; color: #999; }
.coach-phone { font-size: 12px; color: #666; margin-top: 2px; }

.coach-details { display: flex; flex-direction: column; gap: 8px; }
.detail-row { display: flex; gap: 8px; font-size: 13px; }
.detail-row .label { color: #999; min-width: 48px; flex-shrink: 0; }
.detail-row .value { color: #333; }
.detail-row .bio { font-size: 12px; color: #666; }

.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
