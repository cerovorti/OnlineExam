package com.exam.aspect;

import com.exam.entity.SystemLog;
import com.exam.service.SystemLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private SystemLogService systemLogService;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void logPointCut() {}

    @AfterReturning(pointcut = "logPointCut()")
    public void afterReturning(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            SystemLog log = new SystemLog();
            log.setUserId(getUserId(request));
            log.setUsername(getUsername(request));
            log.setRole(getRole(request));

            String methodName = joinPoint.getSignature().getName();
            log.setAction(methodName);
            log.setDescription(buildDescription(joinPoint.getSignature().getDeclaringTypeName(), methodName, joinPoint.getArgs()));
            log.setIp(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setCreatedAt(LocalDateTime.now());
            
            systemLogService.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    private String getUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : null;
    }

    private String getRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return role != null ? role.toString() : null;
    }

    private String buildDescription(String className, String methodName, Object[] args) {
        Map<String, String> actionMap = new LinkedHashMap<>();
        actionMap.put("login", "用户登录");
        actionMap.put("getProfile", "获取个人信息");
        actionMap.put("updatePassword", "修改密码");
        actionMap.put("getExams", "查看考试列表");
        actionMap.put("getExamDetail", "查看考试详情");
        actionMap.put("startExam", "开始考试");
        actionMap.put("submitExam", "提交试卷");
        actionMap.put("saveAnswer", "保存答案");
        actionMap.put("getExamRecords", "查看考试记录");
        actionMap.put("getRecordDetail", "查看记录详情");
        actionMap.put("getWrongQuestions", "查看错题本");
        actionMap.put("markAsMastered", "标记错题已掌握");

        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        String desc = actionMap.getOrDefault(methodName, methodName);

        if ("startExam".equals(methodName) && args.length > 0) {
            desc = desc + " [考试ID:" + args[0] + "]";
        } else if ("submitExam".equals(methodName) && args.length > 0) {
            desc = desc + " [考试ID:" + args[0] + "]";
        }

        return simpleName + " - " + desc;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
