<template>
  <div class="register-page">
    <div class="register-overlay" />
    <div class="register-card">
      <div class="register-header">
        <h2 class="register-logo">FITNESS</h2>
        <p class="register-subtitle">创建你的健身账号</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-50位）" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-100位）" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.phone" placeholder="手机号（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" class="register-btn">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <p class="register-tip">已有账号？<router-link to="/login" class="register-link">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

async function handleRegister() {
  loading.value = true
  try {
    const res = await userStore.doRegister(form)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      await router.replace('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (e) {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url(https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=1400&q=80) center / cover no-repeat fixed;
  position: relative;
  isolation: isolate;
  padding: 20px;
}
.register-overlay {
  position: absolute; inset: 0; z-index: 0;
  background: rgba(0, 0, 0, 0.75);
}
.register-card {
  position: relative; z-index: 1;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 48px 40px 36px;
  width: 400px;
  max-width: 100%;
  box-shadow: 0 8px 40px rgba(0,0,0,0.3);
}
.register-header { text-align: center; margin-bottom: 32px; }
.register-logo {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-title);
  letter-spacing: 3px;
  margin: 0 0 8px;
}
.register-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}
.register-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: var(--color-brand);
  border-color: var(--color-brand);
}
.register-btn:hover { background: var(--color-brand-hover); border-color: var(--color-brand-hover); }
.register-tip {
  text-align: center;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 20px 0 0;
}
.register-link { color: var(--color-brand); font-weight: 500; text-decoration: none; }
.register-link:hover { text-decoration: underline; }
</style>
