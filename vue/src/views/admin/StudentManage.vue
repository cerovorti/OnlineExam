<template>
  <div class="main-container">
    <div class="page-header">
      <h2>学生管理</h2>
      <div>
        <el-button @click="handleDownloadTemplate">下载模板</el-button>
        <el-button type="primary" @click="importVisible = true">批量导入</el-button>
        <el-button type="success" @click="handleAdd">新增学生</el-button>
      </div>
    </div>

    <el-card v-loading="loading">
      <el-table :data="tableData" stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column label="班级" width="120">
          <template #default="{ row }">{{ getClassName(row.classId) }}</template>
        </el-table-column>
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无学生数据" />
      <el-pagination
        v-if="total > 0"
        style="margin-top:20px; justify-content:flex-end"
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑学生' : '新增学生'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学号" required>
          <el-input v-model="form.studentNo" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.studentName" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="form.grade" />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.classId" placeholder="请选择班级" clearable style="width:100%">
            <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="批量导入学生" width="480px" @closed="importFile = null">
      <input ref="fileInputRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleFileSelected" />
      <div class="upload-zone" @click="$refs.fileInputRef.click()" @dragover.prevent @drop.prevent="handleDrop">
        <el-icon :size="48" color="#409eff"><upload-filled /></el-icon>
        <div class="upload-text">{{ importFile ? importFile.name : '将Excel文件拖到此处，或点击上传' }}</div>
      </div>
      <div style="margin-top:12px; color:#909399; font-size:13px">
        模板格式：学号、姓名、班级ID（班级ID可在院系/班级管理页面查看）
      </div>
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
import { UploadFilled } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const loading = ref(false)
const formVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = ref({ studentNo: '', studentName: '', major: '', grade: '', classId: null })
const classList = ref([])
const importVisible = ref(false)
const importFile = ref(null)
const importing = ref(false)
const fileInputRef = ref(null)

const handleDownloadTemplate = async () => {
  try {
    const res = await adminApi.downloadTemplate()
    const url = URL.createObjectURL(res)
    const a = document.createElement('a')
    a.href = url
    a.download = 'student_template.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载模板失败')
  }
}

const handleFileSelected = (e) => {
  const file = e.target.files?.[0]
  if (file) importFile.value = file
  e.target.value = ''
}

const handleDrop = (e) => {
  const file = e.dataTransfer?.files?.[0]
  if (file) importFile.value = file
}

const doImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择Excel文件')
    return
  }
  importing.value = true
  try {
    await adminApi.importStudents(importFile.value)
    ElMessage.success('导入成功')
    importVisible.value = false
    importFile.value = null
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.data?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const getClassName = (classId) => {
  if (!classId) return '-'
  const cls = classList.value.find(c => c.id === classId)
  return cls ? cls.className : classId
}

const loadClasses = async () => {
  try {
    const res = await adminApi.getClasses({ pageSize: 1000 })
    const data = res.data
    classList.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { /* ignore */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getStudents({ page: currentPage.value, pageSize: pageSize.value })
    const data = res.data
    if (data?.records) {
      tableData.value = data.records
      total.value = data.total
    } else {
      tableData.value = Array.isArray(data) ? data : []
      total.value = tableData.value.length
    }
  } catch (e) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleAdd = () => {
  editingId.value = null
  form.value = { studentNo: '', studentName: '', major: '', grade: '', classId: null }
  formVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  form.value = { ...row }
  formVisible.value = true
}

const handleSave = async () => {
  if (!form.value.studentNo || !form.value.studentName) {
    ElMessage.warning('学号和姓名不能为空')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await adminApi.updateStudent(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await adminApi.addStudent(form.value)
      ElMessage.success('添加成功')
    }
    formVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除学生 ${row.studentName}(${row.studentNo}) 吗？`, '确认删除', {
      type: 'warning'
    })
    await adminApi.deleteStudent(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
  loadClasses()
})

onBeforeUnmount(() => {
  formVisible.value = false
  forceCleanDialogs()
})
</script>

<style scoped>
.upload-zone {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 36px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
}
.upload-zone:hover { border-color: #409eff; }
.upload-text { margin-top: 8px; color: #606266; font-size: 14px; }
</style>