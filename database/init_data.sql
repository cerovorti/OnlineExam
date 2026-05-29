-- ==============================
-- 在线考试系统初始数据（基础数据）
-- 用户/教师/学生/试卷/考试等完整数据由 DataInitializer 组件在首次启动时自动创建
-- 文件编码: UTF-8
-- ==============================

-- 1. 院系数据
INSERT IGNORE INTO departments (id, name, parent_id, description) VALUES
(1, '计算机科学与技术学院', NULL, '计算机科学与技术相关专业'),
(2, '软件工程系', 1, '软件工程相关专业'),
(3, '网络工程系', 1, '网络工程相关专业');

-- 2. 班级数据
INSERT IGNORE INTO classes (id, class_name, department_id, major, grade, student_count) VALUES
(1, '软件工程2023-1班', 2, '软件工程', '2023级', 40),
(2, '计算机科学2023-1班', 1, '计算机科学与技术', '2023级', 35),
(3, '网络工程2023-B班', 3, '网络工程', '2023级', 30);

-- 3. 科目数据
INSERT IGNORE INTO subjects (name, code, description) VALUES
('Java程序设计', 'CS101', 'Java编程语言基础与面向对象编程'),
('数据结构', 'CS102', '数据结构与算法'),
('数据库原理', 'CS103', '数据库系统原理与应用'),
('计算机网络', 'CS104', '计算机网络基础'),
('操作系统', 'CS105', '操作系统原理');

-- 4. 题库数据
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

-- 5. 系统日志 (示例)
INSERT IGNORE INTO system_logs (user_id, username, role, action, description, ip) VALUES
(1, 'admin', 'admin', '导入学生数据', '导入学生数据120条', '192.168.1.100'),
(2, 'teacher001', 'teacher', '发布考试', '发布考试Java期末考试', '192.168.1.101');