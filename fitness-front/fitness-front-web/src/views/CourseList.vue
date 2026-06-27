<template>
  <div class="course-list">
    <h2 class="page-title">课程中心</h2>

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
        <div class="course-card" @click="$router.push(`/courses/${course.id}`)">
          <div class="cover">
            <img v-if="course.coverUrl" :src="course.coverUrl" alt="" />
            <div v-else class="cover-placeholder">📹</div>
            <div class="cover-overlay">
              <span class="cover-diff" :class="'diff-' + course.difficulty">{{ diffLabel(course.difficulty) }}</span>
            </div>
          </div>
          <div class="card-body">
            <span class="card-category">{{ catLabel(course.category) }}</span>
            <h4 class="title">{{ course.title }}</h4>
            <div class="meta">
              <span>{{ course.duration || 0 }} 分钟</span>
              <el-rate :model-value="course.rating || 0" disabled show-score text-color="#f56c6c" size="small" />
              <span class="price">¥{{ course.price || 0 }}</span>
            </div>
          </div>
        </div>
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
.course-grid { min-height: 200px; }
.course-card {
  cursor: pointer;
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all var(--transition-normal);
  margin-bottom: 20px;
  border: 1px solid var(--color-border);
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}
.cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: var(--color-bg-dark);
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease-out);
}
.course-card:hover .cover img { transform: scale(1.06); }
.cover-placeholder { height: 100%; display: flex; align-items: center; justify-content: center; font-size: 48px; color: rgba(255,255,255,0.3); }

/* Difficulty badge — weight plate style */
.cover-overlay {
  position: absolute;
  top: 0;
  right: 0;
  padding: 5px 14px;
  background: rgba(10,10,10,0.8);
  border-radius: 0 0 0 var(--radius-md);
  backdrop-filter: blur(4px);
}
.cover-diff {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.diff-BEGINNER    { color: #52C41A; }
.diff-INTERMEDIATE { color: #FAAD14; }
.diff-ADVANCED     { color: #FF4D4F; }

.card-body { padding: var(--space-md); }
.card-category {
  font-size: 11px;
  color: var(--color-brand);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 4px;
  display: block;
}
.title { margin: 0 0 10px; font-size: 16px; font-weight: 600; color: var(--color-text-title); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.meta { display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: var(--color-text-secondary); }
.price { color: var(--color-brand); font-weight: 700; font-size: 15px; }
.pagination-wrap { margin-top: 8px; display: flex; justify-content: center; }
</style>
