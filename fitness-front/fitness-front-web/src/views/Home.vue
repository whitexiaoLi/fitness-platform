<template>
  <div class="home">
    <!-- Hero Carousel -->
    <section class="hero-carousel" @mousemove="onHeroMouseMove" @mouseleave="onHeroMouseLeave" ref="heroRef">
      <div class="hc-track" :style="{ transform: 'translateX(-' + (currentSlide * 100) + '%)' }">
        <div class="hc-slide" v-for="(s, i) in slides" :key="i" :style="{ backgroundImage: 'url(' + s.image + ')' }" :class="{ 'hc-slide-active': mounted && currentSlide === i }">
          <div class="hc-overlay">
            <div class="hc-content" :class="{ 'hc-content-active': mounted && currentSlide === i }">
              <p class="hero-overline hc-anim">{{ s.overline }}</p>
              <h1 class="hero-title hc-anim" style="transition-delay:150ms">{{ s.titlePrefix }}<span class="hero-red">{{ s.titleRed }}</span>{{ s.titleSuffix }}</h1>
              <p class="hero-desc hc-anim" style="transition-delay:300ms">{{ s.desc }}</p>
              <div class="hero-actions hc-anim" style="transition-delay:450ms" v-if="i === 0">
                <el-button type="primary" size="large" class="hero-btn" @click="$router.push('/courses')">探索课程</el-button>
                <el-button size="large" class="hero-btn-outline" @click="$router.push('/training')">开始训练</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- Indicators -->
      <div class="hc-dots">
        <span v-for="(s, i) in slides" :key="i" class="hc-dot" :class="{ active: currentSlide === i }" @click="goToSlide(i)">
          <span class="hc-dot-fill" v-if="currentSlide === i" :style="{ animationDuration: '5s' }" />
        </span>
      </div>
      <!-- Arrows -->
      <button class="hc-arrow hc-prev" @click="prevSlide">‹</button>
      <button class="hc-arrow hc-next" @click="nextSlide">›</button>
      <!-- Bottom stats (only on first slide) -->
      <!-- Ambient particles -->
      <div class="particles">
        <div class="particle" v-for="i in 10" :key="i" />
      </div>
      <!-- Mouse spotlight -->
      <div class="hc-spotlight" :style="{ background: 'radial-gradient(circle 500px at ' + spotlightX + '% ' + spotlightY + '%, rgba(217,56,49,0.08) 0%, transparent 60%)' }" />
      <div class="hero-stats" v-show="currentSlide === 0">
        <div class="hero-stat"><span class="hs-num">10+</span><span class="hs-label">专业教练</span></div>
        <div class="hero-stat"><span class="hs-num">11</span><span class="hs-label">专业课程</span></div>
        <div class="hero-stat"><span class="hs-num">60</span><span class="hs-label">动作教学</span></div>
      </div>
    </section>

    <!-- Features -->
    <section class="section-full section-dark">
      <h2 class="section-title reveal-item">
        核心<span class="red">功能</span>
      </h2>
      <p class="section-subtitle reveal-item" style="transition-delay:100ms">一站式健身管理，覆盖训练、饮食、数据追踪</p>
      <div class="feature-grid">
        <div class="feature-card scroll-fade" v-for="(item, i) in features" :key="item.path" :style="{ transitionDelay: (i * 100) + 'ms' }">
          <div class="feature-icon-box">
            <el-icon :size="28"><component :is="item.iconComponent" /></el-icon>
          </div>
          <h3 class="feature-label">{{ item.label }}</h3>
          <p class="feature-desc">{{ item.desc }}</p>
          <el-button class="feature-btn" @click="$router.push(item.path)">{{ item.btnText }}</el-button>
        </div>
      </div>
    </section>

    <!-- Courses Preview -->
    <section class="section-full section-dark">
      <div class="courses-header reveal-item">
        <div>
          <h2 class="section-title reveal-item">热门<span class="red">课程</span></h2>
          <p class="section-subtitle reveal-item" style="transition-delay:100ms">精选专业健身课程，覆盖增肌、减脂、瑜伽、形体纠正</p>
        </div>
        <el-button type="primary" @click="$router.push('/courses')" class="view-all-btn">查看全部 →</el-button>
      </div>
      <!-- Skeleton loading -->
      <div class="course-grid" v-if="courseLoading">
        <div class="course-card skeleton-card" v-for="i in 3" :key="'sk'+i">
          <div class="course-cover skeleton" />
        </div>
      </div>
      <!-- Real courses -->
      <div class="course-grid" v-else>
        <div class="course-card course-stagger" v-for="(c, i) in previewCourses" :key="c.id" :data-stagger-delay="0" @click="$router.push(`/courses/${c.id}`)">
          <div class="course-cover">
            <img :src="courseCover(c)" alt="" />
            <div class="course-cover-gradient" />
            <div class="course-cover-info">
              <div class="course-cover-tags">
                <el-tag size="small" effect="dark" type="danger">{{ c.category }}</el-tag>
                <el-tag size="small" effect="dark" :type="diffTag(c.difficulty)">{{ diffLabel(c.difficulty) }}</el-tag>
              </div>
              <h4 class="course-title">{{ c.title }}</h4>
              <div class="course-meta">
                <span class="course-price">{{ c.price > 0 ? '¥' + c.price : '免费' }}</span>
                <span class="course-duration">{{ c.duration || 0 }}分钟</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!courseLoading && previewCourses.length === 0" style="text-align:center;padding:40px 0">
        <el-empty description="暂无课程" :image-size="80" />
      </div>
    </section>

    <!-- Coaches -->
    <section class="section-full section-dark coach-section">
      <div class="coach-header">
        <h2 class="section-title reveal-item">专业<span class="red">教练</span>团队</h2>
        <p class="section-subtitle reveal-item" style="transition-delay:100ms">经验丰富的认证教练团队，为你的健身之路保驾护航</p>
      </div>
      <div class="coach-grid">
        <div class="trainer-card scroll-fade" v-for="(c, i) in coaches" :key="c.name" :style="{ transitionDelay: (i * 80) + 'ms' }">
          <div class="trainer-img-box">
            <img :src="c.avatar" :alt="c.name" class="trainer-img" @error="handleAvatarError(c)" />
            <div v-if="!c.avatar" class="trainer-img-fallback">{{ c.name.charAt(0) }}</div>
          </div>
          <div class="trainer-body">
            <h3 class="trainer-name">{{ c.name }}</h3>
            <p class="trainer-role">{{ c.title }}</p>
            <p class="trainer-bio">{{ c.bio }}</p>
            <p class="trainer-stat">{{ c.stat }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Banner -->
    <section class="cta-banner" :style="{ backgroundImage: 'url(https://images.unsplash.com/photo-1534258936925-c58bed479fcb?w=1200&q=80)' }">
      <div class="cta-overlay" />
      <div class="cta-content">
        <h2 class="cta-title scroll-fade">准备好<span class="cta-red">改变</span>了吗？</h2>
        <p class="cta-desc scroll-fade" style="transition-delay:80ms">加入 FITNESS，科学管理你的健身之旅。今天就开始第一次训练。</p>
        <el-button class="cta-btn scroll-fade" style="transition-delay:160ms" size="large" @click="$router.push('/training')">立即开始</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowRight, Reading, TrendCharts, DishDot, Monitor } from '@element-plus/icons-vue'
