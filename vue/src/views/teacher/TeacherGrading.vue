<template>
  <div class="main-container">
    <div class="page-header">
      <h2>主观题阅卷</h2>
    </div>

    <el-card>
      <el-form :inline="true" style="margin-bottom:20px">
        <el-form-item label="选择考试">
          <el-select v-model="selectedExamId" placeholder="请选择考试" style="width:300px" @change="loadAnswers">
            <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-empty v-if="!selectedExamId" description="请选择考试" />

      <div v-if="selectedExamId" v-loading="loading">
        <el-table :data="answers" stripe>
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="100" />
          <el-table-column label="题目" show-overflow-tooltip>
            <template #default="{ row }">
              <div style="max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ row.questionTitle }}</div>
            </template>
          </el-table-column>
          <el-table-column label="学生答案" show-overflow-tooltip>
            <template #default="{ row }">
              <span style="font-size:13px;color:#303133">{{ row.studentAnswer || '(未作答)' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="referenceAnswer" label="参考答案" show-overflow-tooltip width="200" />
          <el-table-column label="评分" width="200">
            <template #default="{ row }">
              <div v-if="row.isCorrect === 1" style="color:#67c23a">已评 {{ row.score }}/{{ row.maxScore }}分</div>
              <div v-else style="display:flex;align-items:center;gap:8px">
                <el-input-number v-model="row._score" :min="0" :max="row.maxScore" size="small" style="width:100px" />
                <el-button type="primary" size="small" @click="handleGrade(row)">提交</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'

const selectedExamId = ref(null)
const exams = ref([])
const answers = ref([])
const loading = ref(false)

const loadExams = async () => {
  try {
    const res = await teacherApi.getExams({ page: 1, pageSize: 999 })
    const data = res.data
    exams.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) {
    console.error(e)
  }
}

const loadAnswers = async () => {
  if (!selectedExamId.value) return
  loading.value = true
  try {
    const res = await teacherApi.getSubjectiveAnswers(selectedExamId.value)
    const list = res.data || []
    answers.value = list.map(a => ({
      ...a,
      _score: a.score !== null && a.score !== undefined ? a.score : 0
    }))
  } catch (e) {
    ElMessage.error('加载答题失败')
  } finally {
    loading.value = false
  }
}

const handleGrade = async (row) => {
  try {
    await teacherApi.gradeSubjectiveAnswer(row.id, {
      isCorrect: row._score >= row.maxScore * 0.5 ? 1 : 0,
      score: row._score
    })
    row.isCorrect = row._score >= row.maxScore * 0.5 ? 1 : 0
    row.score = row._score
    ElMessage.success('评分成功')
  } catch (e) {
    ElMessage.error('评分失败')
  }
}

loadExams()
</script>
