<template>
  <div class="main-container">
    <div class="page-header">
      <h2>发布考试</h2>
    </div>

    <el-card>
      <el-table :data="exams" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="考试名称" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ row.startTime }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ row.endTime }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" link @click="openPublishDialog(row)" :disabled="row.status==='published'">发布</el-button>
            <el-button link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && exams.length === 0" description="暂无考试" />

      <div style="margin-top:16px">
        <el-button type="primary" @click="openCreateDialog">创建考试</el-button>
      </div>
    </el-card>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑考试' : '创建考试'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="考试名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="选择试卷" required>
          <el-select v-model="form.paperId" placeholder="请选择试卷" style="width:100%">
            <el-option v-for="p in papers" :key="p.id" :label="`${p.name} (${p.totalScore}分, ${p.questionCount}题)`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="考试须知">
          <el-input v-model="form.notice" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="防作弊">
          <el-switch v-model="form.antiCheatEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="最大切屏次数" v-if="form.antiCheatEnabled">
          <el-input-number v-model="form.maxCutScreen" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="随机打乱题目">
          <el-switch v-model="form.shuffleQuestions" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="随机打乱选项">
          <el-switch v-model="form.shuffleOptions" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveExam">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="publishVisible" title="发布考试" width="600px">
      <el-descriptions :column="2" border v-if="currentExam">
        <el-descriptions-item label="考试名称">{{ currentExam.name }}</el-descriptions-item>
        <el-descriptions-item label="考试时间">{{ currentExam.startTime }} ~ {{ currentExam.endTime }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin:16px 0 8px">分配班级</h4>
      <div style="margin-bottom:12px">
        <el-select v-model="selectedClassId" placeholder="选择班级" style="width:200px" @change="addClassToExam">
          <el-option v-for="c in classes" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
      </div>
      <el-table :data="examClasses" stripe max-height="250">
        <el-table-column prop="classId" label="班级ID" width="80" />
        <el-table-column prop="className" label="班级名称" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="removeClassFromExam(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="examClasses.length === 0" description="未分配班级" :image-size="40" />

      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherApi, adminApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'

const loading = ref(false)
const exams = ref([])
const papers = ref([])
const classes = ref([])

const formVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = ref({
  name: '', paperId: null, startTime: '', endTime: '', notice: '',
  antiCheatEnabled: 0, maxCutScreen: 3, shuffleQuestions: 0, shuffleOptions: 0
})

const publishVisible = ref(false)
const publishing = ref(false)
const currentExam = ref(null)
const examClasses = ref([])
const selectedClassId = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await teacherApi.getExams()
    const data = res.data
    exams.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { ElMessage.error('加载考试列表失败') } finally { loading.value = false }
}

const loadPapers = async () => {
  try {
    const res = await teacherApi.getPapers({ pageSize: 100 })
    const data = res.data
    papers.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { /* ignore */ }
}

const loadClasses = async () => {
  try {
    const res = await adminApi.getClasses()
    const data = res.data
    classes.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { /* ignore */ }
}

const openCreateDialog = () => {
  editingId.value = null
  form.value = {
    name: '', paperId: null, startTime: '', endTime: '', notice: '',
    antiCheatEnabled: 0, maxCutScreen: 3, shuffleQuestions: 0, shuffleOptions: 0
  }
  formVisible.value = true
}

const openEditDialog = (row) => {
  editingId.value = row.id
  form.value = { ...row }
  formVisible.value = true
}

const handleSaveExam = async () => {
  if (!form.value.name || !form.value.paperId || !form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请完善考试信息')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await teacherApi.updateExam(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await teacherApi.createExam(form.value)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('操作失败') } finally { saving.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除考试 ${row.name} 吗？`, '确认删除', { type: 'warning' })
    await teacherApi.deleteExam(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const openPublishDialog = async (row) => {
  currentExam.value = row
  publishVisible.value = true
}

const addClassToExam = async (classId) => {
  if (!classId) return
  try {
    await teacherApi.addClassToExam(currentExam.value.id, { classId })
    const cls = classes.value.find(c => c.id === classId)
    examClasses.value.push({ classId, className: cls?.className || classId })
    selectedClassId.value = null
  } catch (e) {
    const msg = e?.response?.data?.message || '添加失败'
    ElMessage.error(msg)
  }
}

const removeClassFromExam = async (row) => {
  try {
    await teacherApi.removeClassFromExam(currentExam.value.id, row.classId)
    examClasses.value = examClasses.value.filter(c => c.classId !== row.classId)
    ElMessage.success('移除成功')
  } catch (e) { ElMessage.error('操作失败') }
}

const handlePublish = async () => {
  publishing.value = true
  try {
    await teacherApi.publishExam(currentExam.value.id)
    ElMessage.success('考试发布成功')
    publishVisible.value = false
    examClasses.value = []
    loadData()
  } catch (e) {
    const msg = e?.response?.data?.message || '发布失败'
    ElMessage.error(msg)
  } finally { publishing.value = false }
}

onMounted(() => { loadData(); loadPapers(); loadClasses() })

onBeforeUnmount(() => {
  formVisible.value = false
  publishVisible.value = false
  forceCleanDialogs()
})
</script>