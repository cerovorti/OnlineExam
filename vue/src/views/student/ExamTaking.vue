<template>
  <div class="exam-taking">
    <div class="exam-header">
      <h3>{{ examInfo.name }}</h3>
      <div class="timer">⏰ 剩余时间: {{ formattedTime }}</div>
    </div>
    <el-alert
      v-if="cutScreenWarning"
      :title="`防切屏警告：您已切屏 ${examStore.cutScreenCount} 次，超过 ${examStore.maxCutScreen} 次将自动交卷`"
      type="warning"
      :closable="false"
      show-icon
    />
    <div class="exam-body">
      <div class="question-nav">
        <div class="nav-title">答题卡</div>
        <div class="nav-grid">
          <div
            v-for="(q, index) in questions"
            :key="q.id"
            :class="['nav-item', {
              'answered': examStore.answers[q.id],
              'marked': examStore.markedQuestions.has(q.id),
              'current': currentIndex === index
            }]"
            @click="switchQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
        <el-button type="primary" style="margin-top:20px; width:100%" @click="submitPaper" :loading="isSubmitting" :disabled="isSubmitting">交卷</el-button>
      </div>

      <div class="question-area">
        <div class="question-content" v-if="currentQuestion">
          <div class="question-header">
            <span>第 {{ currentIndex + 1 }} 题 ({{ typeLabel(currentQuestion.type) }}，{{ currentQuestion.score }}分)</span>
            <el-button link @click="toggleMark(currentQuestion.id)">
              <el-icon><Star /></el-icon> {{ examStore.markedQuestions.has(currentQuestion.id) ? '取消标记' : '标记' }}
            </el-button>
          </div>
          <div class="question-title" v-html="currentQuestion.title"></div>

          <el-radio-group v-if="currentQuestion.type === 'single'" v-model="answer" class="options-group">
            <el-radio v-for="(text, key) in parseOptions(currentQuestion.options)" :key="key" :label="key">{{ text }}</el-radio>
          </el-radio-group>

          <el-checkbox-group v-else-if="currentQuestion.type === 'multiple'" v-model="multiAnswer" class="options-group">
            <el-checkbox v-for="(text, key) in parseOptions(currentQuestion.options)" :key="key" :label="key">{{ text }}</el-checkbox>
          </el-checkbox-group>

          <el-radio-group v-else-if="currentQuestion.type === 'judge'" v-model="answer" class="options-group">
            <el-radio label="1">正确</el-radio>
            <el-radio label="0">错误</el-radio>
          </el-radio-group>

          <el-input v-else-if="currentQuestion.type === 'fill' || currentQuestion.type === 'short_answer' || currentQuestion.type === 'programming'"
            type="textarea" v-model="answer" rows="6" placeholder="请输入答案" />

          <div class="action-buttons">
            <el-button @click="prevQuestion" :disabled="currentIndex === 0">上一题</el-button>
            <el-button type="primary" @click="nextQuestion">下一题</el-button>
          </div>
        </div>
        <div v-else class="loading-area">
          <template v-if="loadError">
            <el-result icon="error" title="加载失败" :sub-title="loadError">
              <template #extra>
                <el-button type="primary" @click="retryLoad">重试</el-button>
                <el-button @click="router.push('/student/exams')">返回列表</el-button>
              </template>
            </el-result>
          </template>
          <template v-else>
            <el-skeleton :rows="6" animated :throttle="300">
              <template #template>
                <div style="padding:20px">
                  <el-skeleton-item variant="h1" style="width:60%;margin-bottom:16px" />
                  <el-skeleton-item variant="text" style="width:80%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:90%;margin-bottom:8px" />
                  <el-skeleton-item variant="text" style="width:75%;margin-bottom:24px" />
                  <el-skeleton-item variant="rect" style="width:40%;height:36px;margin-bottom:12px" />
                  <el-skeleton-item variant="rect" style="width:40%;height:36px" />
                </div>
              </template>
            </el-skeleton>
            <p style="color:#909399;margin-top:12px">正在加载试题...</p>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useExamStore } from '../../stores/exam'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { studentApi } from '../../api'
import { questionTypeName } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const examStore = useExamStore()

const examId = route.params.id
const examInfo = ref({ name: '加载中...' })
const questions = ref([])
const loadError = ref('')
const currentIndex = ref(0)
const cutScreenWarning = ref(false)
const isSubmitting = ref(false)
const recordId = ref(null)
let timer = null
let autoSaveTimer = null

const remainingSeconds = ref(0)

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)

