package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.*;
import com.exam.service.*;
import com.exam.util.AutoPaperUtil;
import com.exam.util.AutoPaperUtil.QuestionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController extends BaseController {

    @Autowired
    private ExamService examService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private ExamExtensionService examExtensionService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ClassInfoService classInfoService;

    @Autowired
    private AutoPaperUtil autoPaperUtil;

    @Autowired
    private StudentAnswerService studentAnswerService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @GetMapping("/exams")
    public Result<Page<Exam>> getExams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Long teacherId = getCurrentUserId(request);
        Page<Exam> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exam::getCreatorId, teacherId);
        wrapper.orderByDesc(Exam::getCreatedAt);
        Page<Exam> result = examService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping("/exams")
    public Result<Void> createExam(@RequestBody Exam exam, HttpServletRequest request) {
        exam.setCreatorId(getCurrentUserId(request));
        exam.setStatus("draft");
        examService.save(exam);
        return Result.success();
    }

    @PutMapping("/exams/{id}")
    public Result<Void> updateExam(@PathVariable Long id, @RequestBody Exam exam) {
        exam.setId(id);
        examService.updateById(exam);
        return Result.success();
    }

    @DeleteMapping("/exams/{id}")
    public Result<Void> deleteExam(@PathVariable Long id) {
        List<ExamClass> examClasses = examClassService.list(
                new LambdaQueryWrapper<ExamClass>().eq(ExamClass::getExamId, id));
        for (ExamClass ec : examClasses) {
            examClassService.removeById(ec.getId());
        }

        List<ExamRecord> records = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id));
        for (ExamRecord record : records) {
            examExtensionService.remove(
                    new LambdaQueryWrapper<ExamExtension>().eq(ExamExtension::getExamRecordId, record.getId()));
            wrongQuestionService.remove(
                    new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getExamRecordId, record.getId()));
            studentAnswerService.remove(
                    new LambdaQueryWrapper<StudentAnswer>().eq(StudentAnswer::getExamRecordId, record.getId()));
        }
        examRecordService.remove(new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id));

        examService.removeById(id);
        return Result.success();
    }

    @PostMapping("/exams/{id}/publish")
    public Result<Void> publishExam(@PathVariable Long id) {
        Exam exam = examService.getById(id);
        if (exam == null) {
            return Result.error("考试不存在");
        }

        List<ExamClass> examClasses = examClassService.list(
                new LambdaQueryWrapper<ExamClass>().eq(ExamClass::getExamId, id)
        );

        if (examClasses.isEmpty()) {
            return Result.error("请先为考试分配班级");
        }

        for (ExamClass ec : examClasses) {
            ClassInfo classInfo = classInfoService.getById(ec.getClassId());
            if (classInfo == null) continue;

            List<Student> students = studentService.list(
                    new LambdaQueryWrapper<Student>().eq(Student::getClassId, ec.getClassId())
            );

            for (Student student : students) {
                ExamRecord existing = examRecordService.getByExamAndStudent(id, student.getId());
                if (existing == null) {
                    ExamRecord record = new ExamRecord();
                    record.setExamId(id);
                    record.setStudentId(student.getId());
                    record.setPaperId(exam.getPaperId());
                    record.setStatus("not_started");
                    examRecordService.save(record);
                }
            }
        }

        exam.setStatus("published");
        examService.updateById(exam);
        return Result.success();
    }

    @PostMapping("/exam-records/{id}/extend")
    public Result<Void> extendTime(@PathVariable Long id, @RequestBody ExamExtension extension,
                                    HttpServletRequest request) {
        extension.setExamRecordId(id);
        extension.setOperatorId(getCurrentUserId(request));
        examExtensionService.save(extension);
        return Result.success();
    }

    @GetMapping("/exams/{id}/monitor")
    public Result<Map<String, Object>> getExamMonitor(@PathVariable Long id) {
        List<ExamRecord> records = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id)
        );

        List<Map<String, Object>> recordList = new ArrayList<>();
        int submittedCount = 0;
        int totalCount = records.size();

        for (ExamRecord record : records) {
            Student student = studentService.getById(record.getStudentId());
            Map<String, Object> item = new HashMap<>();
            item.put("recordId", record.getId());
            item.put("studentId", record.getStudentId());
            item.put("studentNo", student != null ? student.getStudentNo() : "");
            item.put("studentName", student != null ? student.getStudentName() : "");
            item.put("status", record.getStatus());
            item.put("startTime", record.getStartTime());
            item.put("cutScreenCount", record.getCutScreenCount() != null ? record.getCutScreenCount() : 0);
            item.put("score", record.getScore());

            if (record.getStartTime() != null) {
                long elapsed = java.time.Duration.between(record.getStartTime(), LocalDateTime.now()).toMinutes();
                item.put("elapsedMinutes", elapsed);
            } else {
                item.put("elapsedMinutes", null);
            }

            if ("submitted".equals(record.getStatus()) || "timeout".equals(record.getStatus()) 
                    || "auto_submitted".equals(record.getStatus())) {
                submittedCount++;
            }

            recordList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("submittedCount", submittedCount);
        result.put("records", recordList);
        return Result.success(result);
    }

    @GetMapping("/exams/{id}/analysis")
    public Result<Map<String, Object>> getExamAnalysis(@PathVariable Long id) {
        Map<String, Object> analysis = examService.getExamAnalysis(id);
        return Result.success(analysis);
    }

    @GetMapping("/exams/{id}/class-comparison")
    public Result<List<Map<String, Object>>> getClassComparison(@PathVariable Long id) {
        List<ExamRecord> submittedRecords = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, id)
                        .eq(ExamRecord::getStatus, "submitted")
        );

        Map<Long, List<ExamRecord>> classRecordsMap = new HashMap<>();
        for (ExamRecord record : submittedRecords) {
            Student student = studentService.getById(record.getStudentId());
            if (student != null && student.getClassId() != null) {
                classRecordsMap.computeIfAbsent(student.getClassId(), k -> new ArrayList<>()).add(record);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<ExamRecord>> entry : classRecordsMap.entrySet()) {
            ClassInfo classInfo = classInfoService.getById(entry.getKey());
            List<ExamRecord> records = entry.getValue();

            double avgScore = records.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToDouble(r -> r.getScore().doubleValue())
                    .average().orElse(0);
            avgScore = Math.round(avgScore * 100.0) / 100.0;

            double maxScore = records.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToDouble(r -> r.getScore().doubleValue())
                    .max().orElse(0);

            double minScore = records.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToDouble(r -> r.getScore().doubleValue())
                    .min().orElse(0);

            long passedCount = records.stream()
                    .filter(r -> r.getPassed() != null && r.getPassed() == 1)
                    .count();

            double passRate = records.size() > 0 ? Math.round(passedCount * 10000.0 / records.size()) / 100.0 : 0;

            Map<String, Object> item = new HashMap<>();
            item.put("classId", entry.getKey());
            item.put("className", classInfo != null ? classInfo.getClassName() : "未知班级");
            item.put("studentCount", records.size());
            item.put("avgScore", avgScore);
            item.put("maxScore", maxScore);
            item.put("minScore", minScore);
            item.put("passedCount", (int) passedCount);
            item.put("passRate", passRate);
            result.add(item);
        }

        result.sort((a, b) -> Double.compare(
                (double) b.get("avgScore"),
                (double) a.get("avgScore")
        ));

        return Result.success(result);
    }

    @GetMapping("/exams/{id}/question-quality")
    public Result<List<Map<String, Object>>> getQuestionQuality(@PathVariable Long id) {
        Exam exam = examService.getById(id);
        if (exam == null) return Result.error("考试不存在");

        Paper paper = paperService.getById(exam.getPaperId());
        if (paper == null) return Result.error("试卷不存在");

        List<PaperQuestion> paperQuestions = paperQuestionService.list(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paper.getId())
                        .orderByAsc(PaperQuestion::getSortOrder)
        );

        List<ExamRecord> submittedRecords = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, id)
                        .ne(ExamRecord::getStatus, "not_started")
                        .ne(ExamRecord::getStatus, "in_progress")
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            Question question = questionService.getById(pq.getQuestionId());
            if (question == null) continue;

            int correctCount = 0;
            int totalCount = 0;
            for (ExamRecord record : submittedRecords) {
                StudentAnswer answer = studentAnswerService.getAnswersByRecordId(record.getId())
                        .stream()
                        .filter(a -> a.getQuestionId().equals(question.getId()))
                        .findFirst().orElse(null);

                if (answer != null) {
                    totalCount++;
                    if (answer.getIsCorrect() != null && answer.getIsCorrect() == 1) {
                        correctCount++;
                    }
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("questionId", question.getId());
            item.put("questionType", question.getType());
            item.put("questionTitle", question.getTitle());
            item.put("correctCount", correctCount);
            item.put("totalCount", totalCount);
            item.put("accuracyRate", totalCount > 0 ? correctCount * 100 / totalCount : 0);
            result.add(item);
        }

        return Result.success(result);
    }

    @PostMapping("/exams/{id}/export")
    public void exportExamScores(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Paper paper = null;
        Exam exam = examService.getById(id);
        if (exam != null) {
            paper = paperService.getById(exam.getPaperId());
        }

        List<ExamRecord> records = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id)
        );

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=exam_scores_" + id + ".csv");
        response.getWriter().write('\uFEFF');
        response.getWriter().write("学号,姓名,得分,满分,及格分,是否及格,交卷时间,交卷方式,切屏次数\n");

        for (ExamRecord record : records) {
            Student student = studentService.getById(record.getStudentId());
            String studentNo = student != null ? student.getStudentNo() : "";
            String studentName = student != null ? student.getStudentName() : "";
            String score = record.getScore() != null ? record.getScore().toString() : "";
            String totalScore = paper != null ? String.valueOf(paper.getTotalScore()) : "";
            String passScore = paper != null ? String.valueOf(paper.getPassScore()) : "";
            String passed = record.getPassed() != null ? (record.getPassed() == 1 ? "是" : "否") : "";
            String submitTime = record.getSubmitTime() != null ? record.getSubmitTime().toString() : "";
            String submitType = record.getSubmitType() != null ? record.getSubmitType() : "";
            String cutScreen = record.getCutScreenCount() != null ? String.valueOf(record.getCutScreenCount()) : "0";

            response.getWriter().write(String.join(",",
                    studentNo, studentName, score, totalScore, passScore,
                    passed, submitTime, submitType, cutScreen) + "\n");
        }

        response.getWriter().flush();
    }

    @PostMapping("/papers/{id}/questions")
    public Result<Void> addQuestionToPaper(@PathVariable Long id, @RequestBody PaperQuestion paperQuestion) {
        paperQuestion.setPaperId(id);

        Paper paper = paperService.getById(id);
        PaperQuestion existing = paperQuestionService.getOne(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, id)
                        .eq(PaperQuestion::getQuestionId, paperQuestion.getQuestionId())
        );

        if (existing != null) {
            return Result.error("该题目已在试卷中");
        }

        long count = paperQuestionService.count(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id)
        );
        paperQuestion.setSortOrder((int) count + 1);
        paperQuestionService.save(paperQuestion);

        if (paper != null) {
            paper.setTotalScore(paper.getTotalScore() != null ? paper.getTotalScore() + paperQuestion.getScore() : paperQuestion.getScore());
            paper.setQuestionCount(paper.getQuestionCount() != null ? paper.getQuestionCount() + 1 : 1);
            paperService.updateById(paper);
        }

        return Result.success();
    }

    @DeleteMapping("/papers/{paperId}/questions/{questionId}")
    public Result<Void> removeQuestionFromPaper(@PathVariable Long paperId, @PathVariable Long questionId) {
        PaperQuestion pq = paperQuestionService.getOne(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId)
                        .eq(PaperQuestion::getQuestionId, questionId)
        );

        if (pq != null) {
            Paper paper = paperService.getById(paperId);
            paperQuestionService.removeById(pq.getId());

            if (paper != null) {
                paper.setTotalScore(paper.getTotalScore() - pq.getScore());
                paper.setQuestionCount(Math.max(0, paper.getQuestionCount() - 1));
                paperService.updateById(paper);
            }

            List<PaperQuestion> remaining = paperQuestionService.list(
                    new LambdaQueryWrapper<PaperQuestion>()
                            .eq(PaperQuestion::getPaperId, paperId)
                            .orderByAsc(PaperQuestion::getSortOrder)
            );
            for (int i = 0; i < remaining.size(); i++) {
                PaperQuestion r = remaining.get(i);
                r.setSortOrder(i + 1);
                paperQuestionService.updateById(r);
            }
        }

        return Result.success();
    }

    @PostMapping("/exams/{id}/classes")
    public Result<Void> addClassToExam(@PathVariable Long id, @RequestBody ExamClass examClass) {
        examClass.setExamId(id);
        ExamClass existing = examClassService.getOne(
                new LambdaQueryWrapper<ExamClass>()
                        .eq(ExamClass::getExamId, id)
                        .eq(ExamClass::getClassId, examClass.getClassId())
        );
        if (existing != null) {
            return Result.error("该班级已分配");
        }
        examClassService.save(examClass);
        return Result.success();
    }

    @DeleteMapping("/exams/{examId}/classes/{classId}")
    public Result<Void> removeClassFromExam(@PathVariable Long examId, @PathVariable Long classId) {
        ExamClass ec = examClassService.getOne(
                new LambdaQueryWrapper<ExamClass>()
                        .eq(ExamClass::getExamId, examId)
                        .eq(ExamClass::getClassId, classId)
        );
        if (ec != null) {
            examClassService.removeById(ec.getId());
        }
        return Result.success();
    }

    @PostMapping("/papers/auto-generate")
    public Result<Paper> autoGeneratePaper(@RequestBody Map<String, Object> config, HttpServletRequest request) {
        Long subjectId = Long.valueOf(config.get("subjectId").toString());
        Object nameObj = config.get("name");
        String paperName = nameObj instanceof String ? (String) nameObj : "";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> configList = (List<Map<String, Object>>) config.get("configs");
        List<QuestionConfig> questionConfigs = new ArrayList<>();

        for (Map<String, Object> item : configList) {
            QuestionConfig qc = new QuestionConfig();
            Object itemType = item.get("type");
            qc.setType(itemType instanceof String ? (String) itemType : "");
            qc.setCount(Integer.parseInt(item.get("count").toString()));
            qc.setScore(Integer.parseInt(item.get("score").toString()));
            Object itemDiff = item.get("difficulty");
            qc.setDifficulty(itemDiff instanceof String ? (String) itemDiff : "");
            questionConfigs.add(qc);
        }

        List<Question> selectedQuestions = autoPaperUtil.generatePaperByConfig(subjectId, questionConfigs);

        if (selectedQuestions.isEmpty()) {
            return Result.error("题库中符合条件的题目不足，无法自动组卷");
        }

        Paper paper = new Paper();
        paper.setName(paperName);
        paper.setSubjectId(subjectId);
        paper.setCreatorId(getCurrentUserId(request));
        paper.setDuration(Integer.parseInt(config.getOrDefault("duration", "120").toString()));
        paper.setPassScore(Integer.parseInt(config.getOrDefault("passScore", "60").toString()));
        paper.setMode("auto");

        List<Long> questionIds = selectedQuestions.stream().map(Question::getId).collect(Collectors.toList());
        List<Integer> scores = selectedQuestions.stream().map(Question::getScore).collect(Collectors.toList());

        paper = paperService.createPaperWithQuestions(paper, questionIds, scores);
        return Result.success(paper);
    }

    @GetMapping("/exams/{id}/subjective-answers")
    public Result<List<Map<String, Object>>> getSubjectiveAnswers(@PathVariable Long id) {
        List<ExamRecord> submittedRecords = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, id)
                        .eq(ExamRecord::getStatus, "submitted")
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamRecord record : submittedRecords) {
            Student student = studentService.getById(record.getStudentId());
            List<StudentAnswer> answers = studentAnswerService.getAnswersByRecordId(record.getId());

            for (StudentAnswer answer : answers) {
                Question question = questionService.getById(answer.getQuestionId());
                if (question == null) continue;

                String type = question.getType();
                if (!"short_answer".equals(type) && !"fill".equals(type) && !"programming".equals(type)) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("id", answer.getId());
                item.put("examRecordId", record.getId());
                item.put("questionId", question.getId());
                item.put("questionTitle", question.getTitle());
                item.put("questionType", type);
                item.put("studentAnswer", answer.getAnswer());
                item.put("referenceAnswer", question.getAnswer());
                item.put("isCorrect", answer.getIsCorrect());
                item.put("score", answer.getScore());
                item.put("maxScore", question.getScore());
                item.put("studentNo", student != null ? student.getStudentNo() : "");
                item.put("studentName", student != null ? student.getStudentName() : "");
                result.add(item);
            }
        }

        return Result.success(result);
    }

    @PutMapping("/student-answers/{id}/grade")
    public Result<Void> gradeSubjectiveAnswer(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        StudentAnswer answer = studentAnswerService.getById(id);
        if (answer == null) {
            return Result.error("答案不存在");
        }

        int isCorrect = Integer.parseInt(data.get("isCorrect").toString());
        double score = Double.parseDouble(data.get("score").toString());

        answer.setIsCorrect(isCorrect);
        answer.setScore(BigDecimal.valueOf(score));
        studentAnswerService.updateById(answer);

        ExamRecord record = examRecordService.getById(answer.getExamRecordId());
        if (record != null && "submitted".equals(record.getStatus())) {
            List<StudentAnswer> allAnswers = studentAnswerService.getAnswersByRecordId(record.getId());
            double totalScore = allAnswers.stream()
                    .filter(a -> a.getScore() != null)
                    .mapToDouble(a -> a.getScore().doubleValue())
                    .sum();
            record.setScore(BigDecimal.valueOf(totalScore));

            Exam exam = examService.getById(record.getExamId());
            Paper paper = exam != null ? paperService.getById(exam.getPaperId()) : null;
            if (paper != null && paper.getPassScore() != null) {
                record.setPassed(totalScore >= paper.getPassScore() ? 1 : 0);
            }
            examRecordService.updateById(record);
        }

        return Result.success();
    }
}