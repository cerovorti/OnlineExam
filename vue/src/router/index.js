import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login, meta: { title: '登录' } },
  {
    path: '/admin',
    component: MainLayout,
    meta: { role: 'admin', title: '管理员首页' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'students', component: () => import('../views/admin/StudentManage.vue'), meta: { title: '学生管理' } },
      { path: 'teachers', component: () => import('../views/admin/TeacherManage.vue'), meta: { title: '教师管理' } },
      { path: 'departments', component: () => import('../views/admin/DepartmentManage.vue'), meta: { title: '班级/院系管理' } },
      { path: 'subjects', component: () => import('../views/admin/SubjectManage.vue'), meta: { title: '科目管理' } },
      { path: 'logs', component: () => import('../views/admin/SystemLog.vue'), meta: { title: '系统日志' } },
    ]
  },
  {
    path: '/teacher',
    component: MainLayout,
    meta: { role: 'teacher', title: '教师首页' },
    children: [
      { path: '', redirect: '/teacher/question' },
      { path: 'question', component: () => import('../views/teacher/QuestionBank.vue'), meta: { title: '题库管理' } },
      { path: 'manual', component: () => import('../views/teacher/ManualPaper.vue'), meta: { title: '手动组卷' } },
      { path: 'auto', component: () => import('../views/teacher/AutoPaper.vue'), meta: { title: '自动组卷' } },
      { path: 'publish', component: () => import('../views/teacher/PublishExam.vue'), meta: { title: '发布考试' } },
      { path: 'monitor', component: () => import('../views/teacher/ExamMonitor.vue'), meta: { title: '考试监控' } },
      { path: 'preview', component: () => import('../views/teacher/PaperPreview.vue'), meta: { title: '试卷预览' } },
      { path: 'analysis', component: () => import('../views/teacher/ScoreAnalysis.vue'), meta: { title: '成绩分析' } },
      { path: 'grading', component: () => import('../views/teacher/TeacherGrading.vue'), meta: { title: '主观题阅卷' } },
    ]
  },
  {
    path: '/student',
    component: MainLayout,
    meta: { role: 'student', title: '学生首页' },
    children: [
      { path: '', redirect: '/student/exams' },
      { path: 'exams', component: () => import('../views/student/ExamList.vue'), meta: { title: '我的考试' } },
      { path: 'exam/:id', component: () => import('../views/student/ExamTaking.vue'), meta: { title: '在线考试' } },
      { path: 'submitted', component: () => import('../views/student/ExamSubmitted.vue'), meta: { title: '已交卷' } },
      { path: 'records', component: () => import('../views/student/ExamRecords.vue'), meta: { title: '考试记录' } },
      { path: 'wrongbook', component: () => import('../views/student/WrongBook.vue'), meta: { title: '错题本' } },
      { path: 'profile', component: () => import('../views/student/Profile.vue'), meta: { title: '个人信息' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let sessionRestored = false

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    if (token) next()
    else next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (to.meta.role) {
    const { useUserStore } = await import('../stores/user')
    const userStore = useUserStore()

    if (!userStore.role || !sessionRestored) {
      try {
        await userStore.restoreSession()
        sessionRestored = true
      } catch {
        next('/login')
        return
      }
    }

    if (to.meta.role !== userStore.role) {
      next(`/${userStore.role}`)
      return
    }
  }

  next()
})

export default router
