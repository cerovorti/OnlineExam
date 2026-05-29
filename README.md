# 在线考试系统

基于 **Vue 3 + Spring Boot 2.7** 的简单在线考试平台，支持管理员、教师、学生三种角色。

---

## 项目结构

```
online-exam-system/
├── database/
│   └── online_exam_system.sql   # 数据库完整脚本（建库 + 16 张表 + 初始测试数据）
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/exam/
│   │   ├── aspect/             # AOP 切面 — 操作日志自动记录
│   │   ├── common/             # 全局异常处理 + 统一响应体 Result
│   │   ├── component/          # DataInitializer — 首次启动自动初始化管理员/教师/学生账号
│   │   ├── config/             # CORS / MyBatis-Plus / Swagger / WebMvc 配置
│   │   ├── controller/         # 10 个控制器（Admin / Auth / Paper / Question / Student / Subject / Teacher / User / File / Base）
│   │   ├── entity/             # 16 个实体类，映射全部数据表
│   │   ├── interceptor/        # JWT 登录拦截器
│   │   ├── mapper/             # MyBatis-Plus Mapper 接口
│   │   ├── service/            # 业务层接口 + impl 实现
│   │   └── util/               # JWT / 密码加密 / 自动组卷 / 题目乱序 工具类
│   ├── src/main/resources/
│   │   └── application.yml     # 数据库连接、服务端口等配置
│   └── pom.xml
├── vue/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/index.js        # 统一 API 请求层（Axios 封装 + 拦截器）
│   │   ├── layouts/            # MainLayout 主布局（侧边导航 + 角色路由）
│   │   ├── router/index.js     # 路由配置 + 角色守卫 + session 恢复
│   │   ├── stores/             # Pinia 状态管理（user / exam）
│   │   ├── utils/              # 常量 / 对话框清理工具
│   │   └── views/
│   │       ├── Login.vue
│   │       ├── admin/          # 仪表盘 / 学生管理 / 教师管理 / 院系班级 / 科目管理 / 系统日志
│   │       ├── teacher/        # 题库 / 手动组卷 / 自动组卷 / 试卷预览 / 发布考试 / 监控 / 阅卷 / 成绩分析
│   │       └── student/        # 待考 / 在线考试 / 考试记录 / 错题本 / 个人信息
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── .gitignore
└── README.md
```

---

## 快速启动

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 18+ |
| MySQL | 8.0+ |

### 1. 导入数据库

用 `mysql` 命令行执行合并后的 SQL 脚本：

```bash
mysql -u root -p < database/online_exam_system.sql
```

执行后会自动：
- 创建数据库 `online_exam_system`
- 创建全部 16 张表
- 导入院系、班级、科目、题库初始测试数据

如需修改数据库连接信息，编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_exam_system?...
    username: root
    password: 123456     # 改成你的 MySQL 密码
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

首次启动时 `DataInitializer` 会自动创建以下内置账号（密码都是 `123456`）：

| 账号 | 角色 | 说明 |
|------|:--:|------|
| `admin` | 管理员 | 系统管理员 |
| `teacher001` | 教师 | 张老师 |
| `teacher002` | 教师 | 李老师 |
| `teacher003` | 教师 | 王教授 |
| `student001` ~ `student004` | 学生 | 4 个学生账号 |

后端启动默认端口：**8080**  
Swagger 文档：http://localhost:8080/doc.html

### 3. 启动前端

```bash
cd vue
npm install
npx vite
```

前端启动默认端口：**3000**，浏览器访问 http://localhost:3000

Vite 已配置 `/api` 代理至 `http://127.0.0.1:8080`，无需额外配置。

---

## 功能模块

### 管理员端

| 功能 | 说明 |
|------|------|
| 仪表盘 | 用户数 / 考试数 / 参与人次 / 活跃考试统计 + 近 7 天考试趋势 |
| 学生管理 | 增删改查（学号、姓名、班级、专业）+ Excel 批量导入 + 模板下载 |
| 教师管理 | 增删改查（工号、院系、职称）+ 密码重置 |
| 院系班级管理 | 院系和班级组织架构维护 |
| 科目管理 | 科目增删改查（名称、编码、描述） |
| 系统日志 | 按操作类型 / 角色筛选 + Excel 导出 |

### 教师端

| 功能 | 说明 |
|------|------|
| 题库管理 | 题目增删改查（单选/多选/判断/填空/简答/编程）+ Excel 批量导入 + 按科目/知识点/难度筛选 |
| 手动组卷 | 从题库逐题挑选，实时预览总分 |
| 自动组卷 | 按题型数量/分值/难度自动随机抽题，一键生成试卷 |
| 试卷预览 | 学生视角预览试卷效果 |
| 发布考试 | 选择试卷 + 指定班级 + 设置起止时间 + 防作弊配置 |
| 考试监控 | 实时查看考生状态/已用时长/切屏次数 + 手动延长考试时间 |
| 主观题阅卷 | 选择考试 → 逐题评分 → 自动重算总分和及格判定 |
| 成绩分析 | 最高分/最低分/平均分/及格率 + 试题质量分析 + 班级对比 + Excel 导出 |

### 学生端

| 功能 | 说明 |
|------|------|
| 待考考试 | 按时间显示考试名称、时长、起止时间（含考试窗口校验） |
| 在线考试 | 顶部倒计时 + 左侧答题卡导航（已答/未答/标记）+ 30 秒自动保存 + 防切屏超限自动交卷 + 刷新恢复 |
| 考试记录 | 历史成绩 + 答卷详情（含正确答案与解析） |
| 错题本 | 自动收录错题 + 按科目筛选 + 标记已掌握 |
| 成绩分析 | 各科成绩趋势折线图 + 各科目平均分雷达图 |
| 个人信息 | 查看资料 + 修改密码 |

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| ORM | MyBatis-Plus 3.5.3 |
| 数据库 | MySQL 8.0 |
| 连接池 | Druid |
| 认证 | JWT + BCrypt |
| 文档 | Swagger / Knife4j |
| 构建 | Maven |
| 前端框架 | Vue 3 (Composition API) |
| UI 库 | Element Plus |
| 路由 | Vue Router 4 |
| 状态管理 | Pinia |
| 图表 | ECharts |
| HTTP | Axios |
| 构建 | Vite 5 |