import request from '@/utils/request'

// Hero carousel
const slides = [
  { image:'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1200&q=80', overline:'你的私人健身伙伴', titlePrefix:'用数据', titleRed:'雕刻', titleSuffix:'更好的自己', desc:'科学管理每一次训练、每一餐饮食、每一组身体数据。让健身不再盲目，用数据看见改变。' },
  { image:'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=1200&q=80', overline:'持证教练团队', titlePrefix:'专业', titleRed:'教练', titleSuffix:'指导', desc:'持证上岗的教练团队，量身定制训练计划，科学指导每一次训练。' },
  { image:'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=1200&q=80', overline:'科学课程体系', titlePrefix:'系统', titleRed:'课程', titleSuffix:'覆盖', desc:'增肌、减脂、瑜伽、形体纠正等11门系统课程，满足不同训练目标。' },
  { image:'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=1200&q=80', overline:'标准动作教学', titlePrefix:'动作', titleRed:'教学', titleSuffix:'演示', desc:'60+标准动作中英文讲解，Ken Burns动态演示，告别错误姿势。' },
]
const currentSlide = ref(0)
const mounted = ref(false)
let autoTimer = null
function goToSlide(i){ if(i===currentSlide.value)return; currentSlide.value=i; resetAuto() }
function nextSlide(){ goToSlide((currentSlide.value+1)%slides.length) }
function prevSlide(){ goToSlide((currentSlide.value-1+slides.length)%slides.length) }
function resetAuto(){ clearInterval(autoTimer); autoTimer=setInterval(nextSlide,5000) }
// Hero spotlight
const heroRef = ref(null)
const spotlightX = ref(50)
const spotlightY = ref(50)
function onHeroMouseMove(e) {
  const rect = heroRef.value?.getBoundingClientRect()
  if (!rect) return
  spotlightX.value = ((e.clientX - rect.left) / rect.width) * 100
  spotlightY.value = ((e.clientY - rect.top) / rect.height) * 100
}
function onHeroMouseLeave() { spotlightX.value = 50; spotlightY.value = 50 }

