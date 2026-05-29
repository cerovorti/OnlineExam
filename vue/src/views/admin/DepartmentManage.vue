<template>
  <div class="main-container">
    <div class="page-header">
      <h2>班级/院系管理</h2>
      <div>
        <el-button type="primary" @click="handleAddDept">新增院系</el-button>
        <el-button type="success" @click="handleAddClass">新增班级</el-button>
      </div>
    </div>

    <el-row :gutter="20" v-loading="deptLoading || classLoading">
      <el-col :span="10">
        <el-card header="院系列表">
          <el-table :data="departments" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="院系名称" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditDept(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteDept(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!deptLoading && departments.length === 0" description="暂无院系数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card header="班级列表">
          <el-table :data="classes" stripe>
            <el-table-column prop="className" label="班级" />
            <el-table-column prop="major" label="专业" />
            <el-table-column prop="grade" label="年级" width="90" />
            <el-table-column prop="studentCount" label="人数" width="80" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditClass(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteClass(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!classLoading && classes.length === 0" description="暂无班级数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="deptFormVisible" :title="editingDeptId ? '编辑院系' : '新增院系'" width="450px">
      <el-form :model="deptForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="deptForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="deptForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="deptSaving" @click="handleSaveDept">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="classFormVisible" :title="editingClassId ? '编辑班级' : '新增班级'" width="500px">
      <el-form :model="classForm" label-width="80px">
        <el-form-item label="班级名称" required>
          <el-input v-model="classForm.className" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="classForm.major" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="classForm.grade" />
        </el-form-item>
        <el-form-item label="所属院系">
          <el-select v-model="classForm.departmentId" placeholder="请选择院系" style="width:100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="classSaving" @click="handleSaveClass">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'

const deptLoading = ref(false)
const classLoading = ref(false)
const departments = ref([])
const classes = ref([])

const deptFormVisible = ref(false)
const deptSaving = ref(false)
const editingDeptId = ref(null)
const deptForm = ref({ name: '', description: '' })

const classFormVisible = ref(false)
const classSaving = ref(false)
const editingClassId = ref(null)
const classForm = ref({ className: '', major: '', grade: '', departmentId: null })

const loadDepartments = async () => {
  deptLoading.value = true
  try {
    const res = await adminApi.getDepartments()
    const data = res.data
    departments.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) {
    ElMessage.error('加载院系列表失败')
  } finally {
    deptLoading.value = false
  }
}

const loadClasses = async () => {
  classLoading.value = true
  try {
    const res = await adminApi.getClasses()
    const data = res.data
    classes.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) {
    ElMessage.error('加载班级列表失败')
  } finally {
    classLoading.value = false
  }
}

const handleAddDept = () => {
  editingDeptId.value = null
  deptForm.value = { name: '', description: '' }
  deptFormVisible.value = true
}

const handleEditDept = (row) => {
  editingDeptId.value = row.id
  deptForm.value = { ...row }
  deptFormVisible.value = true
}

const handleSaveDept = async () => {
  if (!deptForm.value.name) {
    ElMessage.warning('院系名称不能为空')
    return
  }
  deptSaving.value = true
  try {
    if (editingDeptId.value) {
      await adminApi.updateDepartment(editingDeptId.value, deptForm.value)
      ElMessage.success('修改成功')
    } else {
      await adminApi.addDepartment(deptForm.value)
      ElMessage.success('添加成功')
    }
    deptFormVisible.value = false
    loadDepartments()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    deptSaving.value = false
  }
}

const handleDeleteDept = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除院系 ${row.name} 吗？`, '确认删除', { type: 'warning' })
    await adminApi.deleteDepartment(row.id)
    ElMessage.success('删除成功')
    loadDepartments()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleAddClass = () => {
  editingClassId.value = null
  classForm.value = { className: '', major: '', grade: '', departmentId: null }
  classFormVisible.value = true
}

const handleEditClass = (row) => {
  editingClassId.value = row.id
  classForm.value = { ...row }
  classFormVisible.value = true
}

const handleSaveClass = async () => {
  if (!classForm.value.className) {
    ElMessage.warning('班级名称不能为空')
    return
  }
  classSaving.value = true
  try {
    if (editingClassId.value) {
      await adminApi.updateClass(editingClassId.value, classForm.value)
      ElMessage.success('修改成功')
    } else {
      await adminApi.addClass(classForm.value)
      ElMessage.success('添加成功')
    }
    classFormVisible.value = false
    loadClasses()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    classSaving.value = false
  }
}

const handleDeleteClass = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除班级 ${row.className} 吗？`, '确认删除', { type: 'warning' })
    await adminApi.deleteClass(row.id)
    ElMessage.success('删除成功')
    loadClasses()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadDepartments()
  loadClasses()
})

onBeforeUnmount(() => {
  deptFormVisible.value = false
  classFormVisible.value = false
  forceCleanDialogs()
})
</script>