<template>
  <div class="main-container">
    <div class="page-header">
      <h2>科目管理</h2>
      <el-button type="success" @click="handleAdd">新增科目</el-button>
    </div>

    <el-card v-loading="loading">
      <el-table :data="subjects" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="科目名称" />
        <el-table-column prop="code" label="科目编码" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && subjects.length === 0" description="暂无科目" />
    </el-card>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑科目' : '新增科目'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="科目名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="科目编码">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { subjectApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'

const loading = ref(false)
const saving = ref(false)
const subjects = ref([])
const formVisible = ref(false)
const editingId = ref(null)
const form = ref({ name: '', code: '', description: '' })

const loadSubjects = async () => {
  loading.value = true
  try {
    const res = await subjectApi.getSubjects()
    const data = res.data
    subjects.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) {
    ElMessage.error('加载科目失败')
  } finally { loading.value = false }
}

const handleAdd = () => {
  editingId.value = null
  form.value = { name: '', code: '', description: '' }
  formVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  form.value = { name: row.name, code: row.code, description: row.description }
  formVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) { ElMessage.warning('请输入科目名称'); return }
  saving.value = true
  try {
    if (editingId.value) {
      await subjectApi.updateSubject(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await subjectApi.addSubject(form.value)
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    loadSubjects()
  } catch (e) { ElMessage.error('操作失败') } finally { saving.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该科目吗？', '确认', { type: 'warning' })
    await subjectApi.deleteSubject(row.id)
    ElMessage.success('删除成功')
    loadSubjects()
  } catch (e) { /* cancelled */ }
}

onMounted(() => { loadSubjects() })
onBeforeUnmount(() => { formVisible.value = false; forceCleanDialogs() })
</script>
