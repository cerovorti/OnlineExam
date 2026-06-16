<template>
  <div class="main-container">
    <div class="page-header">
      <h2>试卷预览</h2>
    </div>

    <el-card>
      <el-form :inline="true" style="margin-bottom:20px">
        <el-form-item label="选择试卷">
          <el-select v-model="selectedPaperId" placeholder="请选择试卷" style="width:300px" @change="loadPreview">
            <el-option v-for="p in papers" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <div v-if="previewLoading" v-loading="previewLoading" style="min-height:200px" />
      <el-empty v-else-if="!paper && !selectedPaperId" description="请选择一份试卷进行预览" />

      <div v-if="paper" class="paper-preview">
        <div class="paper-header">
          <h2>{{ paper.name }}</h2>
          <div class="paper-meta">
            <el-tag>总分：{{ paper.totalScore }} 分</el-tag>
            <el-tag type="warning">时长：{{ paper.duration }} 分钟</el-tag>
            <el-tag type="danger">及格分：{{ paper.passScore }} 分</el-tag>
            <el-tag type="info">共 {{ questions.length }} 题</el-tag>
          </div>
        </div>

        <div v-for="(q, idx) in questions" :key="q.id" class="question-block">
          <div class="question-header">
            <span class="question-index">{{ idx + 1 }}.</span>
            <el-tag size="small" :type="qTypeTag(q.type)">{{ typeName(q.type) }}</el-tag>
            <el-tag size="small" :type="diffTag(q.difficulty)">{{ diffName(q.difficulty) }}</el-tag>
            <span class="question-score">({{ q.score || 1 }} 分)</span>
          </div>
          <div class="question-title">{{ q.title }}</div>
          <div v-if="q.options" class="question-options">
            <div v-for="(opt, oi) in parseOptions(q.options)" :key="oi" class="option-item">{{ opt }}</div>
          </div>
          <div class="question-answer">
            <el-text type="success">答案：{{ q.answer }}</el-text>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'
import { questionTypeName as typeName, questionTagType as qTypeTag, difficultyName as diffName, difficultyTagType as diffTag } from '@/utils/constants'

const route = useRoute()

const papers = ref([])
const selectedPaperId = ref(null)
const paper = ref(null)
const questions = ref([])
const previewLoading = ref(false)

const parseOptions = (opts) => {
  if (!opts) return []
  if (Array.isArray(opts)) return opts
  // JSON format: {"A":"xxx","B":"yyy"}
  if (typeof opts === 'string' && opts.trim().startsWith('{')) {
    try {
      const obj = JSON.parse(opts)
      return Object.entries(obj).map(([k, v]) => k + '. ' + v)
    } catch { /* fall through */ }
  }
  // Pipe-separated: "A.xxx|B.yyy" or newline-separated
  if (typeof opts === 'string') {
    const parts = opts.includes('|') ? opts.split('|') : opts.split('\n')
    return parts.map(o => o.trim()).filter(o => o)
  }
  if (typeof opts === 'object') {
    return Object.entries(opts).map(([k, v]) => k + '. ' + v)
  }
  return []
}

const loadPapers = async () => {
  try {
    const res = await teacherApi.getPapers({ pageSize: 100 })
    const data = res.data
    papers.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { ElMessage.error('加载试卷列表失败') }
}

const loadPreview = async () => {
  if (!selectedPaperId.value) return
  previewLoading.value = true
  try {
    const res = await teacherApi.previewPaper(selectedPaperId.value)
    paper.value = res.data?.paper
    questions.value = res.data?.questions || []
  } catch (e) { ElMessage.error('加载试卷预览失败') } finally { previewLoading.value = false }
}

onMounted(async () => {
  await loadPapers()
  const pid = route.query.paperId
  if (pid) {
    selectedPaperId.value = Number(pid)
    loadPreview()
  }
})
</script>

<style scoped>
.paper-preview { max-width: 900px; }
.paper-header { text-align: center; margin-bottom: 32px; }
.paper-header h2 { margin-bottom: 12px; }
.paper-meta { display: flex; gap: 8px; justify-content: center; }
.question-block { border: 1px solid #ebeef5; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.question-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.question-index { font-weight: bold; font-size: 16px; }
.question-score { color: #909399; font-size: 13px; }
.question-title { font-size: 15px; line-height: 1.6; margin-bottom: 8px; }
.question-options { margin: 8px 0 8px 24px; }
.option-item { line-height: 1.8; font-size: 14px; }
.question-answer { margin-top: 8px; padding-top: 8px; border-top: 1px dashed #ebeef5; }
</style>