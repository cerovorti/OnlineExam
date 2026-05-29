<template>
  <div class="main-container">
    <div class="page-header">
      <h2>系统日志</h2>
      
    </div>

    <el-card>
      <el-form :inline="true" class="filter-form">
        <el-form-item label="操作类型">
          <el-select v-model="filters.action" placeholder="全部" clearable style="width: 150px" @change="handleSearch">
            <el-option label="登录" value="login" />
            <el-option label="发布考试" value="publish" />
            <el-option label="完成考试" value="complete" />
            <el-option label="导入数据" value="import" />
            <el-option label="创建试卷" value="create_paper" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filters.role" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="120" />
        <el-table-column prop="description" label="操作描述" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="操作时间" width="180">
          <template #default="{ row }">{{ row.createdAt || row.created_at }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && logs.length === 0" description="暂无日志" />

      <el-pagination
        v-if="total > 0"
        style="margin-top:20px; justify-content:flex-end"
        background
        layout="prev, pager, next, sizes, total"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'
import { roleTagType } from '@/utils/constants'

const filters = ref({ action: '', role: '' })
const logs = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value }
    if (filters.value.action) params.action = filters.value.action
    if (filters.value.role) params.role = filters.value.role
    const res = await adminApi.getLogs(params)
    const data = res.data
    if (data?.records) {
      logs.value = data.records
      total.value = data.total
    } else {
      logs.value = Array.isArray(data) ? data : []
      total.value = logs.value.length
    }
  } catch (e) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const resetFilters = () => {
  filters.value = { action: '', role: '' }
  currentPage.value = 1
  loadData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.filter-form {
  margin-bottom: 20px;
}
</style>