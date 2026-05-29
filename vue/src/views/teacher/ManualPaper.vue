<template>
  <div class="main-container">
    <div class="page-header">
      <h2>手动组卷</h2>
      <el-button type="primary" @click="showCreateDialog">创建试卷</el-button>
    </div>

    <el-card>
      <el-table :data="papers" v-loading="paperLoading" stripe @row-click="selectPaper">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="试卷名称" />
        <el-table-column prop="questionCount" label="题目数量" width="100" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="duration" label="考试时长(分钟)" width="120" />
        <el-table-column prop="passScore" label="及格分" width="80" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="danger" link @click.stop="handleDeletePaper(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!paperLoading && papers.length === 0" description="暂无试卷，请先创建" />

      <el-divider v-if="selectedPaperId" />
      <h3 v-if="selectedPaperId" style="margin-bottom:16px">为试卷添加题目</h3>

      <el-form v-if="selectedPaperId" :inline="true" class="filter-form">
        <el-form-item label="题型">
          <el-select v-model="qFilter.type" placeholder="全部" clearable @change="searchQuestions" style="width:130px">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="填空题" value="fill" />
            <el-option label="简答题" value="short_answer" />
            <el-option label="编程题" value="programming" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="qFilter.difficulty" placeholder="全部" clearable @change="searchQuestions" style="width:100px">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchQuestions">搜索题目</el-button>
        </el-form-item>
      </el-form>

      <el-table v-if="selectedPaperId" :data="availableQuestions" v-loading="questionLoading" stripe style="margin-bottom:16px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">{{ typeName(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="题目" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="70" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="addToPaper(row)">添加</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="paperQuestions.length > 0" style="margin-top:16px">
        <h4>试卷已添加题目 ({{ paperQuestions.length }} 题，共 {{ totalPaperScore }} 分)</h4>
        <el-table :data="paperQuestions" stripe>
          <el-table-column prop="sortOrder" label="序号" width="60" />
          <el-table-column label="题型" width="90">
            <template #default="{ row }">{{ typeName(row.type) }}</template>
          </el-table-column>
          <el-table-column prop="title" label="题目" show-overflow-tooltip />
          <el-table-column prop="score" label="分值" width="70" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" link @click="removeFromPaper(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="创建试卷" width="450px">
      <el-form :model="paperForm" label-width="100px">
        <el-form-item label="试卷名称" required>
          <el-input v-model="paperForm.name" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="paperForm.subjectId" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时长(分钟)">
          <el-input-number v-model="paperForm.duration" :min="10" :max="300" />
        </el-form-item>
        <el-form-item label="及格分">
          <el-input-number v-model="paperForm.passScore" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreatePaper">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherApi, subjectApi } from '@/api'
import { questionTypeName as typeName } from '@/utils/constants'
import { forceCleanDialogs } from '@/utils/dialog'

const paperLoading = ref(false)
const papers = ref([])
const questionLoading = ref(false)
const availableQuestions = ref([])
const paperQuestions = ref([])
const selectedPaperId = ref(null)
const subjects = ref([])

const createVisible = ref(false)
const creating = ref(false)
const paperForm = ref({ name: '', subjectId: null, duration: 120, passScore: 60 })

const qFilter = ref({ type: '', difficulty: '' })

const totalPaperScore = computed(() => paperQuestions.value.reduce((sum, q) => sum + (q.score || 0), 0))

const loadPapers = async () => {
  paperLoading.value = true
  try {
    const res = await teacherApi.getPapers()
    const data = res.data
    papers.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { ElMessage.error('加载试卷列表失败') } finally { paperLoading.value = false }
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.getSubjects()
    const data = res.data
    subjects.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { /* ignore */ }
}

const searchQuestions = async () => {
  questionLoading.value = true
  try {
    const params = { pageSize: 100 }
    if (qFilter.value.type) params.type = qFilter.value.type
    if (qFilter.value.difficulty) params.difficulty = qFilter.value.difficulty
    const res = await teacherApi.getQuestions(params)
    const data = res.data
    availableQuestions.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { ElMessage.error('搜索题目失败') } finally { questionLoading.value = false }
}

const selectPaper = async (row) => {
  selectedPaperId.value = row.id
  try {
    const res = await teacherApi.previewPaper(row.id)
    paperQuestions.value = res.data?.questions || []
  } catch (e) { ElMessage.error('加载试卷题目失败') }
}

const addToPaper = async (question) => {
  try {
    await teacherApi.addQuestionToPaper(selectedPaperId.value, {
      questionId: question.id,
      score: question.score || 1
    })
    ElMessage.success('添加成功')
    selectPaper({ id: selectedPaperId.value })
  } catch (e) {
    const msg = e?.response?.data?.message || '添加失败'
    ElMessage.error(msg)
  }
}

const removeFromPaper = async (row) => {
  try {
    await teacherApi.removeQuestionFromPaper(selectedPaperId.value, row.id)
    ElMessage.success('移除成功')
    selectPaper({ id: selectedPaperId.value })
  } catch (e) { ElMessage.error('移除失败') }
}

const handleCreatePaper = async () => {
  if (!paperForm.value.name) { ElMessage.warning('请输入试卷名称'); return }
  creating.value = true
  try {
    const res = await teacherApi.createPaper(paperForm.value)
    ElMessage.success('创建成功')
    createVisible.value = false
    selectedPaperId.value = res.data.id
    paperQuestions.value = []
    loadPapers()
  } catch (e) { ElMessage.error('创建失败') } finally { creating.value = false }
}

const handleDeletePaper = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除试卷 ${row.name} 吗？`, '确认删除', { type: 'warning' })
    await teacherApi.deletePaper(row.id)
    ElMessage.success('删除成功')
    if (selectedPaperId.value === row.id) { selectedPaperId.value = null; paperQuestions.value = [] }
    loadPapers()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const showCreateDialog = () => {
  paperForm.value = { name: '', subjectId: null, duration: 120, passScore: 60 }
  createVisible.value = true
}

onMounted(() => { loadPapers(); loadSubjects() })

onBeforeUnmount(() => {
  createVisible.value = false
  forceCleanDialogs()
})
</script>

<style scoped>
.filter-form { margin-bottom: 0; }
</style>