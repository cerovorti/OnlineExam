<template>
  <div class="main-container">
    <div class="page-header">
      <h2>考试监控</h2>
    </div>

    <el-card>
      <el-form :inline="true" style="margin-bottom:20px">
        <el-form-item label="选择考试">
          <el-select v-model="selectedExamId" placeholder="请选择考试" style="width:300px" @change="startMonitor">
            <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-tag v-if="monitorData" type="success">
            刷新间隔: 10秒 | 已交卷: {{ monitorData.submittedCount || 0 }}/{{ monitorData.totalCount || 0 }}
          </el-tag>
        </el-form-item>
      </el-form>

      <div v-if="!selectedExamId" style="text-align:center; padding:40px">
        <el-empty description="请选择考试查看监控" />
      </div>

      <div v-if="monitorData && selectedExamId" v-loading="monitorLoading">
        <el-table :data="monitorData.records || []" stripe>
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'in_progress'" type="warning">考试中</el-tag>
              <el-tag v-else-if="row.status === 'submitted'" type="success">已交卷</el-tag>
              <el-tag v-else-if="row.status === 'timeout'" type="info">超时交卷</el-tag>
              <el-tag v-else-if="row.status === 'auto_submitted'" type="danger">强制作</el-tag>
              <el-tag v-else type="info">未开始</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="开始答题" width="170">
            <template #default="{ row }">{{ row.startTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="已用时(分钟)" width="100">
            <template #default="{ row }">{{ row.elapsedMinutes != null ? row.elapsedMinutes : '-' }}</template>
          </el-table-column>
          <el-table-column label="切屏次数" width="90">
            <template #default="{ row }">{{ row.cutScreenCount || 0 }}</template>
          </el-table-column>
          <el-table-column label="得分" width="80">
            <template #default="{ row }">{{ row.score != null ? row.score : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'in_progress'"
                type="warning"
                size="small"
                link
                @click="showExtendDialog(row)"
              >延时</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model="extendVisible" title="延长考试时间" width="400px">
      <el-form label-width="100px">
        <el-form-item label="学生">
          <span>{{ extendStudentName }}</span>
        </el-form-item>
        <el-form-item label="延长分钟">
          <el-input-number v-model="extendMinutes" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="extendReason" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extendVisible = false">取消</el-button>
        <el-button type="primary" :loading="extending" @click="doExtend">确认延时</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'
import { forceCleanDialogs } from '@/utils/dialog'

const exams = ref([])
const selectedExamId = ref(null)
const monitorData = ref(null)
const monitorLoading = ref(false)
let pollTimer = null

const extendVisible = ref(false)
const extending = ref(false)
const extendStudentName = ref('')
const extendRecordId = ref(null)
const extendMinutes = ref(10)
const extendReason = ref('')

const loadExams = async () => {
  try {
    const res = await teacherApi.getExams({ pageSize: 100 })
    const data = res.data
    exams.value = data?.records || (Array.isArray(data) ? data : [])
  } catch (e) { /* ignore */ }
}

const fetchMonitor = async () => {
  if (!selectedExamId.value) return
  monitorLoading.value = true
  try {
    const res = await teacherApi.getExamMonitor(selectedExamId.value)
    monitorData.value = res.data
  } catch (e) {
    ElMessage.error('获取监控数据失败')
    stopPoll()
  } finally { monitorLoading.value = false }
}

const startMonitor = () => {
  stopPoll()
  fetchMonitor()
  pollTimer = setInterval(fetchMonitor, 10000)
}

const stopPoll = () => {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

const showExtendDialog = (row) => {
  extendRecordId.value = row.recordId || row.id
  extendStudentName.value = row.studentName
  extendMinutes.value = 10
  extendReason.value = ''
  extendVisible.value = true
}

const doExtend = async () => {
  extending.value = true
  try {
    await teacherApi.extendTime(extendRecordId.value, {
      extendMinutes: extendMinutes.value,
      reason: extendReason.value
    })
    ElMessage.success('延时成功')
    extendVisible.value = false
    fetchMonitor()
  } catch (e) { ElMessage.error('延时失败') } finally { extending.value = false }
}

onMounted(() => { loadExams() })

onBeforeUnmount(() => {
  stopPoll()
  extendVisible.value = false
  forceCleanDialogs()
})
</script>
