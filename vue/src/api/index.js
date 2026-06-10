// API服务层 - 用于前后端通信
import axios from 'axios'

const request = axios.create({
  baseURL: '/api', // 后端API基础路径
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const body = response.data
    if (body.code && body.code !== 200) {
      const error = new Error(body.message || '请求失败')
      error.data = body
      return Promise.reject(error)
    }
    return body
  },
  error => {
    const msg = error?.response?.data?.message || error?.data?.message || error.message
    console.error('API Error:', msg)
    return Promise.reject(error)
  }
)

// 用户相关API
export const userApi = {
  login(data) {
    return request.post('/auth/login', data)
  },
  logout() {
    return request.post('/auth/logout')
  },
  updatePassword(data) {
    return request.put('/user/password', data)
  },
  getProfile() {
    return request.get('/user/profile')
  }
}

// 管理员API
export const adminApi = {
  // 学生管理
  getStudents(params) {
    return request.get('/admin/students', { params })
  },
  addStudent(data) {
    return request.post('/admin/students', data)
  },
  updateStudent(id, data) {
    return request.put(`/admin/students/${id}`, data)
  },
  deleteStudent(id) {
    return request.delete(`/admin/students/${id}`)
  },
  importStudents(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/admin/students/import', formData)
  },
  downloadTemplate() {
    return request.get('/admin/students/template', { responseType: 'blob' })
  },

  // 教师管理
  getTeachers(params) {
    return request.get('/admin/teachers', { params })
  },
  addTeacher(data) {
    return request.post('/admin/teachers', data)
  },
  updateTeacher(id, data) {
    return request.put(`/admin/teachers/${id}`, data)
  },
  deleteTeacher(id) {
    return request.delete(`/admin/teachers/${id}`)
  },
  resetTeacherPassword(id) {
    return request.post(`/admin/teachers/${id}/reset-password`)
  },

  // 院系管理
  getDepartments() {
    return request.get('/admin/departments')
  },
  addDepartment(data) {
    return request.post('/admin/departments', data)
  },
  updateDepartment(id, data) {
    return request.put(`/admin/departments/${id}`, data)
  },
  deleteDepartment(id) {
    return request.delete(`/admin/departments/${id}`)
  },

  // 班级管理
  getClasses(params) {
    return request.get('/admin/classes', { params })
  },
  addClass(data) {
    return request.post('/admin/classes', data)
  },
  updateClass(id, data) {
    return request.put(`/admin/classes/${id}`, data)
  },
  deleteClass(id) {
    return request.delete(`/admin/classes/${id}`)
  },

  // 系统日志
  getLogs(params) {
    return request.get('/admin/logs', { params })
  },
  exportLogs(params) {
    return request.get('/admin/logs/export', { params, responseType: 'blob' })
  },

  // 数据统计
  getDashboardStats() {
    return request.get('/admin/dashboard/stats')
  },
  getExamTrends(params) {
    return request.get('/admin/dashboard/trends', { params })
  }
}

// 教师API
export const teacherApi = {
  // 题库管理
  getQuestions(params) {
    return request.get('/teacher/questions', { params })
  },
  addQuestion(data) {
    return request.post('/teacher/questions', data)
  },
  updateQuestion(id, data) {
    return request.put(`/teacher/questions/${id}`, data)
  },
  deleteQuestion(id) {
    return request.delete(`/teacher/questions/${id}`)
  },
  importQuestions(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/teacher/questions/import', formData)
  },
  downloadQuestionTemplate() {
    return request.get('/teacher/questions/template', { responseType: 'blob' })
  },

  // 试卷管理
  getPapers(params) {
    return request.get('/teacher/papers', { params })
  },
  createPaper(data) {
    return request.post('/teacher/papers', data)
  },
  updatePaper(id, data) {
    return request.put(`/teacher/papers/${id}`, data)
  },
  deletePaper(id) {
    return request.delete(`/teacher/papers/${id}`)
  },
  previewPaper(id) {
    return request.get(`/teacher/papers/${id}/preview`)
  },

  // 考试管理
  getExams(params) {
    return request.get('/teacher/exams', { params })
  },
  createExam(data) {
    return request.post('/teacher/exams', data)
  },
  updateExam(id, data) {
    return request.put(`/teacher/exams/${id}`, data)
  },
  deleteExam(id) {
    return request.delete(`/teacher/exams/${id}`)
  },
  publishExam(id) {
    return request.post(`/teacher/exams/${id}/publish`)
  },

  // 考试监控
  getExamMonitor(examId) {
    return request.get(`/teacher/exams/${examId}/monitor`)
  },
  extendTime(recordId, data) {
    return request.post(`/teacher/exam-records/${recordId}/extend`, data)
  },

  // 手动组卷 - 添加/移除题目
  addQuestionToPaper(paperId, data) {
    return request.post(`/teacher/papers/${paperId}/questions`, data)
  },
  removeQuestionFromPaper(paperId, questionId) {
    return request.delete(`/teacher/papers/${paperId}/questions/${questionId}`)
  },

  // 自动组卷
  autoGeneratePaper(data) {
    return request.post('/teacher/papers/auto-generate', data)
  },

  // 考试班级分配
  addClassToExam(examId, data) {
    return request.post(`/teacher/exams/${examId}/classes`, data)
  },
  removeClassFromExam(examId, classId) {
    return request.delete(`/teacher/exams/${examId}/classes/${classId}`)
  },

  // 成绩分析
  getExamAnalysis(examId) {
    return request.get(`/teacher/exams/${examId}/analysis`)
  },
  getQuestionQuality(examId) {
    return request.get(`/teacher/exams/${examId}/question-quality`)
  },
  getClassComparison(examId) {
    return request.get(`/teacher/exams/${examId}/class-comparison`)
  },
  exportExamScores(examId) {
    return request.post(`/teacher/exams/${examId}/export`, {}, { responseType: 'blob' })
  },

  // 主观题阅卷
  getSubjectiveAnswers(examId) {
    return request.get(`/teacher/exams/${examId}/subjective-answers`)
  },
  gradeSubjectiveAnswer(answerId, data) {
    return request.put(`/teacher/student-answers/${answerId}/grade`, data)
  }
}

// 学生API
export const studentApi = {
  // 考试相关
  getExams() {
    return request.get('/student/exams')
  },
  getExamDetail(examId) {
    return request.get(`/student/exams/${examId}`)
  },
  startExam(examId) {
    return request.post(`/student/exams/${examId}/start`)
  },
  submitExam(examId, data) {
    return request.post(`/student/exams/${examId}/submit`, data)
  },
  saveAnswer(examId, data) {
    return request.post(`/student/exams/${examId}/save-answer`, data)
  },

  // 考试记录
  getExamRecords(params) {
    return request.get('/student/records', { params })
  },
  getRecordDetail(recordId) {
    return request.get(`/student/records/${recordId}`)
  },

  // 错题本
  getWrongQuestions(params) {
    return request.get('/student/wrong-questions', { params })
  },
  markAsMastered(id) {
    return request.post(`/student/wrong-questions/${id}/master`)
  },

  // 成绩分析
  getScoreAnalysis() {
    return request.get('/student/analysis')
  },

  // 个人信息
  updateProfile(data) {
    return request.put('/student/profile', data)
  }
}

// 科目API
export const subjectApi = {
  getSubjects() {
    return request.get('/subjects')
  },
  addSubject(data) {
    return request.post('/subjects', data)
  },
  updateSubject(id, data) {
    return request.put(`/subjects/${id}`, data)
  },
  deleteSubject(id) {
    return request.delete(`/subjects/${id}`)
  }
}

export default request