onMounted(()=>{ setTimeout(()=>{ mounted.value=true }, 100); autoTimer=setInterval(nextSlide,5000) })
onUnmounted(()=>{ clearInterval(autoTimer) })

const courseLoading = ref(false)
const previewCourses = ref([])

const features = [
  { label: '课程中心', desc: '专业健身课程，覆盖增肌减脂瑜伽多品类，系统化训练计划', path: '/courses', iconComponent: Reading, btnText: '浏览课程' },
  { label: '训练记录', desc: '记录每组训练，智能化追踪进步趋势，内置计时器与训练引导', path: '/training', iconComponent: TrendCharts, btnText: '开始训练' },
  { label: '饮食管理', desc: '智能食物搜索，精准计算三大宏量素，200+食物数据库', path: '/diet', iconComponent: DishDot, btnText: '记录饮食' },
  { label: '身体数据', desc: '实时监测体重体脂变化，BMI自动计算，可视化趋势图表', path: '/metrics', iconComponent: Monitor, btnText: '查看数据' },
]

const coaches = [
  { name: '陈伟', title: '高级私人教练', avatar: 'https://images.unsplash.com/photo-1567013127542-490d757e51fc?w=400&q=80', bio: 'NSCA-CSCS认证，曾获全国力量举锦标赛亚军，擅长生物力学分析与个性化周期化训练方案设计。', stat: '累计指导 500+ 学员' },
  { name: '林小曼', title: '瑜伽/普拉提导师', avatar: 'https://images.unsplash.com/photo-1594381898411-846e7d193883?w=400&q=80', bio: '全美瑜伽联盟RYT-500认证导师，曾赴印度进修Ashtanga，擅长将瑜伽融入现代康复理疗。', stat: '累计指导 400+ 学员' },
  { name: '张浩宇', title: '体能训练专家', avatar: 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=400&q=80', bio: 'ACE-CPT认证，前国家队体能助理教练，专注代谢训练与功能性体能提升，累计帮助300+学员达成目标。', stat: '累计指导 300+ 学员' },
  { name: '王思雅', title: '运动营养师', avatar: 'https://images.unsplash.com/photo-1591084728795-1149f32d9866?w=400&q=80', bio: '注册营养师RD，运动营养学硕士，擅长结合训练计划制定精准营养方案，从饮食源头改变体态。', stat: '累计指导 350+ 学员' },
]

function handleAvatarError(c) { c.avatar = '' }

const difficulties = [
  { label: '初级', value: 'BEGINNER' },
  { label: '中级', value: 'INTERMEDIATE' },
  { label: '高级', value: 'ADVANCED' },
]
const diffTag = (d) => ({ BEGINNER: 'success', INTERMEDIATE: 'warning', ADVANCED: 'danger' }[d] || 'info')
const diffLabel = (d) => difficulties.find(x => x.value === d)?.label || d

// Course cover fallback images keyed by category keyword
const coverMap = {
  yoga:     'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&q=80',
  cardio:   'https://images.unsplash.com/photo-1534258936925-c58bed479fcb?w=600&q=80',
  strength: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600&q=80',
  stretch:  'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=600&q=80',
  hiit:     'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600&q=80',
  fatLoss:  'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=600&q=80',
  general:  'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=600&q=80',
}
// Course cover: use DB coverUrl, else match by title, else fallback by keyword
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
/* ---- Layout offset: cancel .layout-main padding for full-bleed sections ---- */
.home {
  margin: calc(-1 * var(--space-lg)) calc(-1 * var(--page-padding-x)) calc(-1 * var(--space-2xl));
}

/* ---- Remove // prefix from all section titles ---- */
.section-title::before { display: none; }

/* ---- Hero Carousel ---- */
.hero-carousel {
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  margin-top: calc(-1 * var(--nav-height) - 24px);
  width: 100vw;
  overflow: hidden;
  height: 100vh;
}
.hc-track {
  display: flex;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.42, 0, 0.58, 1);
}
/* Slide image fade */
.hc-slide {
  flex: 0 0 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  opacity: 0;
  transition: opacity 1.2s ease;
}
.hc-slide.hc-slide-active { opacity: 1; }

