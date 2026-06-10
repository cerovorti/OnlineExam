-- =============================================
-- 在线考试系统 - 数据库完整脚本
-- 包含：建库 + 建表（16张） + 初始测试数据
-- 编码：UTF-8  (MySQL 8.0+)
-- 使用方式：mysql -u root -p < online_exam_system.sql
-- =============================================

-- ----------------------------
-- Part 1: 建库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS online_exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_exam_system;

-- ----------------------------
-- Part 2: 建表（16 张）
-- ----------------------------

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role ENUM('admin', 'teacher', 'student') NOT NULL COMMENT '角色',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 院系表
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '院系ID',
    name VARCHAR(100) NOT NULL COMMENT '院系名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父院系ID',
    description TEXT DEFAULT NULL COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';

-- 3. 班级表
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    department_id BIGINT NOT NULL COMMENT '院系ID',
    major VARCHAR(100) DEFAULT NULL COMMENT '专业',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 4. 学生详细信息表
CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    student_no VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    student_name VARCHAR(50) NOT NULL COMMENT '姓名',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    major VARCHAR(100) DEFAULT NULL COMMENT '专业',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
    enrollment_date DATE DEFAULT NULL COMMENT '入学日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_student_no (student_no),
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生详细信息表';

-- 5. 教师详细信息表
CREATE TABLE IF NOT EXISTS teachers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    teacher_no VARCHAR(20) NOT NULL UNIQUE COMMENT '工号',
    department_id BIGINT NOT NULL COMMENT '院系ID',
    title VARCHAR(50) DEFAULT NULL COMMENT '职称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师详细信息表';

-- 6. 科目表
CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科目ID',
    name VARCHAR(100) NOT NULL COMMENT '科目名称',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '科目代码',
    description TEXT DEFAULT NULL COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';

-- 7. 题库表
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    subject_id BIGINT NOT NULL COMMENT '科目ID',
    type ENUM('single', 'multiple', 'judge', 'fill', 'short_answer', 'programming') NOT NULL COMMENT '题型',
    title TEXT NOT NULL COMMENT '题目内容',
    options TEXT DEFAULT NULL COMMENT '选项（如 A.xxx|B.xxx|C.xxx|D.xxx 格式）',
    answer TEXT NOT NULL COMMENT '正确答案',
    analysis TEXT DEFAULT NULL COMMENT '题目解析',
    difficulty ENUM('easy', 'medium', 'hard') DEFAULT 'medium' COMMENT '难度',
    knowledge_points VARCHAR(255) DEFAULT NULL COMMENT '知识点',
    score INT DEFAULT 1 COMMENT '默认分值',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_subject_id (subject_id),
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 8. 试卷表
CREATE TABLE IF NOT EXISTS papers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '试卷ID',
    name VARCHAR(200) NOT NULL COMMENT '试卷名称',
    subject_id BIGINT NOT NULL COMMENT '科目ID',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    total_score INT NOT NULL COMMENT '试卷总分',
    pass_score INT DEFAULT 60 COMMENT '及格分数',
    duration INT NOT NULL COMMENT '考试时长（分钟）',
    mode ENUM('manual', 'auto') NOT NULL COMMENT '组卷方式',
    config JSON DEFAULT NULL COMMENT '组卷配置（JSON）',
    question_count INT NOT NULL COMMENT '题目数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_subject_id (subject_id),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 9. 试卷题目关联表
