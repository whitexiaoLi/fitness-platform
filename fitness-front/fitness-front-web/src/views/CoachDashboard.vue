<template>
  <div class="coach-dashboard">
    <h2 class="page-title">📋 教练面板</h2>
    <el-tabs v-model="activeTab">
      <!-- Tab 1: My Courses -->
      <el-tab-pane label="我的课程" name="courses">
        <el-button type="primary" @click="showCourseDialog()" style="margin-bottom:16px">创建课程</el-button>
        <el-card shadow="hover" v-loading="courseLoading">
          <el-table :data="courses" stripe size="default">
            <template #empty><el-empty description="暂无课程" /></template>
            <el-table-column prop="title" label="课程名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="分类" width="100"><template #default="{ row }">{{ catLabel(row.category) }}</template></el-table-column>
            <el-table-column label="难度" width="100"><template #default="{ row }">{{ row.difficulty }}</template></el-table-column>
            <el-table-column prop="duration" label="时长(分)" width="90" />
            <el-table-column prop="price" label="价格" width="90" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button size="small" @click="showCourseDialog(row)">编辑</el-button>
                <el-popconfirm title="确认删除？" @confirm="handleDeleteCourse(row)">
                  <template #reference><el-button size="small" type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- Tab 2: Students -->
      <el-tab-pane label="学员管理" name="students">
        <el-card shadow="hover" v-loading="studentLoading">
          <el-table :data="students" stripe size="default" @row-click="showStudentDetail">
            <template #empty><el-empty description="暂无学员" /></template>
            <el-table-column prop="nickname" label="昵称" width="150" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click.stop="showStudentDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-dialog v-model="studentVisible" :title="`${selectedStudent?.nickname} 的详情`" width="700px">
          <el-tabs v-model="detailTab" v-if="selectedStudent">
            <el-tab-pane label="训练记录" name="training">
              <el-table :data="studentTraining" stripe size="small">
                <el-table-column prop="recordDate" label="日期" width="120" />
                <el-table-column prop="duration" label="时长(分)" width="100" />
                <el-table-column prop="calories" label="热量" width="100" />
                <el-table-column prop="notes" label="备注" />
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="身体数据" name="metrics">
              <el-table :data="studentMetrics" stripe size="small">
                <el-table-column prop="recordDate" label="日期" width="120" />
                <el-table-column prop="weight" label="体重(kg)" width="100" />
                <el-table-column prop="bmi" label="BMI" width="80" />
                <el-table-column prop="bodyFat" label="体脂(%)" width="90" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-dialog>
      </el-tab-pane>

      <!-- Tab 3: Training Plans -->
      <el-tab-pane label="训练计划" name="plans">
        <el-button type="primary" @click="showPlanDialog()" style="margin-bottom:16px">创建计划</el-button>
        <el-card shadow="hover" v-loading="planLoading">
          <el-table :data="plans" stripe size="default">
            <template #empty><el-empty description="暂无计划" /></template>
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column prop="userId" label="目标学员ID" width="110" />
            <el-table-column label="周期" width="200">
              <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
                  {{ row.status === 0 ? '进行中' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Course Dialog -->
    <el-dialog v-model="courseVisible" :title="editingCourse ? '编辑课程' : '创建课程'" width="520px">
      <el-form :model="courseForm" :rules="courseRules" ref="courseFormRef" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="courseForm.title" /></el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="courseForm.category" style="width:100%">
            <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="courseForm.difficulty" style="width:100%">
            <el-option v-for="d in difficulties" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(分)"><el-input-number v-model="courseForm.duration" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="courseForm.price" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="courseForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="courseForm.coverUrl" placeholder="选填" /></el-form-item>
        <el-form-item label="视频URL"><el-input v-model="courseForm.videoUrl" placeholder="选填" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCourse" :loading="savingCourse">保存</el-button>
      </template>
    </el-dialog>

    <!-- Plan Dialog -->
    <el-dialog v-model="planVisible" title="创建训练计划" width="500px">
      <el-form :model="planForm" :rules="planRules" ref="planFormRef" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="planForm.title" /></el-form-item>
        <el-form-item label="学员ID" prop="userId"><el-input-number v-model="planForm.userId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="planForm.content" type="textarea" :rows="5" placeholder="训练计划内容" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="planForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="planForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePlan" :loading="savingPlan">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('courses')

// --- Courses ---
const courses = ref([])
const courseLoading = ref(false)
const courseVisible = ref(false)
const editingCourse = ref(null)
const savingCourse = ref(false)
const courseFormRef = ref(null)
const courseForm = reactive({ title: '', category: 'STRENGTH', difficulty: 'BEGINNER', duration: 60, price: 0, description: '', coverUrl: '', videoUrl: '' })
const courseRules = { title: [{ required: true }], category: [{ required: true }], difficulty: [{ required: true }] }

const categories = [
  { label: '增肌', value: 'STRENGTH' }, { label: '减脂', value: 'FAT_LOSS' },
  { label: '瑜伽', value: 'YOGA' }, { label: '有氧', value: 'CARDIO' }, { label: '拉伸', value: 'STRETCHING' },
]
const difficulties = [
  { label: '初级', value: 'BEGINNER' }, { label: '中级', value: 'INTERMEDIATE' }, { label: '高级', value: 'ADVANCED' },
]
const catLabel = (c) => categories.find(x => x.value === c)?.label || c
const statusTag = (s) => ({ APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger', DRAFT: 'info' }[s] || 'info')

async function fetchCourses() {
  courseLoading.value = true
  try { const r = await request.get('/coach/courses'); if (r.code === 200) courses.value = r.data.records } catch (e) {}
  courseLoading.value = false
}

function showCourseDialog(row) {
  if (row) {
    editingCourse.value = row
    Object.assign(courseForm, row)
  } else {
    editingCourse.value = null
    Object.assign(courseForm, { title: '', category: 'STRENGTH', difficulty: 'BEGINNER', duration: 60, price: 0, description: '', coverUrl: '', videoUrl: '' })
  }
  courseVisible.value = true
}

async function handleSaveCourse() {
  const valid = await courseFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingCourse.value = true
  try {
    const data = { ...courseForm }
    let r
    if (editingCourse.value) {
      r = await request.put(`/coach/courses/${editingCourse.value.id}`, data)
    } else {
      r = await request.post('/coach/courses', data)
    }
    if (r.code === 200) { ElMessage.success(editingCourse.value ? '已更新' : '已创建'); courseVisible.value = false; fetchCourses() }
  } catch (e) { ElMessage.error('操作失败') }
  finally { savingCourse.value = false }
}

async function handleDeleteCourse(row) {
  try { await request.delete(`/coach/courses/${row.id}`); ElMessage.success('已删除'); fetchCourses() } catch (e) { ElMessage.error('删除失败') }
}

// --- Students ---
const students = ref([])
const studentLoading = ref(false)
const studentVisible = ref(false)
const selectedStudent = ref(null)
const detailTab = ref('training')
const studentTraining = ref([])
const studentMetrics = ref([])

async function fetchStudents() {
  studentLoading.value = true
  try { const r = await request.get('/coach/students'); if (r.code === 200) students.value = r.data.records } catch (e) {}
  studentLoading.value = false
}

async function showStudentDetail(row) {
  selectedStudent.value = row
  studentVisible.value = true
  try {
    const [tr, mr] = await Promise.all([
      request.get(`/coach/students/${row.id}/training`),
      request.get(`/coach/students/${row.id}/metrics`),
    ])
    if (tr.code === 200) studentTraining.value = tr.data.records || []
    if (mr.code === 200) studentMetrics.value = mr.data.records || []
  } catch (e) {}
}

// --- Plans ---
const plans = ref([])
const planLoading = ref(false)
const planVisible = ref(false)
const savingPlan = ref(false)
const planFormRef = ref(null)
const planForm = reactive({ title: '', userId: null, content: '', startDate: '', endDate: '' })
const planRules = { title: [{ required: true }], userId: [{ required: true }] }

async function fetchPlans() {
  planLoading.value = true
  try { const r = await request.get('/coach/plans'); if (r.code === 200) plans.value = r.data.records } catch (e) {}
  planLoading.value = false
}

function showPlanDialog() {
  Object.assign(planForm, { title: '', userId: null, content: '', startDate: '', endDate: '' })
  planVisible.value = true
}

async function handleSavePlan() {
  const valid = await planFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingPlan.value = true
  try {
    const r = await request.post('/coach/plans', { ...planForm })
    if (r.code === 200) { ElMessage.success('计划已创建'); planVisible.value = false; fetchPlans() }
  } catch (e) { ElMessage.error('创建失败') }
  finally { savingPlan.value = false }
}

onMounted(() => { fetchCourses(); fetchStudents(); fetchPlans() })
</script>

<style scoped>
.page-title { margin: 0 0 16px; font-size: 20px; }
</style>