/* Text stagger animation */
.hc-anim {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.8s ease, transform 0.8s ease;
}
.hc-content-active .hc-anim {
  opacity: 1;
  transform: translateY(0);
}

.hc-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.hc-content {
  max-width: 900px;
  text-align: center;
  padding: 0 24px;
}
.hero-overline {
  font-size: 13px;
  color: var(--color-brand);
  font-weight: 600;
  letter-spacing: 2px;
  margin: 0 0 var(--space-md);
  text-transform: uppercase;
}
.hero-title {
  font-size: var(--font-hero);
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.2;
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
  display: flex; gap: var(--space-md); justify-content: center; flex-wrap: wrap;
}
.hero-btn {
  background: var(--color-brand); border-color: var(--color-brand);
  font-weight: 600; padding: 12px 36px; font-size: 15px;
}
.hero-btn:hover { background: var(--color-brand-hover); border-color: var(--color-brand-hover); }
.hero-btn-outline {
  background: transparent; border: 1px solid rgba(255,255,255,0.3); color: #FFFFFF;
  font-weight: 600; padding: 12px 36px; font-size: 15px;
}
.hero-btn-outline:hover { border-color: #FFFFFF; color: #FFFFFF; }

/* Dots */
.hc-dots {
  position: absolute; bottom: 60px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 8px; z-index: 2;
}
.hc-dot {
  width: 32px; height: 3px; background: rgba(255,255,255,0.3);
  border-radius: 2px; cursor: pointer; overflow: hidden;
  transition: background var(--transition-fast);
}
.hc-dot.active { background: rgba(255,255,255,0.5); }
.hc-dot-fill {
  display: block; height: 100%; width: 100%; background: var(--color-brand);
  animation: dot-fill 5s linear infinite;
  transform-origin: left;
}
@keyframes dot-fill { from { transform: scaleX(0); } to { transform: scaleX(1); } }

/* Arrows */
.hc-arrow {
  position: absolute; top: 50%; transform: translateY(-50%); z-index: 2;
  background: rgba(0,0,0,0.4); border: none; color: #fff;
  width: 44px; height: 44px; border-radius: 50%; font-size: 28px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity var(--transition-normal);
}
.hc-prev { left: 20px; }
.hc-next { right: 20px; }
.hero-carousel:hover .hc-arrow { opacity: 1; }
.hc-arrow:hover { background: var(--color-brand); }

/* Spotlight */
.hc-spotlight {
  position: absolute; inset: 0; z-index: 2; pointer-events: none;
  transition: opacity 0.3s;
}

/* Title shimmer */
@keyframes shimmer {
  0%   { background-position: -200% center; }
  100% { background-position: 200% center; }
}
.hero-title .hero-red {
  background: linear-gradient(90deg, #D93831 20%, #FF6B6B 50%, #D93831 80%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: shimmer 3s linear infinite;
}

/* Stats bar */
.hero-stats {
  position: absolute; bottom: 0; left: 0; right: 0; z-index: 2;
  display: flex; justify-content: center; gap: var(--space-xl);
  padding: 12px 0; background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
}
.hero-stat {
  text-align: center; padding: 0 var(--space-lg);
  border-right: 1px solid rgba(255,255,255,0.12);
}
.hero-stat:last-child { border-right: none; }
.hs-num { display: block; font-size: 20px; font-weight: 700; color: var(--color-brand); }
.hs-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 2px; }

/* ---- Sections ---- */
.section { padding: var(--space-2xl) 0; }
.section-action { text-align: center; margin-top: var(--space-xl); }

/* ---- Features ---- */
.section-full {
  padding: var(--space-2xl) 0;
  width: 100vw;
  margin-left: -50vw;
  margin-right: -50vw;
  position: relative;
  left: 50%;
  right: 50%;
}
.section-full > * {
  padding-left: var(--page-padding-x);
  padding-right: var(--page-padding-x);
}

.feature-grid {
  margin-top: var(--space-lg);
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
}

@media (max-width: 991px) { .feature-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px) { .feature-grid { grid-template-columns: 1fr; } }
.feature-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.1);
}
.feature-icon-box {
  width: 64px; height: 64px;
  border-radius: 16px;
  background: #FF8C42;
  color: #FFFFFF;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(255,140,66,0.3);
}
.feature-label {
  font-size: 18px; font-weight: 700; color: var(--color-text-title);
  margin: 0 0 8px;
}
.feature-desc {
  font-size: 14px; color: var(--color-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0 0 20px;
  flex: 1;
}
.feature-btn {
  background: #FF8C42;
  border-color: #FF8C42;
  color: #FFFFFF;
  border-radius: 20px;
  padding: 8px 24px;
  font-size: 14px;
  font-weight: 500;
  margin-top: auto;
}
.feature-btn:hover { background: #E67A30; border-color: #E67A30; color: #FFFFFF; }

@media (max-width: 991px) {
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 767px) {
  .feature-grid { grid-template-columns: 1fr; }
}

/* ---- Section Dark (shared background for Features / Courses / Coaches) ---- */
.section-dark {
  background: url(https://pic1.zhimg.com/v2-dfd265c6ec802ec01f6432ccdc4be4b5_1440w.jpg?source=172ae18b) center / cover no-repeat;
  background-attachment: fixed;
  position: relative;
  isolation: isolate;
}
.section-dark::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  background: rgba(0, 0, 0, 0.75);
}
.section-dark > * {
  position: relative;
  z-index: 1;
}
.section-dark .section-title { color: #FFFFFF; justify-content: center; }
.section-dark .section-subtitle { color: rgba(255,255,255,0.7); text-align: center; }

/* ---- Coach Section ---- */
.coach-section {
  padding-top: 60px;
  padding-bottom: 60px;
}
.coach-header {
  text-align: center;
  margin-bottom: 48px;
}
.coach-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
@media (max-width: 991px) { .coach-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px) { .coach-grid { grid-template-columns: 1fr; } }

.trainer-card {
  background: #FFFFFF;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
}
.trainer-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.1);
}

/* Image area — 4:5 portrait */
.trainer-img-box {
  aspect-ratio: 4 / 5;
  overflow: hidden;
  background: #F0F0F0;
  position: relative;
}
.trainer-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.5s ease;
}
.trainer-card:hover .trainer-img { transform: scale(1.05); }

