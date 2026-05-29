package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.*;
import com.exam.service.*;
import com.exam.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ClassInfoService classInfoService;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRecordService examRecordService;

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userService.count();
        long totalStudents = studentService.count();
        long totalTeachers = teacherService.count();
        long totalExams = examService.count();
        long totalParticipations = examRecordService.count();

        stats.put("totalUsers", (int) totalUsers);
        stats.put("totalStudents", (int) totalStudents);
        stats.put("totalTeachers", (int) totalTeachers);
        stats.put("totalExams", (int) totalExams);
        stats.put("totalParticipations", (int) totalParticipations);

        List<Exam> publishedExams = examService.list(
                new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, "published")
        );
        stats.put("activeExams", publishedExams.size());

        return Result.success(stats);
    }

    @GetMapping("/dashboard/trends")
    public Result<List<Map<String, Object>>> getExamTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = examService.count(new LambdaQueryWrapper<Exam>()
                    .ge(Exam::getCreatedAt, date.atStartOfDay())
                    .lt(Exam::getCreatedAt, date.plusDays(1).atStartOfDay())
            );

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(fmt));
            item.put("count", (int) count);
            trends.add(item);
        }

        return Result.success(trends);
    }

    @GetMapping("/students")
    public Result<Page<Student>> getStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Student> pageParam = new Page<>(page, pageSize);
        Page<Student> result = studentService.page(pageParam);
        return Result.success(result);
    }

    @PostMapping("/students")
    public Result<Void> addStudent(@RequestBody Student student) {
        studentService.save(student);

        User existingUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, student.getStudentNo()));
        if (existingUser == null) {
            User user = new User();
            user.setUsername(student.getStudentNo());
            user.setPassword(PasswordUtil.encode("123456"));
            user.setRole("student");
            userService.save(user);

            student.setUserId(user.getId());
            studentService.updateById(student);
        }

        return Result.success();
    }

    @PutMapping("/students/{id}")
    public Result<Void> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        student.setId(id);
        studentService.updateById(student);
        return Result.success();
    }

    @DeleteMapping("/students/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.removeById(id);
        return Result.success();
    }

    @GetMapping("/teachers")
    public Result<Page<Map<String, Object>>> getTeachers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Teacher> pageParam = new Page<>(page, pageSize);
        Page<Teacher> teacherPage = teacherService.page(pageParam);

        Page<Map<String, Object>> result = new Page<>(page, pageSize, teacherPage.getTotal());
        List<Map<String, Object>> enrichedList = new ArrayList<>();

        for (Teacher teacher : teacherPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", teacher.getId());
            item.put("teacherNo", teacher.getTeacherNo());
            item.put("title", teacher.getTitle());
            item.put("departmentId", teacher.getDepartmentId());
            item.put("userId", teacher.getUserId());
            item.put("createdAt", teacher.getCreatedAt());

            if (teacher.getDepartmentId() != null) {
                Department dept = departmentService.getById(teacher.getDepartmentId());
                item.put("departmentName", dept != null ? dept.getName() : "");
            } else {
                item.put("departmentName", "");
            }

            if (teacher.getUserId() != null) {
                User user = userService.getById(teacher.getUserId());
                item.put("name", user != null ? user.getName() : "");
            } else {
                item.put("name", "");
            }

            enrichedList.add(item);
        }

        result.setRecords(enrichedList);
        return Result.success(result);
    }

    @PostMapping("/teachers")
    public Result<Void> addTeacher(@RequestBody Teacher teacher) {
        teacherService.save(teacher);

        User existingUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, teacher.getTeacherNo()));
        if (existingUser == null) {
            User user = new User();
            user.setUsername(teacher.getTeacherNo());
            user.setPassword(PasswordUtil.encode("123456"));
            user.setRole("teacher");
            userService.save(user);

            teacher.setUserId(user.getId());
            teacherService.updateById(teacher);
        }

        return Result.success();
    }

    @PutMapping("/teachers/{id}")
    public Result<Void> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacher) {
        teacher.setId(id);
        teacherService.updateById(teacher);
        return Result.success();
    }

    @DeleteMapping("/teachers/{id}")
    public Result<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.removeById(id);
        return Result.success();
    }

    @PostMapping("/teachers/{id}/reset-password")
    public Result<Void> resetTeacherPassword(@PathVariable Long id) {
        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
        }
        User user = userService.getById(teacher.getUserId());
        if (user != null) {
            user.setPassword(PasswordUtil.encode("123456"));
            userService.updateById(user);
        }
        return Result.success();
    }

    @GetMapping("/departments")
    public Result<Page<Department>> getDepartments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Department> pageParam = new Page<>(page, pageSize);
        Page<Department> result = departmentService.page(pageParam);
        return Result.success(result);
    }

    @PostMapping("/departments")
    public Result<Void> addDepartment(@RequestBody Department department) {
        departmentService.save(department);
        return Result.success();
    }

    @PutMapping("/departments/{id}")
    public Result<Void> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        departmentService.updateById(department);
        return Result.success();
    }

    @DeleteMapping("/departments/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.removeById(id);
        return Result.success();
    }

    @GetMapping("/classes")
    public Result<Page<ClassInfo>> getClasses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<ClassInfo> pageParam = new Page<>(page, pageSize);
        Page<ClassInfo> result = classInfoService.page(pageParam);
        return Result.success(result);
    }

    @PostMapping("/classes")
    public Result<Void> addClass(@RequestBody ClassInfo classInfo) {
        classInfoService.save(classInfo);
        return Result.success();
    }

    @PutMapping("/classes/{id}")
    public Result<Void> updateClass(@PathVariable Long id, @RequestBody ClassInfo classInfo) {
        classInfo.setId(id);
        classInfoService.updateById(classInfo);
        return Result.success();
    }

    @DeleteMapping("/classes/{id}")
    public Result<Void> deleteClass(@PathVariable Long id) {
        classInfoService.removeById(id);
        return Result.success();
    }

    @GetMapping("/logs")
    public Result<Page<SystemLog>> getLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String role) {

        Page<SystemLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();

        if (action != null && !action.isEmpty()) {
            wrapper.eq(SystemLog::getAction, action);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(SystemLog::getRole, role);
        }

        wrapper.orderByDesc(SystemLog::getCreatedAt);

        Page<SystemLog> result = systemLogService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping("/students/import")
    public Result<Void> importStudents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        try {
            Workbook wb = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = wb.getSheetAt(0);
            int saved = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String studentNo = getCellValue(row, 0);
                String name = getCellValue(row, 1);
                Long classId = Long.valueOf(getCellValue(row, 2));
                if (studentNo.isEmpty()) continue;

                Student student = new Student();
                student.setStudentNo(studentNo);
                student.setStudentName(name);
                student.setClassId(classId);
                studentService.save(student);

                User existingUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, studentNo));
                if (existingUser == null) {
                    User user = new User();
                    user.setUsername(studentNo);
                    user.setPassword(PasswordUtil.encode("123456"));
                    user.setRole("student");
                    userService.save(user);
                    student.setUserId(user.getId());
                    studentService.updateById(student);
                }
                saved++;
            }
            wb.close();
            return Result.success();
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/students/template")
    public void downloadStudentTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=student_template.csv");
        response.getWriter().write('\uFEFF');
        response.getWriter().write("学号,姓名,班级ID\n");
        response.getWriter().write("2024001,张三,1\n");
        response.getWriter().flush();
    }

    @GetMapping("/logs/export")
    public void exportLogs(@RequestParam(required = false) String action,
                           @RequestParam(required = false) String role,
                           HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        if (action != null && !action.isEmpty()) wrapper.eq(SystemLog::getAction, action);
        if (role != null && !role.isEmpty()) wrapper.eq(SystemLog::getRole, role);
        wrapper.orderByDesc(SystemLog::getCreatedAt);
        List<SystemLog> logs = systemLogService.list(wrapper);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=system_logs.csv");
        response.getWriter().write('\uFEFF');
        response.getWriter().write("ID,用户名,角色,操作,描述,IP,时间\n");
        for (SystemLog log : logs) {
            response.getWriter().write(String.join(",",
                    String.valueOf(log.getId()),
                    log.getUsername() != null ? log.getUsername() : "",
                    log.getRole() != null ? log.getRole() : "",
                    log.getAction() != null ? log.getAction() : "",
                    log.getDescription() != null ? log.getDescription().replace(",", "，") : "",
                    log.getIp() != null ? log.getIp() : "",
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : "") + "\n");
        }
        response.getWriter().flush();
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}