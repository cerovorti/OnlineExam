# 在线考试系统

## 项目简介

基于 Vue 3 + Spring Boot 的在线考试系统，包含管理员、教师、学生三个角色的完整功能模块，前后端均已完整实现。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **连接池**: Druid
- **安全**: JWT + BCrypt
- **API 文档**: Swagger / Knife4j
- **构建工具**: Maven
- **Excel 处理**: 自定义 ExcelUtil

### 前端
- **框架**: Vue 3 + Vite
- **UI 组件库**: Element Plus
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **图表**: ECharts + vue-echarts
- **HTTP 客户端**: Axios

## 项目结构

```
online-exam-system/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/exam/
│   │   ├── aspect/            # AOP 切面 (@Log 操作日志)
│   │   ├── common/            # 通用响应类 Result
│   │   ├── config/            # CORS、Swagger、MyBatis-Plus 配置
│   │   ├── controller/        # 9 个控制器，71 个 API 端点
│   │   ├── entity/            # 16 个实体类
│   │   ├── interceptor/       # JWT 请求拦截器
│   │   ├── mapper/            # MyBatis Mapper 接口
│   │   ├── service/           # 业务接口 + impl 实现
│   │   └── util/              # JWT、Excel、密码加密、题目乱序工具
│   ├── src/main/resources/
│   │   └── application.yml    # 后端配置文件
│   └── pom.xml
├── vue/                        # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/               # 统一 API 请求层 (62 个接口)
│   │   ├── layouts/           # MainLayout 主布局
│   │   ├── router/            # 路由配置 + 角色守卫
│   │   ├── stores/            # Pinia 状态管理 (user + exam)
│   │   └── views/
│   │       ├── Login.vue
│   │       ├── admin/         # 管理员: 仪表盘/学生/教师/院系/日志
│   │       ├── teacher/       # 教师: 题库/组卷/考试/监控/分析
│   │       └── student/       # 学生: 考试/记录/错题本/个人中心
│   ├── package.json
│   └── vite.config.js
├── database/
│   ├── schema.sql             # 16 张表建表语句
│   └── init_data.sql          # 初始测试数据
└── README.md
```

---

## 功能模块

### 一、管理员端

| 功能 | 描述 |
|------|------|
| 仪表盘 | 用户数/考试场次/参与人次统计、近7天考试趋势图、最近操作日志 |
| 学生管理 | 增删改查（学号、姓名、班级、专业）、Excel 批量导入、模板下载 |
| 教师管理 | 增删改查（工号、姓名、院系）、密码重置 |
| 院系管理 | 院系和班级组织架构维护 |
| 系统日志 | 按操作类型/角色筛选查询、日志 Excel 导出 |

### 二、教师端

| 功能 | 描述 |
|------|------|
| 题库管理 | 题目增删改查（单选/多选/判断/填空/简答/编程）、Excel 批量导入与模板下载、按科目/知识点/难度筛选 |
| 手动组卷 | 从题库逐题挑选、设置每题分值、实时预览总分 |
| 自动组卷 | 按题型/数量/分值/难度自动随机抽题，一键生成试卷 |
| 试卷预览 | 学生视角预览试卷效果 |
| 发布考试 | 选择试卷 + 指定班级 + 设置起止时间 + 防作弊配置 |
| 考试监控 | 实时查看考生状态/参考人数/异常、手动延长考试时间、异常检测高亮 |
| 成绩分析 | 最高分/最低分/平均分/及格率、分数段分布、成绩 Excel 导出 |
| 试题质量分析 | 每题正确率/错误数/未答数统计，进度条可视化 |
| 班级对比 | 按班级聚合平均分/最高分/最低分/及格率，排名展示 |

### 三、学生端

| 功能 | 描述 |
|------|------|
| 待考考试 | 按时间显示考试列表（名称、时长、起止时间） |
| 在线考试 | 顶部倒计时、左侧题号导航（已答/未答/当前题标记）、自动保存、防切屏提醒、超限自动交卷 |
| 考试记录 | 历史成绩、答卷详情（含正确答案和解析） |
| 错题本 | 自动收录错题、按科目筛选、标记已掌握 |
| 成绩分析 | 各科成绩趋势图、知识点掌握度雷达图 |
| 个人信息 | 查看资料、修改密码 |

### 四、亮点功能