.trainer-img-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 700;
  color: #C0C0C0;
  background: #F0F0F0;
}

/* Text body */
.trainer-body {
  padding: 20px;
  text-align: center;
}
.trainer-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-title);
  margin: 0 0 4px;
}
.trainer-role {
  font-size: 14px;
  color: #FF8C42;
  font-weight: var(--font-weight-medium);
  margin: 0 0 10px;
}
.trainer-bio {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.trainer-stat {
  font-size: 12px;
  color: var(--color-text-weak);
  margin: 0;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

/* Course stagger entrance (scroll-driven, no CSS transition) */
.course-stagger {
  opacity: 0;
  transform: translateY(30px) translateZ(0);
  will-change: opacity, transform;
}
.course-stagger.visible {
  opacity: 1;
  transform: translateY(0) translateZ(0);
}

/* ---- Course Cards (poster style) ---- */
.courses-header {
  display: flex; align-items: flex-start; justify-content: center;
  margin-bottom: var(--space-lg); flex-wrap: wrap; gap: var(--space-md);
}
.courses-header .section-title,
.courses-header .section-subtitle { margin-bottom: 0; }
.courses-header > div { text-align: center; }
.view-all-btn { flex-shrink: 0; position: absolute; right: var(--page-padding-x); }
.section-dark .courses-header { position: relative; }
.course-grid {
  min-height: 100px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-lg);
}
.course-card {
  cursor: pointer; border-radius: 12px; overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  transform: translateZ(0);
  will-change: transform;
}
.course-card:hover { transform: translateY(-4px); box-shadow: 0 8px 32px rgba(217,56,49,0.15), 0 4px 12px rgba(0,0,0,0.1); }
.course-cover {
  position: relative; height: 260px; background: var(--color-bg-dark);
  display: flex; align-items: flex-end; overflow: hidden;
}
.course-cover img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.6s var(--ease-out); }
.course-card:hover .course-cover img { transform: scale(1.08); }
.course-cover-gradient {
  position: absolute; inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.1) 50%, rgba(0,0,0,0) 100%);
  z-index: 1;
}
.course-cover-info {
  position: relative; z-index: 2;
  padding: 20px; width: 100%;
}
.course-cover-tags {
  display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px;
}
.course-title {
  font-size: 17px; font-weight: 700; color: #FFFFFF;
  margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  text-shadow: 0 1px 4px rgba(0,0,0,0.5);
}
.course-meta { display: flex; justify-content: space-between; align-items: baseline; }
.course-price { color: var(--color-brand); font-weight: 700; font-size: 18px; text-shadow: 0 1px 4px rgba(0,0,0,0.5); }
.course-duration { font-size: 12px; color: rgba(255,255,255,0.7); }

