<template>
  <div class="main-container">
    <div class="page-header">
      <h2>自动组卷</h2>
    </div>

    <el-card>
      <el-form :model="form" label-width="120px" style="max-width:700px">
        <el-form-item label="试卷名称" required>
          <el-input v-model="form.name" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="科目" required>
          <el-select v-model="form.subjectId" placeholder="请选择科目" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时长(分钟)">
          <el-input-number v-model="form.duration" :min="10" :max="300" />
        </el-form-item>
        <el-form-item label="及格分">
          <el-input-number v-model="form.passScore" :min="0" :max="999" />
        </el-form-item>
      </el-form>

      <h4 style="margin:20px 0 12px">题目配置</h4>
      <div v-for="(cfg, idx) in form.configs" :key="idx" style="display:flex; gap:12px; margin-bottom:10px; align-items:center">
        <el-select v-model="cfg.type" style="width:130px">
          <el-option label="单选题" value="single" />
          <el-option label="多选题" value="multiple" />
          <el-option label="判断题" value="judge" />
          <el-option label="填空题" value="fill" />
          <el-option label="简答题" value="short_answer" />
          <el-option label="编程题" value="programming" />
        </el-select>
        <el-input-number v-model="cfg.count" :min="1" :max="100" placeholder="数量" style="width:100px" />
        <el-input-number v-model="cfg.score" :min="1" :max="100" placeholder="分值" style="width:100px" />
        <el-select v-model="cfg.difficulty" style="width:100px">
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
        </el-select>
        <el-button type="danger" link @click="removeConfig(idx)" :disabled="form.configs.length <= 1">删除</el-button>
      </div>
      <el-button type="primary" link @click="addConfig" style="margin-top:8px">+ 添加题目类型</el-button>

      <div style="margin-top:24px">
        <el-button type="primary" :loading="generating" @click="handleGenerate">开始自动组卷</el-button>
      </div>
    </el-card>

    <el-dialog v-model="resultVisible" title="组卷结果" width="600px">
      <div v-if="resultPaper">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="试卷名称">{{ resultPaper.name }}</el-descriptions-item>
          <el-descriptions-item label="试卷ID">{{ resultPaper.id }}</el-descriptions-item>
          <el-descriptions-item label="题目数量">{{ resultPaper.questionCount }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ resultPaper.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="考试时长">{{ resultPaper.duration }} 分钟</el-descriptions-item>
          <el-descriptions-item label="及格分">{{ resultPaper.passScore }}</el-descriptions-item>
        </el-descriptions>
        <div style="text-align:center; margin-top:24px">
          <el-button type="primary" @click="$router.push({ path: '/teacher/preview', query: { paperId: resultPaper.id } })">查看试卷详情</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi, subjectApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'

const subjects = ref([])
const generating = ref(false)
const resultVisible = ref(false)
const resultPaper = ref(null)

const form = ref({
  name: '',
  subjectId: null,
  duration: 120,
  passScore: 60,
  configs: [{ type: 'single', count: 10, score: 2, difficulty: 'medium' }]
})

const addConfig = () => {
  form.value.configs.push({ type: 'single', count: 5, score: 1, difficulty: 'medium' })
}

const removeConfig = (idx) => {
  form.value.configs.splice(idx, 1)
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.getSubjects()
    const data = res.data
    subjects.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) {
    ElMessage.warning('加载科目失败，请检查后端服务')
  }
}

const handleGenerate = async () => {
  if (!form.value.name) { ElMessage.warning('请输入试卷名称'); return }
  if (!form.value.subjectId) { ElMessage.warning('请选择科目'); return }
  generating.value = true
  try {
    const res = await teacherApi.autoGeneratePaper(form.value)
    resultPaper.value = res.data
    resultVisible.value = true
    ElMessage.success('自动组卷成功')
  } catch (e) {
    const msg = e?.response?.data?.message || '组卷失败'
    ElMessage.error(msg)
  } finally { generating.value = false }
}

onMounted(() => { loadSubjects() })

onBeforeUnmount(() => {
  resultVisible.value = false
  forceCleanDialogs()
})
</script>