| 功能 | 描述 |
|------|------|
| 防切屏监测 | 切屏次数超限自动交卷，监控页异常高亮 |
| 题目/选项乱序 | 不同考生看到不同的题目和选项顺序 |
| 数据实时保存 | 每题作答立即保存，防止断网断电丢数据 |
| 班级对比 | 各班级平均分/及格率横向对比，排名展示 |
| 试题质量分析 | 每题正确率/错误数统计，识别偏难/偏易题目 |
| 知识点掌握度分析 | 按知识点聚合正确率，雷达图可视化 |
| 批量导入/导出 | 学生/题目 Excel 导入，成绩/日志 Excel 导出 |
| AOP 操作日志 | @Log 注解自动记录关键操作，可按操作类型/角色追溯 |

---

## 数据库设计

### 16 张核心表

| 序号 | 表名 | 说明 |
|:---:|------|------|
| 1 | `users` | 用户表（统一登录账号） |
| 2 | `students` | 学生详细信息（学号、姓名、班级、专业） |
| 3 | `teachers` | 教师详细信息（工号、姓名、院系、职称） |
| 4 | `departments` | 院系表 |
| 5 | `classes` | 班级表 |
| 6 | `subjects` | 科目表 |
| 7 | `questions` | 题库表（6种题型、难度、知识点） |
| 8 | `papers` | 试卷表（时长、总分、及格线） |
| 9 | `paper_questions` | 试卷-题目关联表（支持乱序标记） |
| 10 | `exams` | 考试表（时间、乱序配置） |
| 11 | `exam_classes` | 考试-班级关联表 |
| 12 | `exam_records` | 考试记录表（得分、状态、切屏次数） |
| 13 | `student_answers` | 学生答案表（每题答案和得分） |
| 14 | `wrong_questions` | 错题本表（自动收录、标记掌握） |
| 15 | `system_logs` | 系统操作日志表 |
| 16 | `exam_extensions` | 考试延长时间记录表 |

---

## 快速启动

### 1. 数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS online_exam_system DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p online_exam_system < database/schema.sql
mysql -u root -p online_exam_system < database/init_data.sql
```

### 2. 后端启动

编辑 `backend/src/main/resources/application.yml` 修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_exam_system?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

```bash
cd backend
mvn spring-boot:run
```

后端默认端口 **8080**，启动后访问 Swagger 文档：http://localhost:8080/doc.html

### 3. 前端启动

```bash
cd vue
npm install
npm run dev
```

前端默认端口 **5173**，访问 http://localhost:5173

### 4. 前端构建

```bash
cd vue
npm run build
```

构建产物在 `vue/dist/` 目录，可直接部署到 Nginx。

---

## 默认测试账号

| 角色 | 账号 | 密码 | 说明 |
|:---:|------|------|------|
| 管理员 | `admin` | `123456` | 拥有全部权限 |
| 教师 | `teacher001` | `123456` | 默认归属计算机学院 |
| 学生 | `student001` | `123456` | 默认归属计算机科学2024级1班 |

密码使用 BCrypt 加密存储，新建账号默认密码均为 `123456`。

---

## API 接口一览

### 认证
- `POST /api/auth/login` - 登录
- `POST /api/auth/logout` - 登出

### 用户
- `GET /api/user/profile` - 获取个人信息（含角色详情）
- `PUT /api/user/password` - 修改密码

### 管理员 (24 个端点)
- 仪表盘统计、考试趋势、学生/教师/院系/班级 CRUD、批量导入/导出、日志查询/导出

### 教师 (17 个端点)
- 题库 CRUD/导入/模板、试卷 CRUD/预览、手动/自动组卷、考试发布/监控/延长、班级分配、成绩分析/导出、试题质量、班级对比

### 学生 (11 个端点)
- 考试列表/详情/开始/交卷/保存答案、考试记录/详情、错题本/标记掌握、成绩分析

### 科目 (5 个端点)
- 科目 CRUD

详见 [vue/src/api/index.js](vue/src/api/index.js) 和各个 Controller 的 `@RequestMapping` 注解。

---

## 安全与认证

- **JWT 认证**: 登录后签发 Token，请求头携带 `Authorization: Bearer <token>`
- **角色权限**: 路由守卫 + 后端拦截器双重校验，管理员/教师/学生权限隔离
- **密码加密**: BCrypt 加密存储，明文密码不入库
- **接口保护**: 所有 `/api/**` 请求经 JWT 拦截器校验（登录接口除外）

## 注意事项

- 后端跨域已通过 CorsConfig 全局配置，支持前端 localhost:5173 访问
- 前端开发时 `vite.config.js` 已配置代理 `/api` → `http://localhost:8080`
- 系统日志切面已就绪，可在关键接口添加 `@Log` 注解自动记录操作
- 题目导入模板支持 6 种题型的示例数据