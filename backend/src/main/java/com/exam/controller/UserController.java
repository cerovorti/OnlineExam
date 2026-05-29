package com.exam.controller;

import com.exam.common.Result;
import com.exam.entity.*;
import com.exam.service.*;
import com.exam.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("username", user.getUsername());
        profile.put("name", user.getName());
        profile.put("role", user.getRole());
        profile.put("avatar", user.getAvatar());
        profile.put("phone", user.getPhone());
        profile.put("email", user.getEmail());

        if ("student".equals(user.getRole())) {
            Student student = studentService.lambdaQuery()
                    .eq(Student::getUserId, userId).one();
            if (student != null) {
                profile.put("studentNo", student.getStudentNo());
                profile.put("studentName", student.getStudentName());
                profile.put("major", student.getMajor());
                profile.put("grade", student.getGrade());
            }
        } else if ("teacher".equals(user.getRole())) {
            Teacher teacher = teacherService.lambdaQuery()
                    .eq(Teacher::getUserId, userId).one();
            if (teacher != null) {
                profile.put("teacherNo", teacher.getTeacherNo());
                profile.put("title", teacher.getTitle());
            }
        }

        return Result.success(profile);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody PasswordRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!PasswordUtil.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }

        user.setPassword(PasswordUtil.encode(request.getNewPassword()));
        userService.updateById(user);
        return Result.success();
    }

    @lombok.Data
    public static class PasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}