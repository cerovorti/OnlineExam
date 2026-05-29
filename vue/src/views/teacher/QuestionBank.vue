<template>
  <div class="main-container">
    <div class="page-header">
      <h2>题库管理</h2>
      <div>
        <el-button type="success" @click="handleAdd">新增题目</el-button>
        <el-button @click="handleImport">导入Excel</el-button>
        <el-button @click="handleDownloadTemplate">下载模板</el-button>
      </div>
    </div>

    <el-card>
      <el-form :inline="true" class="filter-form">
        <el-form-item label="科目">
          <el-select v-model="filters.subjectId" placeholder="全部" clearable @change="handleSearch" style="width:150px">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="filters.type" placeholder="全部" clearable @change="handleSearch" style="width:130px">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="填空题" value="fill" />
            <el-option label="简答题" value="short_answer" />
            <el-option label="编程题" value="programming" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="filters.difficulty" placeholder="全部" clearable @change="handleSearch" style="width:120px">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="questions" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">{{ questionTypeName(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="题目" show-overflow-tooltip />
        <el-table-column label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="difficultyType(row.difficulty)" size="small">{{ difficultyName(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="70" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && questions.length === 0" description="暂无题目" />

      <el-pagination
        v-if="total > 0"
        style="margin-top:20px; justify-content:flex-end"
        background layout="prev, pager, next, total"
        :total="total" :page-size="pageSize" :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑题目' : '新增题目'" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="科目" required>
          <el-select v-model="form.subjectId" placeholder="请选择" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" required>
          <el-select v-model="form.type" style="width:100%">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="填空题" value="fill" />
            <el-option label="简答题" value="short_answer" />
            <el-option label="编程题" value="programming" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目" required>
          <el-input v-model="form.title" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="选项">
          <el-input v-model="form.options" type="textarea" :rows="3" placeholder="每行一个选项，如：A. 选项内容" />
        </el-form-item>
        <el-form-item label="答案" required>
          <el-input v-model="form.answer" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" style="width:100%">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoints" placeholder="多个用逗号隔开" />
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="导入题目" width="450px">
      <el-upload
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将Excel文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherApi, subjectApi } from '@/api'
import { questionTypeName, difficultyName, difficultyTagType as difficultyType } from '@/utils/constants'
import { forceCleanDialogs } from '@/utils/dialog'

const loading = ref(false)
const questions = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const filters = ref({ subjectId: null, type: '', difficulty: '' })
const subjects = ref([])

const formVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const importVisible = ref(false)
const importing = ref(false)
const importFile = ref(null)
const form = ref({ subjectId: null, type: 'single', title: '', options: '', answer: '', difficulty: 'medium', knowledgePoints: '', score: 1, analysis: '' })

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value }
    if (filters.value.subjectId) params.subjectId = filters.value.subjectId
    if (filters.value.type) params.type = filters.value.type
    if (filters.value.difficulty) params.difficulty = filters.value.difficulty
    const res = await teacherApi.getQuestions(params)
    const data = res.data
    if (data?.records) { questions.value = data.records; total.value = data.total }
    else { questions.value = Array.isArray(data) ? data : []; total.value = questions.value.length }
  } catch (e) { ElMessage.error('加载题目列表失败') } finally { loading.value = false }
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.getSubjects()
    const data = res.data
    subjects.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { /* optional */ }
}

const handleSearch = () => { currentPage.value = 1; loadData() }
const handlePageChange = (page) => { currentPage.value = page; loadData() }

const handleAdd = () => {
  editingId.value = null
  form.value = { subjectId: null, type: 'single', title: '', options: '', answer: '', difficulty: 'medium', knowledgePoints: '', score: 1, analysis: '' }
  formVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  form.value = { ...row }
  formVisible.value = true
}

const handleSave = async () => {
  if (!form.value.subjectId || !form.value.title || !form.value.answer) {
    ElMessage.warning('科目、题目、答案为必填项')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await teacherApi.updateQuestion(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await teacherApi.addQuestion(form.value)
      ElMessage.success('添加成功')
    }
    formVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('操作失败') } finally { saving.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该题目吗？`, '确认删除', { type: 'warning' })
    await teacherApi.deleteQuestion(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const handleImport = () => {
  importFile.value = null
  importVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const doImport = async () => {
  if (!importFile.value) { ElMessage.warning('请选择文件'); return }
  importing.value = true
  try {
    await teacherApi.importQuestions(importFile.value)
    ElMessage.success('导入成功')
    importVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('导入失败') } finally { importing.value = false }
}

const handleDownloadTemplate = () => {
  teacherApi.downloadQuestionTemplate().then(res => {
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url
    a.download = '题目导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  }).catch(() => ElMessage.error('下载模板失败'))
}

onMounted(() => { loadData(); loadSubjects() })

onBeforeUnmount(() => {
  formVisible.value = false
  importVisible.value = false
  forceCleanDialogs()
})
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>