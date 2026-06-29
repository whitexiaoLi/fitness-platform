<template>
  <div class="login-wrapper">
    <!-- Header -->
    <header class="login-header" :class="{ 'header-glow': headerGlow }">
      <span class="header-logo">FITNESS</span>
      <span class="header-lang">中文</span>
    </header>

    <!-- Main -->
    <div class="login-main">
      <!-- Left: Visual -->
      <section class="visual" ref="visualRef" @mousemove="onVisualMove" @mouseleave="onVisualLeave">
        <!-- Diffuse light orb -->
        <div class="geo-orb" :style="{ transform: `translate(${parallaxX * 15}px, ${parallaxY * 10}px)` }" />
        <!-- Floating geometric blocks -->
        <div class="geo-block geo-b1" :style="{ transform: `translate(${parallaxX * 25}px, ${parallaxY * 20}px) rotate(15deg)` }" />
        <div class="geo-block geo-b2" :style="{ transform: `translate(${parallaxX * 20}px, ${parallaxY * 15}px) rotate(25deg)` }" />
        <div class="geo-diamond" :style="{ transform: `translate(${parallaxX * 30}px, ${parallaxY * 25}px) rotate(${parallaxX * 10}deg)` }" />

        <div class="visual-text">
          <h1 class="visual-title">FIT<span class="visual-accent">N</span>ESS</h1>
          <p class="visual-sub">专业 · 科学 · 高效</p>
        </div>
      </section>

      <!-- Right: Form -->
      <section class="form-section">
        <div class="form-card" ref="cardRef">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-sub">登录你的账号继续训练</p>
          </div>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" @keyup.enter="handleLogin" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <p class="form-tip">还没有账号？<router-link to="/register" class="form-link">立即注册</router-link></p>
        </div>
      </section>
    </div>

    <!-- Footer -->
    <footer class="login-footer">
      <span>© 2026 FITNESS. All rights reserved.</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useTilt3D } from '@/composables/useTilt3D'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// ---- 3D tilt on form card ----
const cardRef = ref(null)
useTilt3D(cardRef, { maxDeg: 6, scale: 1.02 })

// ---- Mouse parallax on left visual ----
const visualRef = ref(null)
const parallaxX = ref(0)
const parallaxY = ref(0)
function onVisualMove(e) {
  const rect = visualRef.value?.getBoundingClientRect()
  if (!rect) return
  parallaxX.value = ((e.clientX - rect.left) / rect.width - 0.5) * 2
  parallaxY.value = ((e.clientY - rect.top) / rect.height - 0.5) * 2
}
function onVisualLeave() { parallaxX.value = 0; parallaxY.value = 0 }

// ---- Header glow ----
const headerGlow = ref(false)
onMounted(() => { headerGlow.value = true })

async function handleLogin() {
  loading.value = true
  try {
    const res = await userStore.doLogin(form.username, form.password)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      const to = await router.replace('/')
      if (!to) window.location.href = '/'
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ---- Wrapper ---- */
.login-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #08080A;
  color: #FFFFFF;
}

/* ---- Header ---- */
.login-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 40px;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}
.header-logo {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 4px;
  color: rgba(255,255,255,0.8);
}
.header-lang {
  font-size: 13px;
  color: rgba(255,255,255,0.4);
  cursor: pointer;
}
.header-glow .header-logo {
  animation: logoBreath 3s ease-in-out infinite;
}
@keyframes logoBreath {
  0%, 100% { text-shadow: 0 0 0 rgba(217,56,49,0); }
  50% { text-shadow: 0 0 20px rgba(217,56,49,0.3); }
}

/* ---- Main ---- */
.login-main {
  flex: 1;
  display: grid;
  grid-template-columns: 3fr 2fr;
  min-height: 0;
}
@media (max-width: 767px) {
  .login-main { grid-template-columns: 1fr; }
}

/* ========== LEFT: Visual ========== */
.visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  isolation: isolate;
  background:
    radial-gradient(ellipse 600px 500px at 30% 40%, rgba(217,56,49,0.18) 0%, transparent 70%),
    radial-gradient(ellipse 400px 300px at 70% 70%, rgba(217,56,49,0.08) 0%, transparent 70%),
    #08080A;
  min-height: 400px;
}

/* Diffuse orb */
.geo-orb {
  position: absolute;
  width: 340px;
  height: 340px;
  border-radius: 50%;
  background: rgba(217,56,49,0.15);
  filter: blur(80px);
  top: 25%;
  left: 20%;
  animation: orbDrift 12s ease-in-out infinite;
}
@keyframes orbDrift {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(30px, -20px); }
  66% { transform: translate(-20px, 15px); }
}

/* Geometric blocks */
.geo-block {
  position: absolute;
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.06);
  background: rgba(255,255,255,0.03);
  animation: floatBlock 8s ease-in-out infinite;
}
.geo-b1 {
  width: 100px; height: 100px;
  top: 20%; right: 15%;
  transform: rotate(15deg);
  animation-delay: 0s;
}
.geo-b2 {
  width: 70px; height: 70px;
  bottom: 25%; left: 10%;
  transform: rotate(25deg);
  animation-delay: -3s;
}
@keyframes floatBlock {
  0%, 100% { transform: translateY(0) rotate(15deg); }
  50% { transform: translateY(-16px) rotate(17deg); }
}

/* Diamond */
.geo-diamond {
  position: absolute;
  width: 60px; height: 60px;
  background: linear-gradient(135deg, rgba(217,56,49,0.3), rgba(217,56,49,0.06));
  clip-path: polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%);
  top: 55%; right: 25%;
  animation: floatBlock 6s ease-in-out infinite;
  animation-delay: -1.5s;
}

/* Text */
.visual-text {
  position: relative; z-index: 2;
  text-align: center;
}
.visual-title {
  font-size: 64px;
  font-weight: 800;
  letter-spacing: 8px;
  color: #FFFFFF;
  margin: 0 0 16px;
  line-height: 1;
}
.visual-accent {
  background: linear-gradient(135deg, #D93831, #FF6B6B);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.visual-sub {
  font-size: 17px;
  color: rgba(255,255,255,0.45);
  letter-spacing: 6px;
  margin: 0;
}

/* ========== RIGHT: Form ========== */
.form-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
  position: relative;
  z-index: 2;
}
.form-card {
  width: 100%;
  max-width: 400px;
  background: #FFFFFF;
  border-radius: 20px;
  padding: 48px 40px 40px;
  box-shadow:
    0 20px 60px rgba(0,0,0,0.4),
    0 0 0 1px rgba(0,0,0,0.05);
}
.form-header { margin-bottom: 32px; }
.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 8px;
}
.form-sub {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* Input overrides */
.form-card :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #E5E5E5;
  border-radius: 10px;
  transition: box-shadow 0.3s ease;
}
.form-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #CCC;
}
.form-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(217,56,49,0.3), 0 0 0 1px var(--color-brand);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  margin-top: 4px;
  border-radius: 10px;
  background: var(--color-brand);
  border-color: var(--color-brand);
}
.login-btn:hover { background: var(--color-brand-hover); border-color: var(--color-brand-hover); }
.login-btn.is-loading {
  animation: btnPulse 1.5s ease-in-out infinite;
}
@keyframes btnPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.form-tip {
  text-align: center;
  font-size: 13px;
  color: #999;
  margin: 24px 0 0;
}
.form-link { color: var(--color-brand); font-weight: 500; text-decoration: none; }
.form-link:hover { text-decoration: underline; }

/* ========== FOOTER ========== */
.login-footer {
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: rgba(255,255,255,0.2);
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}
</style>
