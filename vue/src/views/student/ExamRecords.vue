<template>
  <div class="main-container">
    <div class="page-header">
      <h2>考试记录</h2>
    </div>

    <el-card v-loading="loading">
      <el-table :data="records" stripe>
        <el-table-column prop="examName" label="考试名称" />
        <el-table-column prop="score" label="成绩" width="90">
          <template #default="{ row }">
            <span v-if="row.status === 'submitted'">{{ row.score ?? '-' }}</span>
            <el-tag v-else type="info" size="small">未交卷</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用时" width="100">
          <template #default="{ row }">
            {{ calcDuration(row.startTime, row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="交卷时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'submitted'" :type="row.passed === 1 ? 'success' : 'danger'">
              {{ row.passed === 1 ? '已通过' : '未通过' }}
            </el-tag>
            <el-tag v-else-if="row.status === 'in_progress'" type="warning">进行中</el-tag>
            <el-tag v-else type="info">未开始</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'submitted'"
              type="primary"
              link
              @click="viewDetail(row)"
            >
              查看答卷
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && records.length === 0" description="暂无考试记录" />
    </el-card>

    <el-dialog v-model="detailVisible" title="答卷详情" width="800px" top="5vh">
      <div v-if="detailLoading" v-loading="detailLoading" style="min-height:200px" />
      <div v-else-if="detail">
        <el-descriptions :column="2" border style="margin-bottom:20px">
          <el-descriptions-item label="成绩">{{ detail.record?.score ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detail.record?.passed === 1 ? 'success' : 'danger'">
              {{ detail.record?.passed === 1 ? '通过' : '未通过' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detail.record?.startTime }}</el-descriptions-item>
          <el-descriptions-item label="交卷时间">{{ detail.record?.submitTime }}</el-descriptions-item>
        </el-descriptions>
        <div v-for="(item, index) in detail.answers" :key="index" style="margin-bottom:16px;padding:12px;border:1px solid #ebeef5;border-radius:4px">
          <p><strong>第{{ index + 1 }}题</strong> ({{ questionTypeName(item.questionType) }})</p>
          <p>{{ item.questionTitle }}</p>
          <p v-if="item.options" style="color:#909399;white-space:pre-line">选项：{{ item.options }}</p>
          <p>你的答案：<span :style="{ color: item.answer?.isCorrect === 1 ? '#67c23a' : '#f56c6c' }">{{ item.answer?.answer || '未作答' }}</span></p>
          <p>正确答案：<span style="color:#67c23a">{{ item.correctAnswer }}</span></p>
          <p v-if="item.analysis" style="color:#409eff">解析：{{ item.analysis }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { studentApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'
import { questionTypeName } from '@/utils/constants'

const loading = ref(false)
const records = ref([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const calcDuration = (start, end) => {
  if (!start) return '-'
  const s = new Date(start)
  const e = end ? new Date(end) : new Date()
  const diff = Math.floor((e - s) / 1000 / 60)
  if (diff < 60) return diff + '分钟'
  const h = Math.floor(diff / 60)
  const m = diff % 60
  return h + '小时' + m + '分钟'
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await studentApi.getExamRecords()
    records.value = res.data || []
  } catch (e) {
    ElMessage.error('加载考试记录失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await studentApi.getRecordDetail(row.id)
    detail.value = res.data
  } catch (e) {
    ElMessage.error('加载答卷详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadRecords()
})

onBeforeUnmount(() => {
  detailVisible.value = false
  forceCleanDialogs()
})
</script>