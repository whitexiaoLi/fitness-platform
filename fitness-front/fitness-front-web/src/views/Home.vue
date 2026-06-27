<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-content">
        <p class="hero-overline">// 你的私人健身伙伴</p>
        <h1 class="hero-title">
          用数据<span class="hero-red">雕刻</span>更好的自己
        </h1>
        <p class="hero-desc">
          科学管理每一次训练、每一餐饮食、每一组身体数据。<br />
          让健身不再盲目，用数据看见改变。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="hero-btn" @click="$router.push('/courses')">探索课程</el-button>
          <el-button size="large" class="hero-btn-outline" @click="$router.push('/training')">开始训练</el-button>
        </div>
        <div class="hero-stats">
          <div class="hero-stat"><span class="hs-num">200+</span><span class="hs-label">精选食物</span></div>
          <div class="hero-stat"><span class="hs-num">11</span><span class="hs-label">专业课程</span></div>
          <div class="hero-stat"><span class="hs-num">60</span><span class="hs-label">动作教学</span></div>

        </div>
      </div>
    </section>

    <!-- Feature Cards -->
    <section class="section">
      <h2 class="section-title">
        核心<span class="red">功能</span>
      </h2>
      <p class="section-subtitle">一站式健身管理，覆盖训练、饮食、数据追踪</p>
      <el-row :gutter="24" class="feature-grid">
        <el-col :xs="12" :sm="12" :md="6" v-for="item in features" :key="item.path">
          <div class="feature-card" @click="$router.push(item.path)">
            <div class="feature-icon" :style="{ background: item.color }">{{ item.icon }}</div>
            <h3 class="feature-label">{{ item.label }}</h3>
            <p class="feature-desc">{{ item.desc }}</p>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- Courses Preview -->
    <section class="section section-alt">
      <h2 class="section-title">
        热门<span class="red">课程</span>
      </h2>
      <p class="section-subtitle">精选专业健身课程，覆盖增肌、减脂、瑜伽、形体纠正</p>
      <el-row :gutter="20" class="course-grid" v-loading="courseLoading">
        <el-col :xs="12" :sm="8" :md="6" v-for="c in previewCourses" :key="c.id">
          <div class="course-card" @click="$router.push(`/courses/${c.id}`)">
            <div class="course-cover">
              <img v-if="c.coverUrl" :src="c.coverUrl" alt="" />
              <span v-else class="course-cover-icon">📹</span>
            </div>
            <div class="course-body">
              <div class="course-tags">
                <el-tag size="small" type="info">{{ c.category }}</el-tag>
                <el-tag size="small" :type="diffTag(c.difficulty)">{{ diffLabel(c.difficulty) }}</el-tag>
              </div>
              <h4 class="course-title">{{ c.title }}</h4>
              <div class="course-meta">
                <span>{{ c.duration || 0 }} 分钟</span>
                <span class="course-price">¥{{ c.price || 0 }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <div v-if="!courseLoading && previewCourses.length === 0" style="text-align:center;padding:40px 0">
        <el-empty description="暂无课程" :image-size="80" />
      </div>
      <div class="section-action" v-if="previewCourses.length > 0">
        <el-button type="primary" @click="$router.push('/courses')">查看全部课程 →</el-button>
      </div>
    </section>

    <!-- CTA Banner -->
    <section class="cta-banner">
      <div class="cta-content">
        <h2 class="cta-title">准备好<span class="cta-red">改变</span>了吗？</h2>
        <p class="cta-desc">加入 FITNESS，科学管理你的健身之旅。今天就开始第一次训练。</p>
        <el-button size="large" class="cta-btn" @click="$router.push('/training')">立即开始</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()

const courseLoading = ref(false)
const previewCourses = ref([])

const features = [
  { icon: '📚', label: '课程中心', desc: '专业健身课程，覆盖多品类', path: '/courses', color: '#FFF0F0' },
  { icon: '🏋️', label: '训练记录', desc: '记录每组训练，追踪进步', path: '/training', color: '#F0F5FF' },
  { icon: '🥗', label: '饮食管理', desc: '智能食物搜索，精准营养', path: '/diet', color: '#F0FFF4' },
  { icon: '📊', label: '身体数据', desc: '实时监测身体指标变化', path: '/metrics', color: '#FFF8F0' },
]

const difficulties = [
  { label: '初级', value: 'BEGINNER' },
  { label: '中级', value: 'INTERMEDIATE' },
  { label: '高级', value: 'ADVANCED' },
]
const diffTag = (d) => ({ BEGINNER: 'success', INTERMEDIATE: 'warning', ADVANCED: 'danger' }[d] || 'info')
const diffLabel = (d) => difficulties.find(x => x.value === d)?.label || d

onMounted(async () => {
  courseLoading.value = true
  try {
    const res = await request.get('/courses', { params: { page: 1, size: 4 } })
    if (res.code === 200) previewCourses.value = res.data.records || []
  } catch { /* ignore */ }
  finally { courseLoading.value = false }
})
</script>

<style scoped>
/* ---- Hero ---- */
.hero {
  background: var(--color-bg-dark);
  margin: -24px calc(-1 * var(--page-padding-x)) 0;
  padding: var(--space-2xl) var(--page-padding-x);
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-content {
  max-width: 900px;
  text-align: center;
}
.hero-overline {
  font-size: 13px;
  color: var(--color-brand);
  font-weight: var(--font-weight-medium);
  letter-spacing: 2px;
  margin: 0 0 var(--space-md);
  text-transform: uppercase;
}
.hero-title {
  font-size: var(--font-hero);
  font-weight: var(--font-weight-bold);
  color: #FFFFFF;
  line-height: var(--line-height-tight);
  margin: 0 0 var(--space-lg);
  letter-spacing: 0.02em;
}
.hero-red { color: var(--color-brand); }
.hero-desc {
  font-size: 15px;
  color: var(--color-text-on-dark-secondary);
  line-height: 1.8;
  margin: 0 0 var(--space-xl);
}
.hero-actions {
  display: flex;
  gap: var(--space-md);
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: var(--space-xl);
}
.hero-btn {
  background: var(--color-brand);
  border-color: var(--color-brand);
  font-weight: var(--font-weight-medium);
  padding: 12px 36px;
  font-size: 15px;
}
.hero-btn:hover { background: var(--color-brand-hover); border-color: var(--color-brand-hover); }
.hero-btn-outline {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.3);
  color: #FFFFFF;
  font-weight: var(--font-weight-medium);
  padding: 12px 36px;
  font-size: 15px;
}
.hero-btn-outline:hover { border-color: #FFFFFF; color: #FFFFFF; }

/* Hero stats */
.hero-stats {
  display: flex;
  justify-content: center;
  gap: var(--space-xl);
  flex-wrap: wrap;
}
.hero-stat {
  text-align: center;
  padding: 0 var(--space-lg);
  border-right: 1px solid rgba(255,255,255,0.12);
}
.hero-stat:last-child { border-right: none; }
.hs-num {
  display: block;
  font-size: 24px;
  font-weight: var(--font-weight-bold);
  color: var(--color-brand);
}
.hs-label {
  font-size: 12px;
  color: rgba(255,255,255,0.5);
  margin-top: 4px;
}

/* ---- Sections ---- */
.section {
  padding-top: var(--space-2xl);
}
.section-alt {
  background: var(--color-bg-light);
  margin: 0 calc(-1 * var(--page-padding-x));
  padding: var(--space-2xl) var(--page-padding-x);
}
.section-action {
  text-align: center;
  margin-top: var(--space-xl);
}

/* ---- Feature Cards ---- */
.feature-grid { margin-top: var(--space-lg); }
.feature-card {
  text-align: center;
  cursor: pointer;
  padding: var(--space-xl) var(--space-md);
  background: var(--color-bg-page);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  margin-bottom: var(--space-lg);
}
.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}
.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin: 0 auto var(--space-md);
}
.feature-label {
  font-size: 17px;
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-title);
  margin: 0 0 4px;
}
.feature-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* ---- Course Cards ---- */
.course-grid { min-height: 100px; }
.course-card {
  cursor: pointer;
  background: var(--color-bg-page);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all var(--transition-normal);
  margin-bottom: var(--space-lg);
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}
.course-cover {
  height: 150px;
  background: var(--color-bg-light);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease-out);
}
.course-card:hover .course-cover img { transform: scale(1.05); }
.course-cover-icon { font-size: 48px; }
.course-body { padding: var(--space-md); }
.course-tags { display: flex; gap: 6px; margin-bottom: 8px; }
.course-title {
  font-size: 15px;
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-title);
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.course-price {
  color: var(--color-brand);
  font-weight: var(--font-weight-semibold);
  font-size: 15px;
}

/* ---- CTA Banner ---- */
.cta-banner {
  margin: 0 calc(-1 * var(--page-padding-x));
  background: var(--color-bg-dark);
  padding: var(--space-2xl) var(--page-padding-x);
  text-align: center;
}
.cta-title {
  font-size: var(--font-h1);
  font-weight: var(--font-weight-bold);
  color: #FFFFFF;
  margin: 0 0 var(--space-md);
}
.cta-red { color: var(--color-brand); }
.cta-desc {
  font-size: 15px;
  color: var(--color-text-on-dark-secondary);
  margin: 0 0 var(--space-xl);
}
.cta-btn {
  background: var(--color-brand);
  border-color: var(--color-brand);
  font-weight: var(--font-weight-medium);
  padding: 14px 48px;
  font-size: 16px;
}
.cta-btn:hover { background: var(--color-brand-hover); border-color: var(--color-brand-hover); }

/* ---- Responsive ---- */
@media (max-width: 767px) {
  .hero { padding: var(--space-xl) var(--page-padding-x); }
  .hero-stats { gap: var(--space-md); }
  .hero-stat { padding: 0 var(--space-md); }
  .hs-num { font-size: 20px; }
  .section { padding-top: var(--space-xl); }
}
</style>