const answer = computed({
  get: () => {
    if (!currentQuestion.value) return ''
    const saved = examStore.answers[currentQuestion.value.id]
    if (currentQuestion.value.type === 'multiple') {
      return saved ? saved.split(',') : []
    }
    return saved || ''
  },
  set: (val) => {
    if (!currentQuestion.value) return
    const saveVal = Array.isArray(val) ? val.join(',') : val
    examStore.saveAnswer(currentQuestion.value.id, saveVal)
    doSaveAnswer(currentQuestion.value.id, saveVal)
  }
})

const multiAnswer = computed({
  get: () => {
    if (!currentQuestion.value) return []
    const saved = examStore.answers[currentQuestion.value.id]
    return saved ? saved.split(',').filter(Boolean) : []
  },
  set: (val) => {
    if (!currentQuestion.value) return
    const saveVal = Array.isArray(val) ? val.join(',') : ''
    examStore.saveAnswer(currentQuestion.value.id, saveVal)
    doSaveAnswer(currentQuestion.value.id, saveVal)
  }
})

const formattedTime = computed(() => {
  const h = Math.floor(remainingSeconds.value / 3600)
  const m = Math.floor((remainingSeconds.value % 3600) / 60)
  const s = remainingSeconds.value % 60
  return `${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}`
})

const typeLabel = questionTypeName

const parseOptions = (options) => {
  if (!options) return {}
  try {
    if (typeof options === 'string') {
      return JSON.parse(options)
    }
    return options
  } catch {
    return {}
  }
}

const switchQuestion = (index) => {
  currentIndex.value = index
}

const prevQuestion = () => { if (currentIndex.value > 0) currentIndex.value-- }
const nextQuestion = () => { if (currentIndex.value < questions.value.length - 1) currentIndex.value++ }
const toggleMark = (id) => examStore.toggleMark(id)

const doSaveAnswer = async (questionId, answerVal) => {
  if (!recordId.value || isSubmitting.value) return
  try {
    await studentApi.saveAnswer(examId, {
      questionId: questionId,
      answer: answerVal
    })
  } catch (e) {
    console.error('保存答案失败', e)
  }
}

const handleVisibilityChange = () => {
  if (!examInfo.value?.antiCheatEnabled) return
  if (document.hidden) {
    examStore.incrementCutScreen()
    cutScreenWarning.value = true
    if (examStore.cutScreenCount >= examStore.maxCutScreen) {
      ElMessage.error('切屏次数超过限制，系统自动交卷')
      submitPaper('cutscreen')
    }
  }
}

const loadExam = async () => {
  try {
    loadError.value = ''
    examStore.resetAll()

    const detailRes = await studentApi.getExamDetail(examId)
    if (detailRes.code !== 200 || !detailRes.data) {
      loadError.value = detailRes.message || '获取考试信息失败'
      return
    }
    const { exam: examDetail, record } = detailRes.data

    const now = Date.now()
    const startTime = examDetail?.startTime ? new Date(examDetail.startTime).getTime() : null
    const endTime = examDetail?.endTime ? new Date(examDetail.endTime).getTime() : null

    if (startTime && now < startTime) {
      loadError.value = '考试尚未开始，开始时间：' + examDetail.startTime
      return
    }

    if (endTime && now > endTime) {
      const isInProgress = record && record.status === 'in_progress'
      if (!isInProgress) {
        loadError.value = '考试已结束'
        return
      }
    }

    console.log('[ExamTaking] loadExam start, examId=', examId)
    const res = await studentApi.startExam(examId)
    console.log('[ExamTaking] startExam response:', JSON.stringify({ code: res.code, hasData: !!res.data }))
    if (res.code === 200 && res.data) {
      const { exam, questions: qs, record: rec, duration, remainingSeconds: remSec, answers: existingAnswers } = res.data
      console.log('[ExamTaking] parsed: exam=', !!exam, 'questions=', qs?.length, 'record=', !!rec, 'duration=', duration, 'remSec=', remSec)
      examInfo.value = exam || { name: '考试' }
      if (Array.isArray(qs) && qs.length > 0) {
        questions.value = qs
      } else {
        console.warn('[ExamTaking] questions is empty or not array, qs=', qs)
        loadError.value = '试卷未包含题目，请确认组卷是否正确'
        return
      }
      recordId.value = rec?.id
      remainingSeconds.value = (remSec > 0 ? remSec : (duration || 120) * 60)
      if (exam?.maxCutScreen) examStore.maxCutScreen = exam.maxCutScreen
      if (existingAnswers) {
        examStore.loadAnswers(existingAnswers)
      }
    } else {
      console.warn('[ExamTaking] startExam failed:', res)
      loadError.value = res.message || '加载考试失败'
    }
  } catch (e) {
    console.error('[ExamTaking] loadExam error:', e?.message || e, e?.response?.status)
    loadError.value = e?.data?.message || e?.message || '网络请求失败，请检查后端是否启动'
  }
}

