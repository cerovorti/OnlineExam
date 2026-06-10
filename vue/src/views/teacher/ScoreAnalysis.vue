<template>
  <div class="main-container">
    <div class="page-header">
      <h2>成绩分析</h2>
      <el-button v-if="selectedExamId" @click="handleExport">导出Excel</el-button>
    </div>

    <el-card>
      <el-form :inline="true" style="margin-bottom:20px">
        <el-form-item label="选择考试">
          <el-select v-model="selectedExamId" placeholder="请选择考试" style="width:300px" @change="loadAnalysis">
            <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <div v-if="!selectedExamId" style="text-align:center; padding:40px">
        <el-empty description="请选择考试查看成绩分析" />
      </div>

      <div v-if="analysis && selectedExamId" v-loading="analysisLoading">
        <el-row :gutter="20" style="margin-bottom:24px">
          <el-col :span="4">
            <el-statistic title="应考人数" :value="analysis.totalCount || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="已交卷" :value="analysis.submittedCount || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="平均分" :value="analysis.avgScore || 0" :precision="1" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="最高分" :value="analysis.maxScore || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="最低分" :value="analysis.minScore || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="及格率" :value="analysis.passRate || 0" suffix="%" :precision="1" />
          </el-col>
        </el-row>

        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="成绩详情" name="scores">
            <el-table :data="analysis.records" stripe>
              <el-table-column prop="studentNo" label="学号" width="120" />
              <el-table-column prop="studentName" label="姓名" width="120" />
              <el-table-column prop="score" label="得分" width="100">
                <template #default="{ row }">
                  <el-tag :type="(row.score || 0) >= (analysis.passScore || 60) ? 'success' : 'danger'">
                    {{ row.score }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="是否及格" width="100">
                <template #default="{ row }">{{ (row.score || 0) >= (analysis.passScore || 60) ? '是' : '否' }}</template>
              </el-table-column>
              <el-table-column label="交卷时间" width="180">
                <template #default="{ row }">{{ row.submitTime }}</template>
              </el-table-column>
              <el-table-column label="交卷方式" width="100">
                <template #default="{ row }">{{ row.submitType || '-' }}</template>
              </el-table-column>
              <el-table-column label="切屏次数" width="90">
                <template #default="{ row }">{{ row.cutScreenCount || 0 }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="主观题阅卷" name="subjective">
            <div v-loading="subjectiveLoading">
              <el-alert
                v-if="subjectiveAnswers.length === 0 && !subjectiveLoading"
                title="该考试无主观题或所有主观题已评分"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom:16px"
              />
              <el-table :data="subjectiveAnswers" stripe v-if="subjectiveAnswers.length">
                <el-table-column prop="studentNo" label="学号" width="100" />
                <el-table-column prop="studentName" label="姓名" width="100" />
                <el-table-column label="题型" width="80">
                  <template #default="{ row }">{{ typeName(row.questionType) }}</template>
                </el-table-column>
                <el-table-column prop="questionTitle" label="题目" show-overflow-tooltip />
                <el-table-column label="学生答案" width="200">
                  <template #default="{ row }">
                    <el-popover placement="left" width="400" trigger="click">
                      <template #reference>
                        <el-button link type="primary">查看答案</el-button>
                      </template>
                      <pre style="white-space:pre-wrap;word-break:break-word">{{ row.studentAnswer || '（未作答）' }}</pre>
                    </el-popover>
                  </template>
                </el-table-column>
                <el-table-column label="参考答案" width="200">
                  <template #default="{ row }">
                    <el-popover placement="left" width="400" trigger="click">
                      <template #reference>
                        <el-button link type="success">查看参考</el-button>
                      </template>
                      <pre style="white-space:pre-wrap;word-break:break-word">{{ row.referenceAnswer || '（无）' }}</pre>
                    </el-popover>
                  </template>
                </el-table-column>
                <el-table-column label="得分/满分" width="90">
                  <template #default="{ row }">{{ row.score != null ? row.score : '-' }}/{{ row.maxScore }}</template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.gradeScore"
                      :min="0"
                      :max="row.maxScore"
                      :step="1"
                      size="small"
                      style="width:80px"
                    />
                    <el-button
                      type="primary"
                      size="small"
                      style="margin-left:8px"
                      @click="handleGrade(row)"
                    >评分</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="试题质量" name="quality">
            <div v-loading="qualityLoading">
              <el-alert
                v-if="qualityData.length === 0 && !qualityLoading"
                title="暂无试题质量数据"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom:16px"
              />
              <el-table :data="qualityData" stripe v-if="qualityData.length">
                <el-table-column prop="questionId" label="题目ID" width="80" />
                <el-table-column label="题型" width="90">
                  <template #default="{ row }">{{ typeName(row.questionType) }}</template>
                </el-table-column>
                <el-table-column prop="questionTitle" label="题目" show-overflow-tooltip />
                <el-table-column label="正确率" width="100">
                  <template #default="{ row }">
                    <el-progress :percentage="row.accuracyRate" :color="row.accuracyRate >= 60 ? '#67c23a' : '#f56c6c'" />
                  </template>
                </el-table-column>
                <el-table-column label="正确人数" width="90">
                  <template #default="{ row }">{{ row.correctCount }}/{{ row.totalCount }}</template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'
import { questionTypeName as typeName } from '@/utils/constants'

const exams = ref([])
const selectedExamId = ref(null)
const analysis = ref(null)
const analysisLoading = ref(false)

const activeTab = ref('scores')

const subjectiveAnswers = ref([])
const subjectiveLoading = ref(false)

const qualityData = ref([])
const qualityLoading = ref(false)

const loadExams = async () => {
  try {
    const res = await teacherApi.getExams({ pageSize: 100 })
    const data = res.data
    exams.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { ElMessage.error('加载考试列表失败') }
}

const loadAnalysis = async () => {
  if (!selectedExamId.value) return
  analysisLoading.value = true
  try {
    const res = await teacherApi.getExamAnalysis(selectedExamId.value)
    analysis.value = res.data
    activeTab.value = 'scores'
  } catch (e) { ElMessage.error('加载成绩分析失败') } finally { analysisLoading.value = false }
}

const handleTabChange = (tab) => {
  if (tab === 'subjective') loadSubjectiveAnswers()
  if (tab === 'quality') loadQuestionQuality()
}

const loadSubjectiveAnswers = async () => {
  subjectiveLoading.value = true
  try {
    const res = await teacherApi.getSubjectiveAnswers(selectedExamId.value)
    const list = (res.data || []).map(item => ({
      ...item,
      gradeScore: item.score != null ? item.score : 0
    }))
    subjectiveAnswers.value = list
  } catch (e) { ElMessage.error('加载主观题答案失败') } finally { subjectiveLoading.value = false }
}

const handleGrade = async (row) => {
  try {
    await teacherApi.gradeSubjectiveAnswer(row.id, {
      isCorrect: row.gradeScore >= row.maxScore / 2 ? 1 : 0,
      score: row.gradeScore
    })
    row.score = row.gradeScore
    row.isCorrect = row.gradeScore >= row.maxScore / 2 ? 1 : 0
    ElMessage.success('评分成功')
  } catch (e) { ElMessage.error('评分失败') }
}

const loadQuestionQuality = async () => {
  qualityLoading.value = true
  try {
    const res = await teacherApi.getQuestionQuality(selectedExamId.value)
    const list = (res.data || []).map(item => ({
      ...item,
      accuracyRate: item.totalCount > 0 ? Math.round((item.correctCount / item.totalCount) * 100) : 0
    }))
    qualityData.value = list
  } catch (e) { ElMessage.error('加载试题质量失败') } finally { qualityLoading.value = false }
}

const handleExport = () => {
  teacherApi.exportExamScores(selectedExamId.value).then(res => {
    const url = URL.createObjectURL(res)
    const a = document.createElement('a')
    a.href = url
    a.download = '成绩导出.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  }).catch(() => ElMessage.error('导出失败'))
}

onMounted(() => { loadExams() })
</script>