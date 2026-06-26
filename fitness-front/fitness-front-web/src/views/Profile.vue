<template>
  <div class="profile">
    <h2 class="page-title">个人资料</h2>
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card shadow="hover" class="info-card">
          <div class="avatar-section">
            <el-avatar :size="80" :src="form.avatarUrl" icon="UserFilled" />
            <h3>{{ form.nickname || userStore.user?.nickname }}</h3>
            <el-tag :type="roleTag" size="small">{{ roleLabel }}</el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-row"><span class="label">用户名</span><span class="value">@{{ userStore.user?.username }}</span></div>
            <div class="info-row"><span class="label">手机号</span><span class="value">{{ form.phone || '未填写' }}</span></div>
            <div class="info-row"><span class="label">注册时间</span><span class="value">{{ fmt(userStore.user?.createTime) }}</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>编辑资料</span></template>
          <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" maxlength="50" />
            </el-form-item>
            <el-form-item label="头像">
              <el-input v-model="form.avatarUrl" placeholder="头像图片 URL" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="选填" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card shadow="hover" style="margin-top: 16px">
          <template #header><span>修改密码</span></template>
          <el-form :model="pwForm" :rules="pwRules" ref="pwFormRef" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPw">
              <el-input v-model="pwForm.confirmPw" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="handleChangePw" :loading="changingPw">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const saving = ref(false)
const changingPw = ref(false)
const formRef = ref(null)
const pwFormRef = ref(null)

const form = reactive({
  nickname: userStore.user?.nickname || '',
  avatarUrl: userStore.user?.avatarUrl || '',
  phone: userStore.user?.phone || '',
})
const pwForm = reactive({ oldPassword: '', newPassword: '', confirmPw: '' })

const rules = { nickname: [{ required: true, message: '请输入昵称' }] }
const pwRules = {
  oldPassword: [{ required: true, message: '请输入旧密码' }],
  newPassword: [{ required: true, min: 6, message: '密码至少6位' }],
  confirmPw: [{ required: true, validator: (r, v, cb) => v === pwForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }],
}

const roleTag = computed(() => ({ ADMIN: 'danger', COACH: 'warning', USER: 'info' }[userStore.user?.role] || 'info'))
const roleLabel = computed(() => ({ ADMIN: '管理员', COACH: '教练', USER: '普通用户' }[userStore.user?.role] || '用户'))

function fmt(t) { return t ? t.replace('T', ' ') : '-' }

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = await request.put('/user/profile', { nickname: form.nickname, avatarUrl: form.avatarUrl, phone: form.phone })
    if (res.code === 200) {
      userStore.user = { ...userStore.user, ...res.data }
      localStorage.setItem('user', JSON.stringify(userStore.user))
      ElMessage.success('资料已更新')
    }
  } catch (e) { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

async function handleChangePw() {
  const valid = await pwFormRef.value.validate().catch(() => false)
  if (!valid) return
  changingPw.value = true
  try {
    const res = await request.put('/user/password', { oldPassword: pwForm.oldPassword, newPassword: pwForm.newPassword })
    if (res.code === 200) {
      ElMessage.success('密码已修改，请重新登录')
      setTimeout(() => userStore.logout(), 1500)
    }
  } catch (e) { ElMessage.error(e.response?.data?.message || '修改失败') }
  finally { changingPw.value = false }
}
</script>

<style scoped>
.page-title { margin: 0 0 20px; font-size: 20px; }
.avatar-section { text-align: center; }
.avatar-section h3 { margin: 12px 0 8px; font-size: 18px; }
.info-card { height: 100%; }
.info-list { display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; justify-content: space-between; font-size: 14px; }
.info-row .label { color: #999; }
.info-row .value { color: #333; }
</style>