const retryLoad = () => {
  loadError.value = ''
  loadExam()
}

onMounted(async () => {
  await loadExam()
  document.addEventListener('visibilitychange', handleVisibilityChange)
  const examEndTime = examInfo.value?.endTime ? new Date(examInfo.value.endTime).getTime() : null
  timer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      submitPaper('timeout')
      return
    }
    if (examEndTime && Date.now() >= examEndTime) {
      submitPaper('timeout')
    }
  }, 1000)
  autoSaveTimer = setInterval(() => {
    const q = currentQuestion.value
    if (q && examStore.answers[q.id]) {
      doSaveAnswer(q.id, examStore.answers[q.id])
    }
  }, 30000)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  if (timer) clearInterval(timer)
  if (autoSaveTimer) clearInterval(autoSaveTimer)
})

const submitPaper = async (type = 'manual') => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    if (type === 'manual') {
      await ElMessageBox.confirm('确定要交卷吗？', '提示', { type: 'warning' })
    }

    const res = await studentApi.submitExam(examId, {
      type: type,
      cutScreenCount: examStore.cutScreenCount
    })
    console.log('[ExamTaking] submit result:', res)

    if (timer) { clearInterval(timer); timer = null }
    if (autoSaveTimer) { clearInterval(autoSaveTimer); autoSaveTimer = null }
    document.removeEventListener('visibilitychange', handleVisibilityChange)

    router.push({
      path: '/student/submitted',
      query: {
        examId: examId,
        type,
        cutScreenCount: examStore.cutScreenCount,
        maxCutScreen: examStore.maxCutScreen
      }
    })
    if (type === 'manual') ElMessage.success('交卷成功')
  } catch (e) {
    console.error('[ExamTaking] submit error:', e?.message || e, e?.response?.status)
    if (e !== 'cancel') {
      const respData = e?.response?.data
      const errMsg = respData?.message || respData?.error || e?.data?.message || e?.message || '请重试'
      ElMessage.error('交卷失败：' + errMsg)
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.exam-taking { height: 100%; display: flex; flex-direction: column; background: #f5f7fa; }
.exam-header { padding: 18px 24px; border-bottom: 1px solid #ebeef5; display: flex; justify-content: space-between; align-items: center; background: #fff; }
.timer { font-size: 20px; color: #f56c6c; font-weight: 600; }
.exam-body { flex:1; display: flex; overflow: hidden; gap: 24px; padding: 24px; }
.question-nav { width: 220px; padding: 20px; border: 1px solid #ebeef5; border-radius: 12px; background: #fff; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04); flex-shrink: 0; }
.nav-title { margin-bottom: 15px; font-weight: bold; font-size: 16px; }
.nav-grid { display: grid; grid-template-columns: repeat(5,1fr); gap: 8px; }
.nav-item { width: 32px; height: 32px; line-height: 32px; text-align: center; border:1px solid #dcdfe6; border-radius: 6px; cursor: pointer; transition: all 0.2s ease; }
.nav-item.answered { background: #ecf5ff; border-color: #409EFF; }
.nav-item.marked { background: #fdf6ec; border-color: #e6a23c; }
.nav-item.current { border: 2px solid #409EFF; }
.question-area { flex:1; overflow-y: auto; text-align: left; }
.question-content { max-width: 900px; min-height: 100%; padding: 32px 36px; margin: 0 auto; background: #fff; border-radius: 16px; box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05); box-sizing: border-box; }
.question-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; padding-bottom: 18px; border-bottom: 1px solid #ebeef5; color: #606266; }
.question-title { font-size: 22px; font-weight: 600; margin-bottom: 28px; text-align: left; line-height: 1.8; color: #303133; }
.options-group { display: flex; flex-direction: column; gap: 16px; align-items: flex-start; }
.options-group :deep(.el-radio), .options-group :deep(.el-checkbox) { margin-right: 0; white-space: normal; text-align: left; }
.options-group :deep(.el-radio__label), .options-group :deep(.el-checkbox__label) { line-height: 1.7; font-size: 15px; color: #303133; }
.action-buttons { margin-top: 48px; display: flex; gap: 20px; justify-content: flex-end; }
.loading-area { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; color: #909399; }
</style>