CREATE TABLE IF NOT EXISTS paper_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    paper_id BIGINT NOT NULL COMMENT '试卷ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    sort_order INT NOT NULL COMMENT '题目序号',
    score INT NOT NULL COMMENT '该题分值',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_paper_question (paper_id, question_id),
    INDEX idx_paper_id (paper_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- 10. 考试表
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考试ID',
    name VARCHAR(200) NOT NULL COMMENT '考试名称',
    paper_id BIGINT NOT NULL COMMENT '试卷ID',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    notice TEXT DEFAULT NULL COMMENT '考试说明',
    anti_cheat_enabled TINYINT DEFAULT 1 COMMENT '是否启用防作弊',
    max_cut_screen INT DEFAULT 3 COMMENT '最大切屏次数',
    shuffle_questions TINYINT DEFAULT 0 COMMENT '是否题目乱序',
    shuffle_options TINYINT DEFAULT 0 COMMENT '是否选项乱序',
    status ENUM('draft', 'published', 'ended') DEFAULT 'draft' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_paper_id (paper_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 11. 考试班级关联表
CREATE TABLE IF NOT EXISTS exam_classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    UNIQUE KEY uk_exam_class (exam_id, class_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试班级关联表';

-- 12. 考试记录表
CREATE TABLE IF NOT EXISTS exam_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    paper_id BIGINT NOT NULL COMMENT '试卷ID',
    start_time DATETIME DEFAULT NULL COMMENT '开始答题时间',
    submit_time DATETIME DEFAULT NULL COMMENT '交卷时间',
    status ENUM('not_started', 'in_progress', 'submitted', 'timeout', 'auto_submitted') DEFAULT 'not_started' COMMENT '状态',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '得分',
    passed TINYINT DEFAULT NULL COMMENT '是否及格',
    cut_screen_count INT DEFAULT 0 COMMENT '切屏次数',
    submit_type VARCHAR(20) DEFAULT NULL COMMENT '交卷类型（manual/timeout/cutscreen/auto）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_exam_student (exam_id, student_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 13. 学生答案表
CREATE TABLE IF NOT EXISTS student_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答案ID',
    exam_record_id BIGINT NOT NULL COMMENT '考试记录ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer TEXT NOT NULL COMMENT '学生答案',
    is_correct TINYINT DEFAULT NULL COMMENT '是否正确',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '得分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (exam_record_id) REFERENCES exam_records(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_record_question (exam_record_id, question_id),
    INDEX idx_exam_record_id (exam_record_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生答案表';

-- 14. 错题本表
CREATE TABLE IF NOT EXISTS wrong_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '错题ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    exam_record_id BIGINT DEFAULT NULL COMMENT '考试记录ID',
    my_answer TEXT DEFAULT NULL COMMENT '我的答案',
    correct_answer TEXT DEFAULT NULL COMMENT '正确答案',
    wrong_count INT DEFAULT 1 COMMENT '错误次数',
    mastered TINYINT DEFAULT 0 COMMENT '是否已掌握',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_record_id) REFERENCES exam_records(id) ON DELETE SET NULL,
    INDEX idx_student_id (student_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

-- 15. 系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    role VARCHAR(20) DEFAULT NULL COMMENT '角色',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    description TEXT DEFAULT NULL COMMENT '操作描述',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    user_agent TEXT DEFAULT NULL COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 16. 考试延长时间记录表
CREATE TABLE IF NOT EXISTS exam_extensions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '延长时间ID',
    exam_record_id BIGINT NOT NULL COMMENT '考试记录ID',
    extend_minutes INT NOT NULL COMMENT '延长的分钟数',
    reason VARCHAR(255) DEFAULT NULL COMMENT '延长原因',
    operator_id BIGINT NOT NULL COMMENT '操作者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (exam_record_id) REFERENCES exam_records(id) ON DELETE CASCADE,
    FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_exam_record_id (exam_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试延长时间记录表';

-- ----------------------------
-- Part 3: 初始测试数据
-- ----------------------------

-- 院系
INSERT IGNORE INTO departments (id, name, parent_id, description) VALUES
(1, '计算机科学与技术学院', NULL, '计算机科学与技术相关专业'),
(2, '软件工程系', 1, '软件工程相关专业'),
(3, '网络工程系', 1, '网络工程相关专业');

-- 班级
INSERT IGNORE INTO classes (id, class_name, department_id, major, grade, student_count) VALUES
(1, '软件工程2023-1班', 2, '软件工程', '2023级', 40),
(2, '计算机科学2023-1班', 1, '计算机科学与技术', '2023级', 35),
(3, '网络工程2023-B班', 3, '网络工程', '2023级', 30);

-- 科目
INSERT IGNORE INTO subjects (name, code, description) VALUES
('Java程序设计', 'CS101', 'Java编程语言基础与面向对象编程'),
('数据结构', 'CS102', '数据结构与算法'),
('数据库原理', 'CS103', '数据库系统原理与应用'),
('计算机网络', 'CS104', '计算机网络基础'),
('操作系统', 'CS105', '操作系统原理');

-- 题库
INSERT IGNORE INTO questions (subject_id, type, title, options, answer, analysis, difficulty, knowledge_points, score, creator_id) VALUES
(1, 'single', '下列哪个是Java的保留字？', '{"A":"include","B":"define","C":"goto","D":"NULL"}', 'C', 'goto是Java的保留字', 'easy', 'Java基础', 2, 2),
(1, 'single', '下列哪个集合类是线程安全的？', '{"A":"ArrayList","B":"LinkedList","C":"Vector","D":"HashSet"}', 'C', 'Vector是同步的线程安全集合', 'medium', '集合框架', 2, 2),
(1, 'single', 'Java中基本数据类型有哪些？', '{"A":"int, float, boolean","B":"Integer, Float, Boolean","C":"String, Object, Class","D":"List, Set, Map"}', 'A', 'int, float, boolean是基本数据类型', 'easy', 'Java基础', 2, 2),
(1, 'multiple', '以下哪些是面向对象的特性？', '{"A":"封装","B":"继承","C":"多态","D":"抽象"}', 'ABCD', '面向对象四大特性：封装、继承、多态、抽象', 'easy', '面向对象', 3, 2),
(1, 'judge', 'Java支持多重继承。', NULL, '0', 'Java只支持单继承，可通过接口实现多重继承效果', 'easy', '面向对象', 1, 2),
(1, 'short_answer', '简述面向对象的三大特性。', NULL, '封装、继承、多态', '封装隐藏实现细节，继承实现代码复用，多态实现灵活调用', 'medium', '面向对象', 10, 2);

INSERT IGNORE INTO questions (subject_id, type, title, options, answer, analysis, difficulty, knowledge_points, score, creator_id) VALUES
(2, 'single', '栈和队列的共同特点是什么？', '{"A":"只允许在端点处插入和删除元素","B":"都是先进后出","C":"都是先进先出","D":"没有共同点"}', 'A', '栈和队列都只允许在端点处插入和删除元素', 'easy', '线性表', 2, 2),
(2, 'single', '快速排序的时间复杂度是多少？', '{"A":"O(n)","B":"O(nlogn)","C":"O(n^2)","D":"O(logn)"}', 'B', '快速排序平均时间复杂度为O(nlogn)', 'medium', '排序算法', 2, 2),
(2, 'judge', '二叉树中，度为0的节点数等于度为2的节点数加1。', NULL, '1', '对于任意二叉树，n0 = n2 + 1', 'medium', '树', 2, 2);

INSERT IGNORE INTO questions (subject_id, type, title, options, answer, analysis, difficulty, knowledge_points, score, creator_id) VALUES
(3, 'single', 'SQL中查询数据使用哪个关键字？', '{"A":"INSERT","B":"SELECT","C":"UPDATE","D":"DELETE"}', 'B', 'SELECT关键字用于从数据库中查询数据', 'easy', 'SQL基础', 2, 2),
(3, 'multiple', '以下哪些是数据库完整性约束？', '{"A":"主键约束","B":"外键约束","C":"唯一约束","D":"检查约束"}', 'ABCD', '四种都是常见的数据库完整性约束', 'medium', '数据库设计', 3, 2),
(3, 'judge', '数据库的第三范式要求消除传递依赖。', NULL, '1', '第三范式要求非主属性不传递依赖于候选键', 'medium', '数据库范式', 2, 2);
