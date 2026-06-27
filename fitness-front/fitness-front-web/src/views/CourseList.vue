<template>
  <div class="courses-page">
    <!-- Hero Banner -->
    <section class="courses-hero">
      <div class="courses-hero-overlay" />
      <div class="courses-hero-content">
        <h1 class="courses-hero-title reveal-item">课程中心</h1>
        <div class="hero-search reveal-item" style="transition-delay:80ms">
          <el-input v-model="filters.keyword" placeholder="搜索课程..." clearable @keyup.enter="handleSearch" class="hero-search-input" />
        </div>
        <div class="filter-pills reveal-item" style="transition-delay:120ms">
          <span class="pill" :class="{ active: !filters.category }" @click="filters.category='';handleSearch()">全部分类</span>
          <span class="pill" v-for="c in categories" :key="c.value" :class="{ active: filters.category === c.value }" @click="filters.category=c.value;handleSearch()">{{ c.label }}</span>
        </div>
        <div class="filter-pills reveal-item" style="transition-delay:160ms">
          <span class="pill" :class="{ active: !filters.difficulty }" @click="filters.difficulty='';handleSearch()">全部难度</span>
          <span class="pill" v-for="d in difficulties" :key="d.value" :class="{ active: filters.difficulty === d.value }" @click="filters.difficulty=d.value;handleSearch()">{{ d.label }}</span>
        </div>
      </div>
    </section>

    <!-- Course Grid -->
    <div class="courses-container" v-loading="loading">
      <div class="course-grid" v-if="!loading && courses.length > 0">
        <div class="course-card scroll-fade" v-for="(c, i) in courses" :key="c.id" :style="{ transitionDelay: (i % 3 * 60) + 'ms' }" @click="$router.push(`/courses/${c.id}`)">
          <div class="card-cover">
            <img :src="courseCover(c)" alt="" />
            <div class="card-cover-gradient" />
            <div class="card-cover-tags">
              <span class="card-tag">{{ catLabel(c.category) }}</span>
              <span class="card-tag diff" :class="'diff-' + c.difficulty">{{ diffLabel(c.difficulty) }}</span>
            </div>
          </div>
          <div class="card-body">
            <h4 class="card-title">{{ c.title }}</h4>
            <div class="card-meta">
              <span class="card-rating"><el-icon><StarFilled /></el-icon> {{ c.rating || 0 }}</span>
              <span class="card-duration">{{ c.duration || 0 }}分钟</span>
            </div>
            <div class="card-footer">
              <span class="card-price">{{ c.price > 0 ? '¥' + c.price : '免费' }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && courses.length === 0" description="暂无符合条件的课程" :image-size="100" />
      <div class="pagination-wrap" v-if="total > size">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[9,18,36]" layout="total, sizes, prev, pager, next" background @current-change="fetchCourses" @size-change="fetchCourses" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { StarFilled } from '@element-plus/icons-vue'
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
const diffLabel = (d) => ({ BEGINNER: '初级', INTERMEDIATE: '中级', ADVANCED: '高级' }[d] || d)

// Course cover fallback
const exactCovers = {
  '胸部训练完全指南':      'https://k.sinaimg.cn/n/sinacn10/40/w480h360/20180830/a092-hikcahf8819538.jpg/w700h350z1l10t1086f.jpg',
  '肩部宽度与力量训练':    'https://ts1.tc.mm.bing.net/th/id/R-C.2ad691bd775be5099db14641c5514987?rik=rekS4BXOsGRMag&riu=http%3a%2f%2fimg3.codoon.com%2fugc_c69344e1-d6a8-4c72-84dd-2e7c0bf64655_1562318378056_900_599_png&ehk=9Dmr5NVVRifWzOKl7Iv%2feierY%2bnf9x9bz9jDQu1ILeE%3d&risl=&pid=ImgRaw&r=0',
  '背阔肌宽度训练':        'https://pic2.zhimg.com/v2-9613842a4282b215d263e46d3e81d599_1440w.jpg?source=172ae18b',
  '腿部力量基础训练':      'https://ts3.tc.mm.bing.net/th/id/OIP-C.9G5abbfigIYI6TBB6Qgs-QHaHa?rs=1&pid=ImgDetMain&o=7&rm=3',
  '腘绳肌与后链训练':      'https://ts1.tc.mm.bing.net/th/id/R-C.9d2f4a523593a4242372486efd48449e?rik=L%2bcBCyTx%2byOsXw&riu=http%3a%2f%2fcopyright.bdstatic.com%2fvcg%2fcreative%2f551fd74ffcc63b3a91f0e5bd92d2d2c3ba1c2ed2.jpg%40wm_1%2ck_cGljX2JqaHdhdGVyLmpwZw%3d%3d&ehk=oqL54%2bxtQOUvzM7%2bjq4k9oe8umisSNMjxuFYU7iSuSA%3d&risl=&pid=ImgRaw&r=0',
  '臀肌激活与塑形':        'https://ts1.tc.mm.bing.net/th/id/R-C.48117c6ebc32e7d6c851c755ca1e5c3e?rik=jSzYLOO3H5Qz9A&riu=http%3a%2f%2fn.sinaimg.cn%2fsinakd20201104ac%2f223%2fw640h383%2f20201104%2f69c9-kcieywa2100188.jpg&ehk=lramWi%2bmepYQvov2O419KK8AhODswCZIIVfU7Sof7WQ%3d&risl=&pid=ImgRaw&r=0',
  '肱二头肌雕刻训练':      'https://pic1.zhimg.com/v2-dc8551c4ba13ce7d84aff69b2e4d1abc_r.jpg',
  '腹部核心强化训练':      'https://puui.qpic.cn/vpic_cover/p0516p9sgqf/p0516p9sgqf_hz.jpg/1280',
  '肱三头肌增肌计划':      'https://www.jianshen8.com/uploads/allimg/191123/4_191123105348_2.jpg',
  '小腿肌群强化训练':      'https://p0.ifengimg.com/pmop/2018/0226/855F7DDEC3118E2A1A24264777D418670FDB7F70_size540_w1320_h948.png',
  '前臂与握力训练':        'https://ts2.tc.mm.bing.net/th/id/OIP-C.sR5CwFS6Z9zRxGARhUKZOgHaFT?rs=1&pid=ImgDetMain&o=7&rm=3',
}
const coverMap = {
  yoga:     'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&q=80',
  cardio:   'https://images.unsplash.com/photo-1534258936925-c58bed479fcb?w=600&q=80',
  strength: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600&q=80',
  stretch:  'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=600&q=80',
  hiit:     'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600&q=80',
  fatLoss:  'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=600&q=80',
  general:  'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=600&q=80',
}
function courseCover(c) {
  if (c.coverUrl) return c.coverUrl
  if (exactCovers[c.title]) return exactCovers[c.title]
  const t = (c.title || '') + (c.category || '')
  if (/瑜伽|yoga/i.test(t))       return coverMap.yoga
  if (/有氧|cardio|跑步|跑/i.test(t)) return coverMap.cardio
  if (/力量|增肌|举重|strength/i.test(t)) return coverMap.strength
  if (/形体|纠正|拉伸|柔韧|stretch/i.test(t)) return coverMap.stretch
  if (/hiit|高强度|间歇/i.test(t)) return coverMap.hiit
  if (/减脂|燃脂|fat/i.test(t))   return coverMap.fatLoss
  return coverMap.general
}

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
</script>

<style scoped>
/* ---- Layout ---- */
.courses-page {
  margin: calc(-1 * var(--space-lg)) calc(-1 * var(--page-padding-x)) calc(-1 * var(--space-2xl));
}

/* ---- Hero Banner ---- */
.courses-hero {
  position: relative;
  left: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  width: 100vw;
  padding: 80px 0 60px;
  background: url(https://pic3.zhimg.com/v2-f211fd8b848b58a67581cc0b799f6a9b_720w.jpg?source=172ae18b) center / cover no-repeat;
  background-attachment: fixed;
  isolation: isolate;
  text-align: center;
}
.courses-hero-overlay {
  position: absolute; inset: 0; z-index: 0;
  background: rgba(0, 0, 0, 0.75);
}
.courses-hero-content {
  position: relative; z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--page-padding-x);
}
.courses-hero-title {
  font-size: 36px;
  font-weight: var(--font-weight-bold);
  color: #FFFFFF;
  margin: 0 0 24px;
}

/* ---- Hero Search ---- */
.hero-search {
  max-width: 420px;
  margin: 0 auto 24px;
}
.hero-search-input :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  border-radius: 50px;
  box-shadow: none;
  padding: 6px 20px;
}
.hero-search-input :deep(.el-input__inner) {
  color: #FFFFFF;
  font-size: 15px;
}
.hero-search-input :deep(.el-input__inner::placeholder) {
  color: rgba(255,255,255,0.5);
}
.hero-search-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255,255,255,0.4);
}
.hero-search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-brand);
  background: rgba(255,255,255,0.2);
}

