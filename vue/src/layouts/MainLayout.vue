<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span>在线考试系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <template v-if="userStore.role === 'admin'">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/students">
            <el-icon><User /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/teachers">
            <el-icon><UserFilled /></el-icon>
            <span>教师管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/departments">
            <el-icon><OfficeBuilding /></el-icon>
            <span>班级/院系管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/subjects">
            <el-icon><Collection /></el-icon>
            <span>科目管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <span>系统日志</span>
          </el-menu-item>
        </template>

        <template v-else-if="userStore.role === 'teacher'">
          <el-menu-item index="/teacher/question">
            <el-icon><Document /></el-icon>
            <span>题库管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/manual">
            <el-icon><Edit /></el-icon>
            <span>手动组卷</span>
          </el-menu-item>
          <el-menu-item index="/teacher/auto">
            <el-icon><Setting /></el-icon>
            <span>自动组卷</span>
          </el-menu-item>
          <el-menu-item index="/teacher/publish">
            <el-icon><Promotion /></el-icon>
            <span>发布考试</span>
          </el-menu-item>
          <el-menu-item index="/teacher/monitor">
            <el-icon><Monitor /></el-icon>
            <span>考试监控</span>
          </el-menu-item>
          <el-menu-item index="/teacher/analysis">
            <el-icon><PieChart /></el-icon>
            <span>成绩分析</span>
          </el-menu-item>
          <el-menu-item index="/teacher/grading">
            <el-icon><EditPen /></el-icon>
            <span>主观题阅卷</span>
          </el-menu-item>
        </template>

        <template v-else-if="userStore.role === 'student'">
          <el-menu-item index="/student/exams">
            <el-icon><List /></el-icon>
            <span>我的考试</span>
          </el-menu-item>
          <el-menu-item index="/student/wrongbook">
            <el-icon><Notebook /></el-icon>
            <span>错题本</span>
          </el-menu-item>
          <el-menu-item index="/student/records">
            <el-icon><Tickets /></el-icon>
            <span>考试记录</span>
          </el-menu-item>
          <el-menu-item index="/student/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' + userStore.role }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.name }} ({{ roleName }})
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ROLE_MAP } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const roleName = computed(() => ROLE_MAP[userStore.role] || '')
const currentPageName = computed(() => route.meta.title || '')

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  } else {
    if (userStore.role === 'student') {
      router.push('/student/profile')
    } else if (userStore.role === 'admin') {
      router.push('/admin/dashboard')
    } else if (userStore.role === 'teacher') {
      ElMessage.info('教师个人中心请通过个人信息页面访问')
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}
.sidebar {
  background-color: #304156;
  height: 100%;
  overflow-y: auto;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4a;
}
.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-right {
  cursor: pointer;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
}
.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>