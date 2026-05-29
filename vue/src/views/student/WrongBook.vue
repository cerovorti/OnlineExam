<template>
  <div class="main-container">
    <div class="page-header">
      <h2>错题本</h2>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px" v-if="analysisData">
      <el-col :span="12">
        <el-card header="各科成绩趋势" v-loading="analysisLoading">
          <div ref="trendChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="知识点掌握" v-loading="analysisLoading">
          <div ref="radarChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card header="我的错题">
      <el-row style="margin-bottom:12px">
        <el-select v-model="subjectFilter" placeholder="按科目筛选" clearable @change="loadWrongQuestions" style="width:180px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-row>
      <el-table :data="wrongQuestions" v-loading="wrongLoading">
        <el-table-column prop="subjectName" label="科目" width="120" />
        <el-table-column label="题目类型" width="90">
          <template #default="{ row }">{{ questionTypeName(row.questionType) }}</template>
        </el-table-column>
        <el-table-column prop="questionTitle" label="题目" show-overflow-tooltip />
        <el-table-column prop="myAnswer" label="我的答案" width="120" show-overflow-tooltip />
        <el-table-column prop="correctAnswer" label="正确答案" width="120" show-overflow-tooltip />
        <el-table-column prop="wrongCount" label="错误次数" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.mastered === 1 ? 'success' : 'warning'" size="small">
              {{ row.mastered === 1 ? '已掌握' : '未掌握' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.mastered !== 1"
              type="success"
              link
              @click="markMastered(row)"
            >标记掌握</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!wrongLoading && wrongQuestions.length === 0" description="暂无错题" />
    </el-card>

    <el-dialog v-model="detailVisible" title="错题详情" width="600px">
      <div v-if="detailQuestion">
        <p><strong>题目类型：</strong>{{ questionTypeName(detailQuestion.questionType) }}</p>
        <p><strong>题目：</strong>{{ detailQuestion.questionTitle }}</p>
        <p v-if="detailQuestion.options" style="white-space:pre-line;color:#909399">选项：{{ detailQuestion.options }}</p>
        <p><strong>你的答案：</strong><span style="color:#f56c6c">{{ detailQuestion.myAnswer || '未作答' }}</span></p>
        <p><strong>正确答案：</strong><span style="color:#67c23a">{{ detailQuestion.correctAnswer }}</span></p>
        <p v-if="detailQuestion.analysis"><strong>解析：</strong><span style="color:#409eff">{{ detailQuestion.analysis }}</span></p>
        <p><strong>错误次数：</strong>{{ detailQuestion.wrongCount }}</p>
        <p><strong>知识点：</strong>{{ detailQuestion.knowledgePoints }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { studentApi, subjectApi } from '@/api'
import { questionTypeName } from '@/utils/constants'
import { forceCleanDialogs } from '@/utils/dialog'

const echarts = window.echarts

const wrongQuestions = ref([])
const wrongLoading = ref(false)
const subjectFilter = ref(null)
const subjects = ref([])
const detailVisible = ref(false)
const detailQuestion = ref(null)

const analysisData = ref(null)
const analysisLoading = ref(false)
const trendChartRef = ref(null)
const radarChartRef = ref(null)
let trendChart = null
let radarChart = null

const loadWrongQuestions = async () => {
  wrongLoading.value = true
  try {
    const params = subjectFilter.value ? { subjectId: subjectFilter.value } : {}
    const res = await studentApi.getWrongQuestions(params)
    wrongQuestions.value = res.data || []
  } catch (e) {
    ElMessage.error('加载错题失败')
  } finally {
    wrongLoading.value = false
  }
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.getSubjects()
    const data = res.data
    subjects.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) {
    // subjects are optional
  }
}

const viewDetail = (row) => {
  detailQuestion.value = row
  detailVisible.value = true
}

const markMastered = async (row) => {
  try {
    await studentApi.markAsMastered(row.id)
    row.mastered = 1
    ElMessage.success('已标记为掌握')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const loadAnalysis = async () => {
  analysisLoading.value = true
  try {
    const res = await studentApi.getScoreAnalysis()
    if (res.code === 200 && res.data) {
      analysisData.value = res.data
      await nextTick()
      renderTrendChart()
      renderRadarChart()
    }
  } catch (e) {
    analysisData.value = null
  } finally { analysisLoading.value = false }
}

const renderTrendChart = () => {
  if (!trendChartRef.value || !analysisData.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  const subjects = analysisData.value.trendSubjects || []
  const scores = analysisData.value.trendScores || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: subjects },
    yAxis: { type: 'value', name: '成绩' },
    series: [{
      data: scores,
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(64,158,255,0.2)' },
      itemStyle: { color: '#409eff' }
    }]
  })
}

const renderRadarChart = () => {
  if (!radarChartRef.value || !analysisData.value) return
  if (radarChart) radarChart.dispose()
  radarChart = echarts.init(radarChartRef.value)
  const indicators = (analysisData.value.radarIndicators || []).map(k => ({ name: k, max: 100 }))
  const values = analysisData.value.radarValues || []
  radarChart.setOption({
    radar: {
      indicator: indicators,
      shape: 'polygon',
      center: ['50%', '55%'],
      radius: '70%'
    },
    series: [{
      type: 'radar',
      data: [{ value: values, name: '掌握程度', areaStyle: { color: 'rgba(64,158,255,0.3)' } }]
    }]
  })
}

onMounted(() => {
  loadWrongQuestions()
  loadSubjects()
  loadAnalysis()
})

onBeforeUnmount(() => {
  detailVisible.value = false
  forceCleanDialogs()
  if (trendChart) { trendChart.dispose(); trendChart = null }
  if (radarChart) { radarChart.dispose(); radarChart = null }
})
</script>