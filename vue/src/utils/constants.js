export const QUESTION_TYPE_MAP = {
  single: '单选题',
  multiple: '多选题',
  judge: '判断题',
  fill: '填空题',
  short_answer: '简答题',
  programming: '编程题'
}

export const DIFFICULTY_MAP = {
  easy: '简单',
  medium: '中等',
  hard: '困难'
}

export const DIFFICULTY_TAG_MAP = {
  easy: 'success',
  medium: 'warning',
  hard: 'danger'
}

export const QUESTION_TAG_MAP = {
  single: '',
  multiple: 'success',
  judge: 'warning',
  fill: 'info',
  short_answer: 'danger',
  programming: ''
}

export const ROLE_MAP = {
  admin: '管理员',
  teacher: '教师',
  student: '学生'
}

export const ROLE_TAG_MAP = {
  admin: 'danger',
  teacher: 'warning',
  student: 'primary'
}

export const questionTypeName = (type) => QUESTION_TYPE_MAP[type] || type
export const questionTagType = (type) => QUESTION_TAG_MAP[type] || ''
export const difficultyName = (level) => DIFFICULTY_MAP[level] || level
export const difficultyTagType = (level) => DIFFICULTY_TAG_MAP[level] || ''
export const roleName = (role) => ROLE_MAP[role] || role
export const roleTagType = (role) => ROLE_TAG_MAP[role] || ''