<template>
  <div class="main-container">
    <div class="page-header">
      <h2>我的考试</h2>
    </div>
    <el-row :gutter="20">
      <el-col :span="8" v-for="exam in exams" :key="exam.id">
        <el-card class="exam-card">
          <template #header>
            <div style="display:flex; justify-content:space-between">
              <span>{{ exam.name }}</span>
              <el-tag v-if="exam.recordStatus === 'in_progress'" type="warning">未交卷</el-tag>
              <el-tag v-else-if="isActive(exam)" type="danger">进行中</el-tag>
              <el-tag v-else-if="isEnded(exam)" type="info">已结束</el-tag>
              <el-tag v-else type="info">未开始</el-tag>
            </div>
          </template>
          <div>时长: {{ exam.duration }}分钟</div>
          <div>开始: {{ formatTime(exam.startTime) }}</div>
          <div>结束: {{ formatTime(exam.endTime) }}</div>
          <div style="margin-top:15px; text-align:right">
            <el-button
              v-if="exam.recordStatus === 'in_progress'"
              type="warning"
              @click="enterExam(exam)"
            >
              继续考试
            </el-button>
            <el-button
              v-else
              type="primary"
              @click="enterExam(exam)"
              :disabled="!isActive(exam)"
            >
              进入考试
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && exams.length===0" description="暂无考试" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studentApi } from '../../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const exams = ref([])
const loading = ref(true)

const loadExams = async () => {
  loading.value = true
  try {
    const res = await studentApi.getExams()
    if (res.code === 200 && res.data) {
      exams.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载考试列表失败')
  } finally {
    loading.value = false
  }
}

const enterExam = (exam) => {
  router.push(`/student/exam/${exam.id}`)
}

const isActive = (exam) => {
  if (!exam.startTime || !exam.endTime) return false
  const now = new Date()
  const start = new Date(exam.startTime)
  const end = new Date(exam.endTime)
  return now >= start && now <= end
}

const isEnded = (exam) => {
  if (!exam.endTime) return false
  return new Date() > new Date(exam.endTime)
}

const formatTime = (time) => {
  if (!time) return '--'
  return time
}

onMounted(loadExams)
</script>

<style scoped>
.exam-card {
  margin-bottom: 20px;
}
</style>
