package com.exam.component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.entity.*;
import com.exam.service.*;
import com.exam.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ClassInfoService classInfoService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private SystemLogService systemLogService;

    @PostConstruct
    public void init() {
        try {
        if (userService.lambdaQuery().eq(User::getUsername, "admin").count() > 0) {
            return;
        }

        String encodedPassword = PasswordUtil.encode("123456");

        System.out.println("DataInitializer: creating users...");
        User admin = createUser(1L, "admin", encodedPassword, "admin", "系统管理员", "admin@exam.com");
        User teacher1 = createUser(2L, "teacher001", encodedPassword, "teacher", "张老师", "teacher01@exam.com");
        User teacher2 = createUser(3L, "teacher002", encodedPassword, "teacher", "李老师", "teacher02@exam.com");
        User teacher3 = createUser(4L, "teacher003", encodedPassword, "teacher", "王教授", "teacher03@exam.com");
        User student1 = createUser(5L, "student001", encodedPassword, "student", "张三", "student01@exam.com");
        User student2 = createUser(6L, "student002", encodedPassword, "student", "李四", "student02@exam.com");
        User student3 = createUser(7L, "student003", encodedPassword, "student", "王五", "student03@exam.com");
        User student4 = createUser(8L, "student004", encodedPassword, "student", "赵六", "student04@exam.com");

        System.out.println("DataInitializer: creating departments...");
        Department dept1 = createDepartment(1L, "计算机科学与技术学院", null, "计算机科学与技术相关专业");
        Department dept2 = createDepartment(2L, "软件工程系", 1L, "软件工程相关专业");
        Department dept3 = createDepartment(3L, "网络工程系", 1L, "网络工程相关专业");

        System.out.println("DataInitializer: creating classes...");
        ClassInfo class1 = createClass(1L, "软件工程2023-1班", 2L, "软件工程", "2023级", 40);
        ClassInfo class2 = createClass(2L, "计算机科学2023-1班", 1L, "计算机科学与技术", "2023级", 35);
        ClassInfo class3 = createClass(3L, "网络工程2023-B班", 3L, "网络工程", "2023级", 30);

        System.out.println("DataInitializer: creating teachers...");
        createTeacher(2L, "T202401", 2L, "副教授");
        createTeacher(3L, "T202402", 3L, "讲师");
        createTeacher(4L, "T202403", 2L, "教授");

        System.out.println("DataInitializer: creating students...");
        createStudent(5L, "2024001", "张三", 1L, "软件工程", "2023级", LocalDate.of(2023, 9, 1));
        createStudent(6L, "2024002", "李四", 1L, "软件工程", "2023级", LocalDate.of(2023, 9, 1));
        createStudent(7L, "2024003", "王五", 2L, "计算机科学与技术", "2023级", LocalDate.of(2023, 9, 1));
        createStudent(8L, "2024004", "赵六", 1L, "软件工程", "2023级", LocalDate.of(2023, 9, 1));

        System.out.println("DataInitializer: creating subjects...");
        Subject cs101 = createSubject(1L, "Java程序设计", "CS101", "Java编程语言基础与面向对象编程");
        Subject cs102 = createSubject(2L, "数据结构", "CS102", "数据结构与算法");
        Subject cs103 = createSubject(3L, "数据库原理", "CS103", "数据库系统原理与应用");
        Subject cs104 = createSubject(4L, "计算机网络", "CS104", "计算机网络基础");
        Subject cs105 = createSubject(5L, "操作系统", "CS105", "操作系统原理");

        System.out.println("DataInitializer: creating questions...");
        Question q1 = createQuestion(1L, 1L, "single", "下列哪个是Java的保留字？",
                "{\"A\":\"include\",\"B\":\"define\",\"C\":\"goto\",\"D\":\"NULL\"}", "C",
                "goto是Java的保留字", "easy", "Java基础", 2, 2L);
        Question q2 = createQuestion(2L, 1L, "single", "下列哪个集合类是线程安全的？",
                "{\"A\":\"ArrayList\",\"B\":\"LinkedList\",\"C\":\"Vector\",\"D\":\"HashSet\"}", "C",
                "Vector是同步的线程安全集合", "medium", "集合框架", 2, 2L);
        Question q3 = createQuestion(3L, 1L, "single", "Java中基本数据类型有哪些？",
                "{\"A\":\"int, float, boolean\",\"B\":\"Integer, Float, Boolean\",\"C\":\"String, Object, Class\",\"D\":\"List, Set, Map\"}", "A",
                "int, float, boolean是基本数据类型", "easy", "Java基础", 2, 2L);
        Question q4 = createQuestion(4L, 1L, "multiple", "以下哪些是面向对象的特性？",
                "{\"A\":\"封装\",\"B\":\"继承\",\"C\":\"多态\",\"D\":\"抽象\"}", "ABCD",
                "面向对象四大特性：封装、继承、多态、抽象", "easy", "面向对象", 3, 2L);
        Question q5 = createQuestion(5L, 1L, "judge", "Java支持多重继承。",
                null, "0",
                "Java只支持单继承，可通过接口实现多重继承效果", "easy", "面向对象", 1, 2L);
        Question q6 = createQuestion(6L, 1L, "short_answer", "简述面向对象的三大特性。",
                null, "封装、继承、多态",
                "封装隐藏实现细节，继承实现代码复用，多态实现灵活调用", "medium", "面向对象", 10, 2L);
        Question q7 = createQuestion(7L, 2L, "single", "栈和队列的共同特点是什么？",
                "{\"A\":\"只允许在端点处插入和删除元素\",\"B\":\"都是先进后出\",\"C\":\"都是先进先出\",\"D\":\"没有共同点\"}", "A",
                "栈和队列都只允许在端点处插入和删除元素", "easy", "线性表", 2, 2L);
        Question q8 = createQuestion(8L, 2L, "single", "快速排序的时间复杂度是多少？",
                "{\"A\":\"O(n)\",\"B\":\"O(nlogn)\",\"C\":\"O(n^2)\",\"D\":\"O(logn)\"}", "B",
                "快速排序平均时间复杂度为O(nlogn)", "medium", "排序算法", 2, 2L);
        Question q9 = createQuestion(9L, 2L, "judge", "二叉树中，度为0的节点数等于度为2的节点数加1。",
                null, "1",
                "对于任意二叉树，n0 = n2 + 1", "medium", "树", 2, 2L);
        Question q10 = createQuestion(10L, 3L, "single", "SQL中查询数据使用哪个关键字？",
                "{\"A\":\"INSERT\",\"B\":\"SELECT\",\"C\":\"UPDATE\",\"D\":\"DELETE\"}", "B",
                "SELECT关键字用于从数据库中查询数据", "easy", "SQL基础", 2, 2L);
        Question q11 = createQuestion(11L, 3L, "multiple", "以下哪些是数据库完整性约束？",
                "{\"A\":\"主键约束\",\"B\":\"外键约束\",\"C\":\"唯一约束\",\"D\":\"检查约束\"}", "ABCD",
                "四种都是常见的数据库完整性约束", "medium", "数据库设计", 3, 2L);
        Question q12 = createQuestion(12L, 3L, "judge", "数据库的第三范式要求消除传递依赖。",
                null, "1",
                "第三范式要求非主属性不传递依赖于候选键", "medium", "数据库范式", 2, 2L);

        System.out.println("DataInitializer: creating papers...");
        Paper paper1 = createPaper(1L, "Java程序设计期末试卷", 1L, 2L, 20, 12, 120, "manual", null, 6);
        Paper paper2 = createPaper(2L, "数据结构期中测试", 2L, 2L, 6, 3, 60, "manual", null, 3);

        System.out.println("DataInitializer: creating paper questions...");
        createPaperQuestion(1L, 1L, 1, 2);
        createPaperQuestion(1L, 2L, 2, 2);
        createPaperQuestion(1L, 3L, 3, 2);
        createPaperQuestion(1L, 4L, 4, 3);
        createPaperQuestion(1L, 5L, 5, 1);
        createPaperQuestion(1L, 6L, 6, 10);
        createPaperQuestion(2L, 7L, 1, 2);
        createPaperQuestion(2L, 8L, 2, 2);
        createPaperQuestion(2L, 9L, 3, 2);

        System.out.println("DataInitializer: creating exams...");
        createExam(1L, "Java程序设计期末考试", 1L, 2L,
                LocalDateTime.of(2026, 6, 20, 9, 0, 0),
                LocalDateTime.of(2026, 6, 20, 11, 0, 0),
                "请诚信考试，独立完成。切屏超过3次将自动交卷。", 1, 3, 1, 1, "published");
        createExam(2L, "数据结构期中测试", 2L, 3L,
                LocalDateTime.of(2026, 6, 10, 14, 0, 0),
                LocalDateTime.of(2026, 6, 10, 15, 0, 0),
                "请按时参加考试，独立完成。", 1, 3, 0, 0, "published");

        System.out.println("DataInitializer: creating exam classes...");
        createExamClass(1L, 1L);
        createExamClass(2L, 1L);
        createExamClass(2L, 3L);

        System.out.println("DataInitializer: creating system logs...");
        createSystemLog(1L, "admin", "admin", "登录系统", "管理员登录系统后台", "127.0.0.1");
        createSystemLog(2L, "teacher001", "teacher", "发布考试", "发布考试Java期末考试", "127.0.0.1");

        System.out.println("DataInitializer: done!");
        } catch (Exception e) {
            System.err.println("DataInitializer FAILED: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private User createUser(Long id, String username, String password, String role, String name, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setName(name);
        user.setEmail(email);
        user.setStatus(1);
        userService.save(user);
        return user;
    }

    private Department createDepartment(Long id, String name, Long parentId, String description) {
        Department dept = new Department();
        dept.setName(name);
        dept.setParentId(parentId);
        dept.setDescription(description);
        departmentService.save(dept);
        return dept;
    }

    private ClassInfo createClass(Long id, String className, Long departmentId, String major, String grade, Integer studentCount) {
        ClassInfo clazz = new ClassInfo();
        clazz.setClassName(className);
        clazz.setDepartmentId(departmentId);
        clazz.setMajor(major);
        clazz.setGrade(grade);
        clazz.setStudentCount(studentCount);
        classInfoService.save(clazz);
        return clazz;
    }

    private void createTeacher(Long userId, String teacherNo, Long departmentId, String title) {
        Teacher teacher = new Teacher();
        teacher.setUserId(userId);
        teacher.setTeacherNo(teacherNo);
        teacher.setDepartmentId(departmentId);
        teacher.setTitle(title);
        teacherService.save(teacher);
    }

    private void createStudent(Long userId, String studentNo, String studentName, Long classId,
                               String major, String grade, LocalDate enrollmentDate) {
        Student student = new Student();
        student.setUserId(userId);
        student.setStudentNo(studentNo);
        student.setStudentName(studentName);
        student.setClassId(classId);
        student.setMajor(major);
        student.setGrade(grade);
        student.setEnrollmentDate(enrollmentDate);
        studentService.save(student);
    }

    private Subject createSubject(Long id, String name, String code, String description) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setCode(code);
        subject.setDescription(description);
        subjectService.save(subject);
        return subject;
    }

    private Question createQuestion(Long id, Long subjectId, String type, String title,
                                    String options, String answer, String analysis,
                                    String difficulty, String knowledgePoints, Integer score, Long creatorId) {
        Question question = new Question();
        question.setSubjectId(subjectId);
        question.setType(type);
        question.setTitle(title);
        question.setOptions(options);
        question.setAnswer(answer);
        question.setAnalysis(analysis);
        question.setDifficulty(difficulty);
        question.setKnowledgePoints(knowledgePoints);
        question.setScore(score);
        question.setCreatorId(creatorId);
        question.setUsageCount(0);
        questionService.save(question);
        return question;
    }

    private Paper createPaper(Long id, String name, Long subjectId, Long creatorId,
                              Integer totalScore, Integer passScore, Integer duration,
                              String mode, String config, Integer questionCount) {
        Paper paper = new Paper();
        paper.setName(name);
        paper.setSubjectId(subjectId);
        paper.setCreatorId(creatorId);
        paper.setTotalScore(totalScore);
        paper.setPassScore(passScore);
        paper.setDuration(duration);
        paper.setMode(mode);
        paper.setConfig(config);
        paper.setQuestionCount(questionCount);
        paperService.save(paper);
        return paper;
    }

    private void createPaperQuestion(Long paperId, Long questionId, Integer sortOrder, Integer score) {
        PaperQuestion pq = new PaperQuestion();
        pq.setPaperId(paperId);
        pq.setQuestionId(questionId);
        pq.setSortOrder(sortOrder);
        pq.setScore(score);
        paperQuestionService.save(pq);
    }

    private void createExam(Long id, String name, Long paperId, Long creatorId,
                            LocalDateTime startTime, LocalDateTime endTime,
                            String notice, Integer antiCheatEnabled, Integer maxCutScreen,
                            Integer shuffleQuestions, Integer shuffleOptions, String status) {
        Exam exam = new Exam();
        exam.setName(name);
        exam.setPaperId(paperId);
        exam.setCreatorId(creatorId);
        exam.setStartTime(startTime);
        exam.setEndTime(endTime);
        exam.setNotice(notice);
        exam.setAntiCheatEnabled(antiCheatEnabled);
        exam.setMaxCutScreen(maxCutScreen);
        exam.setShuffleQuestions(shuffleQuestions);
        exam.setShuffleOptions(shuffleOptions);
        exam.setStatus(status);
        examService.save(exam);
    }

    private void createExamClass(Long examId, Long classId) {
        ExamClass ec = new ExamClass();
        ec.setExamId(examId);
        ec.setClassId(classId);
        examClassService.save(ec);
    }

    private void createSystemLog(Long userId, String username, String role, String action, String description, String ip) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setRole(role);
        log.setAction(action);
        log.setDescription(description);
        log.setIp(ip);
        systemLogService.save(log);
    }
}