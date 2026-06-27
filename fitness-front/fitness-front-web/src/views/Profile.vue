<template>
  <div class="profile">
    <h2 class="page-title">个人资料</h2>
    <el-row :gutter="24">
      <el-col :sm="24" :md="8">
        <div class="profile-sidebar">
          <div class="avatar-card">
            <div class="avatar-ring">
              <img v-if="userStore.user?.avatarUrl" :src="userStore.user?.avatarUrl" class="sidebar-avatar-img" />
              <el-avatar v-else :size="88" icon="UserFilled" />
            </div>
            <h3 class="profile-name">{{ userStore.user?.nickname }}</h3>
            <el-tag :type="roleTag" size="small">{{ roleLabel }}</el-tag>
          </div>
          <div class="info-strip">
            <div class="info-row"><span class="label">用户名</span><span class="value">@{{ userStore.user?.username }}</span></div>
            <div class="info-row"><span class="label">手机</span><span class="value">{{ userStore.user?.phone || '未填写' }}</span></div>
            <div class="info-row"><span class="label">身高</span><span class="value">{{ userStore.user?.height ? userStore.user.height + ' cm' : '未填写' }}</span></div>
            <div class="info-row"><span class="label">加入于</span><span class="value">{{ fmt(userStore.user?.createTime) }}</span></div>
          </div>
        </div>
      </el-col>
      <el-col :sm="24" :md="16">
        <el-card shadow="hover">
          <template #header><span>编辑资料</span></template>
          <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" maxlength="50" />
            </el-form-item>
            <el-form-item label="头像">
              <div class="avatar-upload">
                <img v-if="avatarPreview" :src="avatarPreview" class="avatar-img" @error="avatarPreview=''" />
                <el-avatar v-else :size="64" icon="UserFilled" class="avatar-preview" />
                <el-upload
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  name="file"
                  accept="image/*"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="onAvatarSuccess"
                  :on-error="onAvatarError"
                >
                  <el-button size="small" type="primary">选择图片上传</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="选填" />
            </el-form-item>
            <el-form-item label="身高(cm)">
              <el-input-number v-model="form.height" :min="100" :max="250" :precision="1" style="width:100%" placeholder="用于自动计算BMI" />
            </el-form-item>

            <!-- Coach fields -->
            <template v-if="isCoach">
              <el-divider>教练资料</el-divider>
              <el-form-item label="个人简介">
                <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="介绍你的训练理念和专业背景" />
              </el-form-item>
              <el-form-item label="专业认证">
                <el-input v-model="form.certifications" placeholder="如：NSCA-CPT, ACE, 国家职业资格" />
              </el-form-item>
              <el-form-item label="擅长领域">
                <el-input v-model="form.specialties" placeholder="如：增肌、减脂、产后恢复、运动康复" />
              </el-form-item>
              <el-form-item label="从业年限">
                <el-input-number v-model="form.experience" :min="0" :max="50" style="width:100%" />
              </el-form-item>
              <el-form-item label="时薪(¥)">
                <el-input-number v-model="form.hourlyRate" :min="0" :precision="0" style="width:100%" />
              </el-form-item>
            </template>

            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" style="margin-top:16px">
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

// Avatar upload
const uploadUrl = '/api/upload/avatar'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('accessToken')
  return token ? { Authorization: 'Bearer ' + token } : {}
})
const avatarPreview = ref(userStore.user?.avatarUrl || '')

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { ElMessage.error('图片大小不能超过5MB'); return false }
  return true
}

function onAvatarSuccess(res) {
  if (res.code === 200) {
    form.avatarUrl = res.data.url
    avatarPreview.value = res.data.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function onAvatarError() {
  ElMessage.error('上传失败，请检查网络')
}

const isCoach = computed(() => userStore.user?.role === 'COACH')

const form = reactive({
  nickname: userStore.user?.nickname || '',
  avatarUrl: userStore.user?.avatarUrl || '',
  phone: userStore.user?.phone || '',
  height: userStore.user?.height || null,
  bio: userStore.user?.bio || '',
  certifications: userStore.user?.certifications || '',
  specialties: userStore.user?.specialties || '',
  experience: userStore.user?.experience || null,
  hourlyRate: userStore.user?.hourlyRate || null,
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

function fmt(t) { return t ? t.replace('T', ' ').slice(0, 10) : '-' }

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      nickname: form.nickname,
      avatarUrl: form.avatarUrl,
      phone: form.phone,
      height: form.height != null ? String(form.height) : null,
    }
    if (isCoach.value) {
      Object.assign(payload, {
        bio: form.bio, certifications: form.certifications, specialties: form.specialties,
        experience: form.experience != null ? String(form.experience) : null,
        hourlyRate: form.hourlyRate != null ? String(form.hourlyRate) : null,
      })
    }
    const res = await request.put('/user/profile', payload)
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
.profile-sidebar { position: sticky; top: calc(var(--nav-height) + 24px); }
.avatar-card {
  background: var(--color-bg-page);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-xl) var(--space-lg);
  text-align: center;
}
.avatar-ring {
  display: inline-flex;
  padding: 3px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-brand), #FF6B6B);
}
.avatar-ring :deep(.el-avatar) { border: 3px solid #fff; }
.sidebar-avatar-img { width: 88px; height: 88px; border-radius: 50%; object-fit: cover; border: 3px solid #fff; }
.profile-name { margin: 12px 0 6px; font-size: 18px; font-weight: 600; color: var(--color-text-title); }

.info-strip {
  margin-top: var(--space-md);
  background: var(--color-bg-page);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-md);
}
.info-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 13px; }
.info-row + .info-row { border-top: 1px solid var(--color-border-light); }
.info-row .label { color: var(--color-text-secondary); }
.info-row .value { color: var(--color-text-title); font-weight: 500; }

.avatar-upload { display: flex; align-items: center; gap: 12px; }
.avatar-preview { flex-shrink: 0; }
.avatar-img { width: 64px; height: 64px; border-radius: 50%; object-fit: cover; border: 2px solid var(--color-border); }

@media (max-width: 991px) {
  .profile-sidebar { position: static; margin-bottom: 16px; }
}
</style>
