<template>
  <div class="course-detail" v-loading="loading">
    <el-button text @click="$router.back()" style="margin-bottom:12px">← 返回课程列表</el-button>

    <template v-if="course">
      <!-- Header -->
      <el-row :gutter="24">
        <el-col :span="14">
          <el-card shadow="hover" class="cover-card">
            <img v-if="course.coverUrl" :src="course.coverUrl" alt="" class="cover-img" />
            <div v-else class="cover-fallback">📹</div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="hover">
            <h2 class="title">{{ course.title }}</h2>
            <div class="tags">
              <el-tag size="small">{{ catLabel(course.category) }}</el-tag>
              <el-tag size="small" :type="diffTag(course.difficulty)">{{ diffLabel(course.difficulty) }}</el-tag>
              <el-tag size="small" type="warning" v-if="subscribed">已订阅</el-tag>
            </div>
            <div class="rating-line" v-if="course.rating">
              <el-rate :model-value="Number(course.rating)" disabled show-score text-color="#f56c6c" />
              <span class="rating-count">({{ course.ratingCount || 0 }} 评价)</span>
            </div>
            <div class="info-list">
              <div class="info-row"><span>教练</span><span>{{ course.coachName || '教练 #' + course.coachId }}</span></div>
              <div class="info-row"><span>时长</span><span>{{ course.duration || 0 }} 分钟</span></div>
              <div class="info-row"><span>动作数</span><span>{{ course.exercises?.length || 0 }} 个</span></div>
              <div class="info-row"><span>价格</span><span class="price">¥{{ course.price || 0 }}</span></div>
              <div class="info-row" v-if="subscribed">
                <span>学习进度</span>
                <el-progress :percentage="course.progress || 0" :stroke-width="8" style="width:120px" />
              </div>
            </div>
            <el-button type="primary" size="large" style="width:100%;margin-top:16px" @click="handleSubscribe" :loading="subscribing" :disabled="subscribed">
              {{ subscribed ? '已订阅' : '立即订阅' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>

      <!-- Tabs: Intro / Exercises -->
      <el-card shadow="hover" style="margin-top:20px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="课程介绍" name="intro">
            <div class="desc">{{ course.description || '暂无介绍' }}</div>
          </el-tab-pane>
          <el-tab-pane label="动作教学" name="exercises">
            <div v-if="course.exercises?.length">
              <el-row :gutter="20">
                <el-col :span="14">
                  <div v-if="selectedExercise?.videoUrl && isExercisePath(selectedExercise.videoUrl)" class="flip-box">
                    <img :src="selectedExercise.videoUrl + '0.jpg'" :alt="selectedExercise.title" class="flip-img img-start" />
                    <img :src="selectedExercise.videoUrl + '1.jpg'" :alt="selectedExercise.title" class="flip-img img-end" />
                  </div>
                  <div v-else-if="selectedExercise?.videoUrl && isImageUrl(selectedExercise.videoUrl)" class="image-box">
                    <img :src="selectedExercise.videoUrl" :alt="selectedExercise.title" class="demo-img" />
                  </div>
                  <div class="video-box" v-else-if="selectedExercise?.videoUrl && !isImageUrl(selectedExercise.videoUrl)">
                    <iframe
                      :src="selectedExercise.videoUrl"
                      frameborder="0"
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                      allowfullscreen
                      class="video-frame"
                    />
                  </div>
                  <div v-else class="video-placeholder">
                    <span>📹</span>
                    <p>该动作暂无演示</p>
                  </div>
                  <div class="exercise-desc" v-if="selectedExercise">
                    <h4>📖 {{ selectedExercise.title }} - 动作讲解</h4>
                    <div class="desc">{{ selectedExercise.description }}</div>
                  </div>
                </el-col>
                <el-col :span="10">
                  <div class="exercise-list">
                    <div
                      v-for="(ex, i) in course.exercises"
                      :key="ex.id"
                      class="exercise-item"
                      :class="{ active: selectedExercise?.id === ex.id }"
                      @click="selectedExercise = ex"
                    >
                      <span class="ex-num">{{ i + 1 }}</span>
                      <div class="ex-info">
                        <span class="ex-title">{{ ex.title }}</span>
                        <span class="ex-hint" v-if="ex.videoUrl">📹 有视频</span>
                      </div>
                      <el-icon v-if="selectedExercise?.id === ex.id" color="#667eea"><VideoPlay /></el-icon>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
            <el-empty v-else description="暂无动作教学" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const course = ref(null)
const loading = ref(false)
const subscribing = ref(false)
const subscribed = ref(false)
const activeTab = ref('intro')
const selectedExercise = ref(null)

function isImageUrl(url) {
  return /\.(jpg|jpeg|png|gif|webp)(\?|$)/i.test(url)
}
function isExercisePath(url) {
  return /\/exercises\/[^/]+\/$/.test(url)
}

const categories = [
  { label: '增肌', value: 'STRENGTH' }, { label: '减脂', value: 'FAT_LOSS' },
  { label: '瑜伽', value: 'YOGA' }, { label: '有氧', value: 'CARDIO' },
  { label: '拉伸', value: 'STRETCHING' }, { label: '形体纠正', value: 'POSTURE' },
]
const catLabel = (c) => categories.find(x => x.value === c)?.label || c
const diffTag = (d) => ({ BEGINNER: 'success', INTERMEDIATE: 'warning', ADVANCED: 'danger' }[d] || 'info')
const diffLabel = (d) => ({ BEGINNER: '初级', INTERMEDIATE: '中级', ADVANCED: '高级' }[d] || d)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/courses/${route.params.id}`)
    if (res.code === 200) {
      course.value = res.data
      subscribed.value = res.data.progress !== undefined && res.data.progress !== null
      if (res.data.exercises?.length) selectedExercise.value = res.data.exercises[0]
    }
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
})

async function handleSubscribe() {
  subscribing.value = true
  try {
    const res = await request.post(`/courses/${route.params.id}/subscribe`)
    if (res.code === 200) { ElMessage.success('订阅成功'); subscribed.value = true }
  } catch (e) { ElMessage.error(e.response?.data?.message || '订阅失败') }
  finally { subscribing.value = false }
}
</script>

<style scoped>
.cover-card :deep(.el-card__body) { padding: 0; }
.cover-img { width: 100%; max-height: 400px; object-fit: cover; display: block; border-radius: 8px; }
.cover-fallback { height: 300px; display: flex; align-items: center; justify-content: center; font-size: 80px; background: #f0f2f5; border-radius: 8px; }

.title { margin: 0 0 10px; font-size: 22px; }
.tags { display: flex; gap: 8px; margin-bottom: 10px; }
.rating-line { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.rating-count { font-size: 12px; color: #999; }

.info-list { display: flex; flex-direction: column; gap: 8px; }
.info-row { display: flex; justify-content: space-between; font-size: 14px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.info-row span:first-child { color: #999; }
.price { color: #f56c6c; font-weight: 600; font-size: 18px; }

.video-box {
  border-radius: 12px;
  overflow: hidden;
  background: #000;
  aspect-ratio: 16/9;
}
.video-frame { width: 100%; height: 100%; border: none; }
.image-box {
  border-radius: 12px;
  overflow: hidden;
  background: #f0f2f5;
  aspect-ratio: 16/9;
  display: flex;
  align-items: center;
  justify-content: center;
}
.demo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.flip-box {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #f0f2f5;
  aspect-ratio: 16/9;
  cursor: pointer;
}
.flip-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: opacity 0.8s ease-in-out;
}
.img-start {
  opacity: 1;
  animation: kenBurns 4s ease-in-out infinite;
}
.img-end {
  opacity: 0;
  animation: kenBurns 4s ease-in-out 2s infinite;
}

.flip-box:hover .img-start { opacity: 0; }
.flip-box:hover .img-end   { opacity: 1; }

@keyframes kenBurns {
  0%, 100% { transform: scale(1); }
  50%      { transform: scale(1.04); }
}
.video-placeholder {
  aspect-ratio: 16/9;
  background: #f0f2f5;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 48px;
}
.video-placeholder p { font-size: 14px; margin: 8px 0 0; }

.exercise-desc { margin-top: 16px; }
.exercise-desc h4 { margin: 0 0 8px; font-size: 16px; }

.exercise-list { display: flex; flex-direction: column; gap: 6px; max-height: 480px; overflow-y: auto; }
.exercise-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.exercise-item:hover { background: #f5f7fa; }
.exercise-item.active { background: #ecf5ff; border-color: #667eea; }
.ex-num {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  flex-shrink: 0;
}
.exercise-item.active .ex-num { background: #667eea; color: #fff; }
.ex-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.ex-title { font-size: 14px; font-weight: 500; }
.ex-hint { font-size: 11px; color: #999; }

.desc { line-height: 1.8; color: #555; white-space: pre-wrap; }
</style>
