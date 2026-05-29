<template>
  <div class="main-container">
    <div class="page-header">
      <h2>仪表盘</h2>
    </div>

    <div class="stat-cards" v-loading="loading">
      <el-card shadow="hover">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalExams || 0 }}</div>
          <div class="stat-label">考试场次</div>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalParticipations || 0 }}</div>
          <div class="stat-label">参与人次</div>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="stat-item">
          <div class="stat-value">{{ stats.activeExams || 0 }}</div>
          <div class="stat-label">进行中考试</div>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalStudents || 0 }}</div>
          <div class="stat-label">学生数</div>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalTeachers || 0 }}</div>
          <div class="stat-label">教师数</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'

const loading = ref(false)
const stats = ref({})

const loadStats = async () => {
  loading.value = true
  try {
    const res = await adminApi.getDashboardStats()
    stats.value = res.data || {}
  } catch (e) {
    console.error('加载统计数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.stat-item {
  text-align: center;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  color: #909399;
  margin-top: 8px;
}
</style>