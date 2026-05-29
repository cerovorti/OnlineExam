<template>
  <div class="main-container">
    <div class="page-header">
      <h2>教师管理</h2>
      <div>
        <el-button type="success" @click="handleAdd">新增教师</el-button>
      </div>
    </div>

    <el-card v-loading="loading">
      <el-table :data="teachers" stripe>
        <el-table-column prop="teacherNo" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="departmentName" label="所属院系" width="140" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && teachers.length === 0" description="暂无教师数据" />
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

    <el-dialog v-model="formVisible" :title="editingId ? '编辑教师' : '新增教师'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="工号" required>
          <el-input v-model="form.teacherNo" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="所属院系">
          <el-select v-model="form.departmentId" placeholder="请选择院系" clearable style="width:100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
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
import { adminApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'

const teachers = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const loading = ref(false)
const formVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = ref({ teacherNo: '', title: '', departmentId: null })
const departments = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getTeachers({ page: currentPage.value, pageSize: pageSize.value })
    const data = res.data
    if (data?.records) {
      teachers.value = data.records
      total.value = data.total
    } else {
      teachers.value = Array.isArray(data) ? data : []
      total.value = teachers.value.length
    }
  } catch (e) {
    ElMessage.error('加载教师列表失败')
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
  form.value = { teacherNo: '', title: '', departmentId: null }
  formVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  form.value = { ...row }
  formVisible.value = true
}

const handleSave = async () => {
  if (!form.value.teacherNo) {
    ElMessage.warning('工号不能为空')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await adminApi.updateTeacher(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await adminApi.addTeacher(form.value)
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
    await ElMessageBox.confirm(`确定要删除教师 ${row.teacherNo} 吗？`, '确认删除', { type: 'warning' })
    await adminApi.deleteTeacher(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重置教师 ${row.teacherNo} 的密码为 123456 吗？`, '重置密码', { type: 'warning' })
    await adminApi.resetTeacherPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const loadDepartments = async () => {
  try {
    const res = await adminApi.getDepartments()
    const data = res.data
    departments.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  loadData()
  loadDepartments()
})

onBeforeUnmount(() => {
  formVisible.value = false
  forceCleanDialogs()
})
</script>