/* Skeleton card */
.skeleton-card { pointer-events: none; min-height: 260px; }
.skeleton-card .course-cover { background: #e8e8e8; animation: skeleton-shimmer 1.5s ease-in-out infinite; }
@keyframes skeleton-shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

@media (max-width: 991px) { .course-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px) { .course-grid { grid-template-columns: 1fr; } .course-cover { height: 220px; } }

/* ---- CTA Banner ---- */
.cta-banner {
  position: relative;
  left: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  width: 100vw;
  background-color: var(--color-bg-dark);
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  padding: var(--space-2xl) 0;
  text-align: center;
  isolation: isolate;
}
.cta-overlay {
  position: absolute; inset: 0; z-index: 0;
  background: rgba(0, 0, 0, 0.75);
}
.cta-content { position: relative; z-index: 1; }
.cta-title { font-size: var(--font-h1); font-weight: var(--font-weight-bold); color: #FFFFFF; margin: 0 0 var(--space-md); }
.cta-red { color: var(--color-brand); }
.cta-desc { font-size: 15px; color: var(--color-text-on-dark-secondary); margin: 0 0 var(--space-xl); }
.cta-btn {
  background: var(--color-brand); border-color: var(--color-brand);
  font-weight: var(--font-weight-medium); padding: 14px 48px; font-size: 16px;
}
.cta-btn:hover { background: var(--color-brand-hover); border-color: var(--color-brand-hover); }

/* ---- Particles ---- */
@keyframes float-up {
  0%   { transform: translateY(0) translateX(0) scale(1); opacity: 0; }
  10%  { opacity: 0.6; }
  90%  { opacity: 0.6; }
  100% { transform: translateY(-400px) translateX(40px) scale(0); opacity: 0; }
}
.particles {
  position: absolute; inset: 0; overflow: hidden; pointer-events: none; z-index: 1;
}
.particle {
  position: absolute;
  width: 2px; height: 2px;
  background: rgba(217, 56, 49, 0.4);
  border-radius: 50%;
  animation: float-up 8s linear infinite;
}
.particle:nth-child(1)  { left: 10%; animation-delay: 0s; animation-duration: 7s; }
.particle:nth-child(2)  { left: 25%; animation-delay: 1s; animation-duration: 9s; }
.particle:nth-child(3)  { left: 40%; animation-delay: 2s; animation-duration: 6s; }
.particle:nth-child(4)  { left: 55%; animation-delay: 3s; animation-duration: 10s; }
.particle:nth-child(5)  { left: 70%; animation-delay: 4s; animation-duration: 8s; }
.particle:nth-child(6)  { left: 85%; animation-delay: 5s; animation-duration: 7s; }
.particle:nth-child(7)  { left: 15%; animation-delay: 6s; animation-duration: 9s; }
.particle:nth-child(8)  { left: 50%; animation-delay: 0.5s; animation-duration: 11s; }
.particle:nth-child(9)  { left: 75%; animation-delay: 2.5s; animation-duration: 8s; }
.particle:nth-child(10) { left: 35%; animation-delay: 4.5s; animation-duration: 10s; }

/* ---- Responsive ---- */
@media (max-width: 767px) {
  .hero-carousel { height: 100vh; margin-top: calc(-1 * var(--nav-height)); margin-left: -50vw; margin-right: -50vw; width: 100vw; }
  .hero-title { font-size: 28px; }
  .hero-stats { gap: var(--space-md); }
  .hero-stat { padding: 0 var(--space-md); }
  .hs-num { font-size: 16px; }
  .section { padding-top: var(--space-xl); }
}
</style>
