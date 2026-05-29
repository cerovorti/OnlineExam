package com.exam.controller;

import com.exam.common.Result;
import com.exam.entity.*;
import com.exam.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentController extends BaseController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private StudentAnswerService studentAnswerService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudentService studentService;

    private Long getStudentId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) return null;
        Student student = studentService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, userId)
        );
        return student != null ? student.getId() : null;
    }

    @GetMapping("/exams")
    public Result<List<Map<String, Object>>> getExams(HttpServletRequest request) {
        Long studentId = getStudentId(request);

        Set<Long> seenIds = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();

        List<Exam> publishedExams = examService.getStudentExams(studentId, "published");
        for (Exam exam : publishedExams) {
            seenIds.add(exam.getId());
            Map<String, Object> map = buildExamMap(exam);
            ExamRecord record = examRecordService.getByExamAndStudent(exam.getId(), studentId);
            map.put("recordStatus", record != null ? record.getStatus() : null);
            result.add(map);
        }

        List<ExamRecord> inProgressRecords = examRecordService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .in(ExamRecord::getStatus, "in_progress", "not_started")
        );

        for (ExamRecord record : inProgressRecords) {
            if (!seenIds.contains(record.getExamId())) {
                Exam exam = examService.getById(record.getExamId());
                if (exam != null) {
                    Map<String, Object> map = buildExamMap(exam);
                    map.put("recordStatus", record.getStatus());
                    result.add(map);
                }
            }
        }

        return Result.success(result);
    }

    private Map<String, Object> buildExamMap(Exam exam) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", exam.getId());
        map.put("name", exam.getName());
        map.put("paperId", exam.getPaperId());
        map.put("creatorId", exam.getCreatorId());
        map.put("startTime", exam.getStartTime());
        map.put("endTime", exam.getEndTime());
        map.put("status", exam.getStatus());
        map.put("createdAt", exam.getCreatedAt());
        map.put("updatedAt", exam.getUpdatedAt());

        Paper paper = paperService.getById(exam.getPaperId());
        map.put("duration", paper != null ? paper.getDuration() : null);
        map.put("passScore", paper != null ? paper.getPassScore() : null);
        return map;
    }

    @GetMapping("/exams/{id}")
    public Result<Map<String, Object>> getExamDetail(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = getStudentId(request);
        Exam exam = examService.getById(id);
        if (exam == null) {
            return Result.error("考试不存在");
        }

        ExamRecord record = examRecordService.getByExamAndStudent(id, studentId);

        Map<String, Object> detail = new HashMap<>();
        detail.put("exam", exam);
        detail.put("record", record);
        return Result.success(detail);
    }

    @PostMapping("/exams/{id}/start")
    public Result<Map<String, Object>> startExam(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = getStudentId(request);
        Exam exam = examService.getById(id);
        if (exam == null) {
            return Result.error("考试不存在");
        }

        ExamRecord record = examRecordService.startExam(id, studentId, exam.getPaperId());
        if (record == null) {
            return Result.error("您已交卷或考试已结束");
        }

        List<Question> questions = examService.getExamQuestions(id, studentId);

        List<StudentAnswer> existingAnswers = studentAnswerService.getAnswersByRecordId(record.getId());
        List<Map<String, Object>> answerList = new ArrayList<>();
        for (StudentAnswer sa : existingAnswers) {
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", sa.getQuestionId());
            item.put("answer", sa.getAnswer());
            answerList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("exam", exam);
        result.put("questions", questions);
        result.put("answers", answerList);

        Paper paper = paperService.getById(exam.getPaperId());
        int totalDuration = 120;
        if (paper != null) {
            totalDuration = paper.getDuration() != null ? paper.getDuration() : 120;
            result.put("passScore", paper.getPassScore());
        }

        long remainingSeconds = totalDuration * 60L;
        if (record.getStartTime() != null) {
            long elapsed = Duration.between(record.getStartTime(), LocalDateTime.now()).getSeconds();
            remainingSeconds = Math.max(0, totalDuration * 60L - elapsed);
        }
        result.put("duration", totalDuration);
        result.put("remainingSeconds", remainingSeconds);

        return Result.success(result);
    }

    @PostMapping("/exams/{id}/submit")
    public Result<Map<String, Object>> submitExam(@PathVariable Long id, @RequestBody Map<String, Object> data,
                                                   HttpServletRequest request) {
        Long studentId = getStudentId(request);
        Object typeObj = data.getOrDefault("type", "manual");
        String submitType = typeObj instanceof String ? (String) typeObj : String.valueOf(typeObj);
        Integer cutScreenCount = data.get("cutScreenCount") != null
                ? Integer.parseInt(data.get("cutScreenCount").toString()) : 0;

        ExamRecord record = examService.submitExam(id, studentId, submitType, cutScreenCount);
        if (record == null) {
            return Result.error("提交失败");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("message", "交卷成功");
        return Result.success(result);
    }

    @PostMapping("/exams/{id}/save-answer")
    public Result<Void> saveAnswer(@PathVariable Long id, @RequestBody StudentAnswer answer,
                                    HttpServletRequest request) {
        Long studentId = getStudentId(request);
        ExamRecord record = examRecordService.getByExamAndStudent(id, studentId);
        if (record == null) {
            return Result.error("考试记录不存在");
        }

        answer.setExamRecordId(record.getId());
        studentAnswerService.saveOrUpdateAnswer(answer);
        return Result.success();
    }

    @GetMapping("/records")
    public Result<List<Map<String, Object>>> getExamRecords(HttpServletRequest request) {
        Long studentId = getStudentId(request);

        List<ExamRecord> records = examRecordService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .orderByDesc(ExamRecord::getCreatedAt)
        );

        List<Map<String, Object>> result = records.stream().map(record -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("examId", record.getExamId());
            item.put("studentId", record.getStudentId());
            item.put("paperId", record.getPaperId());
            item.put("startTime", record.getStartTime());
            item.put("submitTime", record.getSubmitTime());
            item.put("status", record.getStatus());
            item.put("score", record.getScore());
            item.put("passed", record.getPassed());
            item.put("submitType", record.getSubmitType());
            item.put("cutScreenCount", record.getCutScreenCount());

            Exam exam = examService.getById(record.getExamId());
            if (exam != null) {
                item.put("examName", exam.getName());
            }

            return item;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/records/{id}")
    public Result<Map<String, Object>> getRecordDetail(@PathVariable Long id) {
        ExamRecord record = examRecordService.getById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }

        List<StudentAnswer> answers = studentAnswerService.getAnswersByRecordId(id);
        List<Question> questions = paperService.getPaperQuestions(record.getPaperId());

        Map<String, Object> detail = new HashMap<>();
        detail.put("record", record);

        List<Map<String, Object>> answerDetails = new ArrayList<>();
        for (StudentAnswer answer : answers) {
            Map<String, Object> item = new HashMap<>();
            item.put("answer", answer);
            Question question = questionService.getById(answer.getQuestionId());
            if (question != null) {
                item.put("questionTitle", question.getTitle());
                item.put("questionType", question.getType());
                item.put("correctAnswer", question.getAnswer());
                item.put("analysis", question.getAnalysis());
                item.put("options", question.getOptions());
            }
            answerDetails.add(item);
        }
        detail.put("answers", answerDetails);
        detail.put("questions", questions);

        return Result.success(detail);
    }

    @GetMapping("/wrong-questions")
    public Result<List<Map<String, Object>>> getWrongQuestions(
            @RequestParam(required = false) Long subjectId,
            HttpServletRequest request) {
        Long studentId = getStudentId(request);
        List<WrongQuestion> wrongQuestions = wrongQuestionService.getByStudentId(studentId, subjectId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WrongQuestion wq : wrongQuestions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", wq.getId());
            item.put("studentId", wq.getStudentId());
            item.put("questionId", wq.getQuestionId());
            item.put("examRecordId", wq.getExamRecordId());
            item.put("myAnswer", wq.getMyAnswer());
            item.put("correctAnswer", wq.getCorrectAnswer());
            item.put("wrongCount", wq.getWrongCount());
            item.put("mastered", wq.getMastered());
            item.put("createdAt", wq.getCreatedAt());

            Question question = questionService.getById(wq.getQuestionId());
            if (question != null) {
                item.put("questionTitle", question.getTitle());
                item.put("questionType", question.getType());
                item.put("analysis", question.getAnalysis());
                item.put("difficulty", question.getDifficulty());
                item.put("knowledgePoints", question.getKnowledgePoints());
                item.put("options", question.getOptions());
                item.put("subjectId", question.getSubjectId());

                if (question.getSubjectId() != null) {
                    Subject subject = subjectService.getById(question.getSubjectId());
                    item.put("subjectName", subject != null ? subject.getName() : "");
                }
            }

            result.add(item);
        }

        return Result.success(result);
    }

    @PostMapping("/wrong-questions/{id}/master")
    public Result<Void> markAsMastered(@PathVariable Long id) {
        WrongQuestion wrongQuestion = wrongQuestionService.getById(id);
        if (wrongQuestion == null) {
            return Result.error("错题不存在");
        }
        wrongQuestion.setMastered(1);
        wrongQuestionService.updateById(wrongQuestion);
        return Result.success();
    }

    @GetMapping("/analysis")
    public Result<Map<String, Object>> getScoreAnalysis(HttpServletRequest request) {
        Long studentId = getStudentId(request);

        List<ExamRecord> records = examRecordService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getStatus, "submitted")
                        .orderByAsc(ExamRecord::getCreatedAt)
        );

        Map<String, Object> analysis = new HashMap();
        analysis.put("totalExams", records.size());

        if (records.isEmpty()) {
            return Result.success(analysis);
        }

        double avgScore = records.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(r -> r.getScore().doubleValue())
                .average().orElse(0);
        analysis.put("averageScore", Math.round(avgScore * 100.0) / 100.0);

        long passCount = records.stream().filter(r -> r.getPassed() != null && r.getPassed() == 1).count();
        analysis.put("passCount", passCount);
        analysis.put("failCount", records.size() - passCount);
        analysis.put("passRate", records.size() > 0
                ? Math.round((double) passCount / records.size() * 1000.0) / 10.0 : 0);

        List<Map<String, Object>> trend = new ArrayList<>();
        for (ExamRecord record : records) {
            Map<String, Object> point = new HashMap<>();
            Exam exam = examService.getById(record.getExamId());
            Paper paper = exam != null ? paperService.getById(exam.getPaperId()) : null;
            point.put("examName", exam != null ? exam.getName() : "未知");
            point.put("score", record.getScore());
            point.put("passScore", paper != null ? paper.getPassScore() : null);
            point.put("date", record.getSubmitTime());
            trend.add(point);
        }
        analysis.put("scoreTrend", trend);

        return Result.success(analysis);
    }
}