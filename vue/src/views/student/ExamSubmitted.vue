<template>
  <div class="main-container">
    <div class="page-header">
      <h2>已交卷</h2>
    </div>

    <el-card>
      <el-result
        :icon="resultIcon"
        title="已交卷"
        :sub-title="submitReason"
      >
        <template #extra>
          <el-button type="primary" @click="goExams">返回我的考试</el-button>
          <el-button @click="goRecords">查看考试记录</el-button>
        </template>
      </el-result>

      <el-divider />

      <div class="summary-wrap">
        <div class="summary-square">
          <div class="summary-item">
            <span class="label">试卷ID</span>
            <span class="value">{{ examId }}</span>
          </div>
          <div class="summary-item">
            <span class="label">切屏次数</span>
            <span class="value">{{ cutScreenCount }}</span>
          </div>
          <div class="summary-item">
            <span class="label">切屏上限</span>
            <span class="value">{{ maxCutScreen }}</span>
          </div>
          <div class="summary-item">
            <span class="label">提交时间</span>
            <span class="value value-time">{{ submittedAt }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const examId = computed(() => route.query.examId || '-')
const type = computed(() => {
  const t = route.query.type
  if (t === 'timeout' || t === 'cutscreen' || t === 'manual') return t
  return 'manual'
})
const cutScreenCount = computed(() => Number(route.query.cutScreenCount || 0))
const maxCutScreen = computed(() => Number(route.query.maxCutScreen || 0))
const submittedAt = computed(() => route.query.at || new Date().toLocaleString())

const resultIcon = computed(() => (type.value === 'manual' ? 'success' : 'warning'))
const submitReason = computed(() => {
  if (type.value === 'cutscreen') return '原因：切屏次数超限，系统自动交卷'
  if (type.value === 'timeout') return '原因：考试时间结束，系统自动交卷'
  return '原因：考生确认后自主交卷'
})

const goExams = () => router.push('/student/exams')
const goRecords = () => router.push('/student/records')
</script>

<style scoped>
.summary-wrap {
  display: flex;
  justify-content: center;
}

.summary-square {
  width: 340px;
  min-height: 250px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafafa;
  padding: 16px 18px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 12px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  padding: 6px 0;
  border-bottom: 1px dashed #ebeef5;
}

.summary-item:last-child {
  border-bottom: none;
}

.label {
  color: #909399;
}

.value {
  color: #303133;
  font-weight: 500;
  text-align: right;
}

.value-time {
  max-width: 165px;
  word-break: break-all;
}
</style>