/* ---- Filter Pills ---- */
.filter-pills {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 10px;
}
.pill {
  display: inline-block;
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: rgba(255,255,255,0.12);
  color: rgba(255,255,255,0.8);
  border: 1px solid transparent;
  user-select: none;
}
.pill:hover {
  background: rgba(255,255,255,0.2);
  color: #FFFFFF;
}
.pill.active {
  background: var(--color-brand);
  color: #FFFFFF;
  border-color: var(--color-brand);
}

/* ---- Course Container ---- */
.courses-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-xl) var(--page-padding-x) var(--space-2xl);
}

/* ---- Course Grid ---- */
.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 200px;
}
@media (max-width: 991px) { .course-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px) { .course-grid { grid-template-columns: 1fr; } }

/* ---- Course Card ---- */
.course-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  background: #FFFFFF;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}
.course-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.12);
}

/* Card Cover */
.card-cover {
  position: relative;
  aspect-ratio: 3 / 2;
  overflow: hidden;
  background: var(--color-bg-dark);
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.course-card:hover .card-cover img { transform: scale(1.05); }
.card-cover-gradient {
  position: absolute; inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.05) 50%, transparent 100%);
}
.card-cover-tags {
  position: absolute;
  bottom: 10px;
  left: 12px;
  right: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-tag {
  font-size: 11px;
  font-weight: 600;
  color: #FFFFFF;
  background: rgba(0,0,0,0.45);
  padding: 3px 10px;
  border-radius: 10px;
  letter-spacing: 0.5px;
}
.card-tag.diff-BEGINNER    { color: #52C41A; }
.card-tag.diff-INTERMEDIATE { color: #FAAD14; }
.card-tag.diff-ADVANCED     { color: #FF4D4F; }

/* Card Body */
.card-body {
  padding: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-title);
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
}
.card-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #FAAD14;
  font-weight: 600;
}
.card-duration { color: var(--color-text-weak); }
.card-footer { display: flex; justify-content: flex-end; }
.card-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-brand);
}

/* ---- Pagination ---- */
.pagination-wrap {
  margin-top: var(--space-xl);
  display: flex;
  justify-content: center;
}
</style>
