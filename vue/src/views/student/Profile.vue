<template>
  <div class="main-container">
    <div class="page-header">
      <h2>个人信息</h2>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="10">
        <el-card header="基本资料">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="姓名">{{ profile.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ profile.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="roleTagType" size="small">{{ roleName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="profile.studentNo" label="学号">{{ profile.studentNo }}</el-descriptions-item>
            <el-descriptions-item v-if="profile.studentName" label="姓名(学籍)">{{ profile.studentName }}</el-descriptions-item>
            <el-descriptions-item v-if="profile.major" label="专业">{{ profile.major }}</el-descriptions-item>
            <el-descriptions-item v-if="profile.grade" label="年级">{{ profile.grade }}</el-descriptions-item>
            <el-descriptions-item v-if="profile.teacherNo" label="工号">{{ profile.teacherNo }}</el-descriptions-item>
            <el-descriptions-item v-if="profile.title" label="职称">{{ profile.title }}</el-descriptions-item>
            <el-descriptions-item v-if="profile.phone" label="手机号">{{ profile.phone }}</el-descriptions-item>
            <el-descriptions-item v-if="profile.email" label="邮箱">{{ profile.email }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card header="修改密码">
          <el-form :model="form" label-width="100px" @submit.prevent="handleSave">
            <el-form-item label="原密码" required>
              <el-input v-model="form.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input v-model="form.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" required>
              <el-input v-model="form.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { ROLE_MAP, ROLE_TAG_MAP } from '@/utils/constants'

const loading = ref(false)
const saving = ref(false)
const profile = ref({})
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleName = computed(() => ROLE_MAP[profile.value.role] || profile.value.role)

const roleTagType = computed(() => ROLE_TAG_MAP[profile.value.role] || 'info')

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await userApi.getProfile()
    profile.value = res.data || {}
  } catch (e) {
    ElMessage.error('加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!form.value.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!form.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (form.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }

  saving.value = true
  try {
    await userApi.updatePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    })
    ElMessage.success('密码修改成功')
    form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    ElMessage.error(e?.data?.message || '密码修改失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>