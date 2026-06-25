<template>
  <div class="course-review">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批" name="PENDING" />
        <el-tab-pane label="已通过" name="APPROVED" />
        <el-tab-pane label="已拒绝" name="REJECTED" />
      </el-tabs>

      <el-table :data="courses" stripe v-loading="loading" size="default">
        <el-table-column label="课程" min-width="240">
          <template #default="{ row }">
            <div class="course-cell">
              <el-image
                :src="row.coverUrl"
                fit="cover"
                class="cover"
                v-if="row.coverUrl"
              >
                <template #error>
                  <div class="cover-placeholder">📹</div>
                </template>
              </el-image>
              <div v-else class="cover-placeholder">📹</div>
              <div class="course-info">
                <span class="course-title">{{ row.title }}</span>
                <span class="course-coach">教练ID: {{ row.coachId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-tag
              :type="diffTag(row.difficulty)"
              size="small"
            >
              {{ diffLabel(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="90">
          <template #default="{ row }">{{ row.duration }} 分钟</template>
        </el-table-column>
        <el-table-column label="价格" width="90">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" v-if="activeTab === 'PENDING'">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="showRejectDialog(row)">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchCourses"
          @size-change="fetchCourses"
        />
      </div>
    </el-card>

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectVisible" title="拒绝课程" width="450px">
      <el-form :model="rejectForm">
        <el-form-item label="课程">
          <span>{{ rejectTarget?.title }}</span>
        </el-form-item>
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝原因（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject" :loading="rejecting">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const courses = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeTab = ref('PENDING')

const rejectVisible = ref(false)
const rejecting = ref(false)
const rejectTarget = ref(null)
const rejectForm = reactive({ reason: '' })

const diffTag = (d) => ({ BEGINNER: 'success', INTERMEDIATE: 'warning', ADVANCED: 'danger' }[d] || 'info')
const diffLabel = (d) => ({ BEGINNER: '初级', INTERMEDIATE: '中级', ADVANCED: '高级' }[d] || d)
const statusTag = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info')
const statusLabel = (s) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s)

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ')
}

onMounted(() => fetchCourses())

async function fetchCourses() {
  loading.value = true
  try {
    const status = activeTab.value
    const params = { page: page.value, size: size.value }
    if (status) params.status = status
    const res = await request.get('/courses', { params })
    if (res.code === 200) {
      courses.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  fetchCourses()
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确认通过课程「${row.title}」？`, '审批确认', {
      type: 'success',
    })
    await request.put(`/courses/${row.id}/approve`)
    ElMessage.success(`「${row.title}」已通过`)
    fetchCourses()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function showRejectDialog(row) {
  rejectTarget.value = row
  rejectForm.reason = ''
  rejectVisible.value = true
}

async function handleReject() {
  rejecting.value = true
  try {
    await request.put(`/courses/${rejectTarget.value.id}/reject`, {
      reason: rejectForm.reason,
    })
    ElMessage.success(`「${rejectTarget.value.title}」已拒绝`)
    rejectVisible.value = false
    fetchCourses()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    rejecting.value = false
  }
}
</script>

<style scoped>
.course-cell { display: flex; align-items: center; gap: 12px; }
.cover { width: 60px; height: 40px; border-radius: 6px; }
.cover-placeholder {
  width: 60px; height: 40px; border-radius: 6px;
  background: #f0f2f5; display: flex; align-items: center; justify-content: center;
  font-size: 20px;
}
.course-info { display: flex; flex-direction: column; }
.course-title { font-size: 14px; font-weight: 500; }
.course-coach { font-size: 12px; color: #999; }

.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
