<template>
  <div class="course-list">
    <h2 class="page-title">📚 课程中心</h2>

    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filters" size="default">
        <el-form-item label="搜索">
          <el-input v-model="filters.keyword" placeholder="课程名称" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.category" placeholder="全部分类" clearable style="width: 130px">
            <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="filters.difficulty" placeholder="全部难度" clearable style="width: 130px">
            <el-option v-for="d in difficulties" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="course-grid" v-loading="loading">
      <el-col :span="8" v-for="course in courses" :key="course.id">
        <el-card shadow="hover" class="course-card" @click="$router.push(`/courses/${course.id}`)">
          <div class="cover">
            <img v-if="course.coverUrl" :src="course.coverUrl" alt="" />
            <div v-else class="cover-placeholder">📹</div>
          </div>
          <div class="card-body">
            <h4 class="title">{{ course.title }}</h4>
            <div class="tags">
              <el-tag size="small" type="info">{{ catLabel(course.category) }}</el-tag>
              <el-tag size="small" :type="diffTag(course.difficulty)">{{ diffLabel(course.difficulty) }}</el-tag>
            </div>
            <div class="meta">
              <span>{{ course.duration || 0 }} 分钟</span>
              <el-rate :model-value="course.rating || 0" disabled show-score text-color="#f56c6c" size="small" />
              <span class="price">¥{{ course.price || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!loading && courses.length === 0" style="text-align:center;padding:60px 0">
      <el-empty description="暂无课程" />
    </div>

    <div class="pagination-wrap" v-if="total > size">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[9, 18, 36]" :total="total"
        layout="total, sizes, prev, pager, next" @current-change="fetchCourses" @size-change="fetchCourses" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const courses = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(9)
const total = ref(0)
const filters = reactive({ keyword: '', category: '', difficulty: '' })

const categories = [
  { label: '增肌', value: 'STRENGTH' }, { label: '减脂', value: 'FAT_LOSS' },
  { label: '瑜伽', value: 'YOGA' }, { label: '有氧', value: 'CARDIO' },
  { label: '拉伸', value: 'STRETCHING' }, { label: '形体纠正', value: 'POSTURE' },
]
const difficulties = [
  { label: '初级', value: 'BEGINNER' }, { label: '中级', value: 'INTERMEDIATE' }, { label: '高级', value: 'ADVANCED' },
]
const catLabel = (c) => categories.find(x => x.value === c)?.label || c
const diffTag = (d) => ({ BEGINNER: 'success', INTERMEDIATE: 'warning', ADVANCED: 'danger' }[d] || 'info')
const diffLabel = (d) => ({ BEGINNER: '初级', INTERMEDIATE: '中级', ADVANCED: '高级' }[d] || d)

onMounted(() => fetchCourses())

async function fetchCourses() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.category) params.category = filters.category
    if (filters.difficulty) params.difficulty = filters.difficulty
    const res = await request.get('/courses', { params })
    if (res.code === 200) { courses.value = res.data.records; total.value = res.data.total }
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleSearch() { page.value = 1; fetchCourses() }
function handleReset() { filters.keyword = ''; filters.category = ''; filters.difficulty = ''; page.value = 1; fetchCourses() }
</script>

<style scoped>
.page-title { margin: 0 0 16px; font-size: 20px; }
.filter-card { margin-bottom: 20px; }
.filter-card :deep(.el-card__body) { padding: 16px 20px 0; }
.course-grid { min-height: 200px; }
.course-card { cursor: pointer; border-radius: 12px; overflow: hidden; transition: transform 0.2s; margin-bottom: 20px; }
.course-card:hover { transform: translateY(-4px); }
.course-card :deep(.el-card__body) { padding: 0; }
.cover { height: 160px; overflow: hidden; background: #f0f2f5; }
.cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { height: 100%; display: flex; align-items: center; justify-content: center; font-size: 48px; }
.card-body { padding: 16px; }
.title { margin: 0 0 10px; font-size: 16px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tags { display: flex; gap: 8px; margin-bottom: 12px; }
.meta { display: flex; justify-content: space-between; font-size: 13px; color: #999; }
.price { color: #f56c6c; font-weight: 600; font-size: 15px; }
.pagination-wrap { margin-top: 8px; display: flex; justify-content: center; }
</style>
