<template>
  <div class="coach-schedule">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" size="default">
        <el-form-item label="教练ID">
          <el-input-number v-model="coachId" :min="1" placeholder="输入教练ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadShifts">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="coachId" style="margin-top: 16px">
      <template #header><h3>教练 #{{ coachId }} 每周排班</h3></template>
      <el-table :data="weekData" stripe size="default">
        <el-table-column prop="label" label="星期" width="100" />
        <el-table-column label="开始时间" width="200">
          <template #default="{ row }">
            <el-time-picker
              v-model="row.startTime"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="选择时间"
              style="width: 160px"
            />
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="200">
          <template #default="{ row }">
            <el-time-picker
              v-model="row.endTime"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="选择时间"
              style="width: 160px"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag v-if="row.startTime && row.endTime" type="success" size="small">已排班</el-tag>
            <el-tag v-else type="info" size="small">休息</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; text-align: right">
        <el-button type="primary" @click="saveShifts" :loading="saving">保存排班</el-button>
      </div>
    </el-card>

    <el-card v-else style="margin-top: 16px">
      <el-empty description="请输入教练ID查询排班" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const coachId = ref(null)
const saving = ref(false)

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const weekData = reactive(weekLabels.map((label, i) => ({
  label,
  dayOfWeek: i + 1,
  startTime: '',
  endTime: '',
})))

async function loadShifts() {
  if (!coachId.value) return
  try {
    const res = await request.get('/shifts', { params: { coachId: coachId.value } })
    // Reset all
    weekData.forEach(w => { w.startTime = ''; w.endTime = '' })
    if (res.code === 200 && res.data) {
      res.data.forEach(shift => {
        const day = weekData[shift.dayOfWeek - 1]
        if (day) {
          day.startTime = shift.startTime ? shift.startTime.substring(0, 5) : ''
          day.endTime = shift.endTime ? shift.endTime.substring(0, 5) : ''
        }
      })
    }
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

async function saveShifts() {
  saving.value = true
  try {
    const shifts = weekData
      .filter(w => w.startTime && w.endTime)
      .map(w => ({
        coachId: coachId.value,
        dayOfWeek: w.dayOfWeek,
        startTime: w.startTime,
        endTime: w.endTime,
      }))
    const res = await request.post('/shifts', shifts)
    if (res.code === 200) {
      ElMessage.success('排班已保存')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>
