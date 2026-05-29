package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.*;
import com.exam.mapper.ExamMapper;
import com.exam.service.*;
import com.exam.util.ShuffleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {

    @Autowired
    private ExamClassService examClassService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private StudentAnswerService studentAnswerService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private ExamExtensionService examExtensionService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ShuffleUtil shuffleUtil;

    @Override
    public List<Exam> getStudentExams(Long studentId, String status) {
        Student student = studentService.getById(studentId);
        if (student == null) {
            return new ArrayList<>();
        }

        List<ExamClass> examClasses = examClassService.list(
                new LambdaQueryWrapper<ExamClass>().eq(ExamClass::getClassId, student.getClassId())
        );

        if (examClasses.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> examIds = examClasses.stream()
                .map(ExamClass::getExamId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Exam::getId, examIds);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Exam::getStatus, status);
        }

        wrapper.orderByDesc(Exam::getCreatedAt);

        return this.list(wrapper);
    }

    @Override
    public List<Question> getExamQuestions(Long examId, Long studentId) {
        Exam exam = this.getById(examId);
        if (exam == null) {
            return new ArrayList<>();
        }

        List<PaperQuestion> paperQuestions = paperQuestionService.list(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, exam.getPaperId())
                        .orderByAsc(PaperQuestion::getSortOrder)
        );

        List<Long> questionIds = paperQuestions.stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toList());

        if (questionIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Question> questions = questionService.listByIds(questionIds);

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<Question> orderedQuestions = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            Question q = questionMap.get(pq.getQuestionId());
            if (q != null) {
                q.setScore(pq.getScore());
                q.setAnalysis(null);
                q.setKnowledgePoints(null);
                orderedQuestions.add(q);
            }
        }

        if (exam.getShuffleQuestions() != null && exam.getShuffleQuestions() == 1) {
            orderedQuestions = shuffleUtil.shuffleQuestions(orderedQuestions);
        }

        if (exam.getShuffleOptions() != null && exam.getShuffleOptions() == 1) {
            orderedQuestions = orderedQuestions.stream()
                    .map(q -> {
                        Question shuffled = new Question();
                        shuffled.setId(q.getId());
                        shuffled.setTitle(q.getTitle());
                        shuffled.setType(q.getType());
                        shuffled.setOptions(shuffleUtil.shuffleOptions(q.getOptions()));
                        shuffled.setAnswer(q.getAnswer());
                        shuffled.setAnalysis(q.getAnalysis());
                        shuffled.setDifficulty(q.getDifficulty());
                        shuffled.setKnowledgePoints(q.getKnowledgePoints());
                        shuffled.setScore(q.getScore());
                        return shuffled;
                    })
                    .collect(Collectors.toList());
        }

        return orderedQuestions;
    }

    @Override
    @Transactional
    public ExamRecord submitExam(Long examId, Long studentId, String submitType, Integer cutScreenCount) {
        ExamRecord record = examRecordService.getByExamAndStudent(examId, studentId);
        if (record == null) return null;

        record.setSubmitTime(LocalDateTime.now());
        record.setStatus("submitted");
        record.setSubmitType(submitType != null ? submitType : "manual");
        if (cutScreenCount != null) {
            record.setCutScreenCount(cutScreenCount);
        }
        examRecordService.updateById(record);

        studentAnswerService.autoGradeForRecord(record.getId());

        Student student = studentService.getById(studentId);
        collectWrongQuestions(record.getId(), studentId);

        List<StudentAnswer> answers = studentAnswerService.getAnswersByRecordId(record.getId());
        double totalScore = answers.stream()
                .filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1)
                .mapToDouble(a -> a.getScore() != null ? a.getScore().doubleValue() : 0)
                .sum();

        Exam exam = this.getById(examId);
        Paper paper = paperService.getById(exam.getPaperId());

        record.setScore(BigDecimal.valueOf(totalScore));
        if (paper != null && paper.getPassScore() != null) {
            record.setPassed(totalScore >= paper.getPassScore() ? 1 : 0);
        }
        examRecordService.updateById(record);

        return record;
    }

    private void collectWrongQuestions(Long examRecordId, Long studentId) {
        List<StudentAnswer> answers = studentAnswerService.getAnswersByRecordId(examRecordId);
        for (StudentAnswer answer : answers) {
            if (answer.getIsCorrect() == null || answer.getIsCorrect() == 0) {
                Question question = questionService.getById(answer.getQuestionId());
                if (question != null) {
                    wrongQuestionService.collectWrongAnswer(
                            studentId,
                            answer.getQuestionId(),
                            examRecordId,
                            answer.getAnswer(),
                            question.getAnswer()
                    );
                }
            }
        }
    }

    @Override
    public Map<String, Object> getExamAnalysis(Long examId) {
        Map<String, Object> analysis = new HashMap<>();

        List<ExamRecord> records = examRecordService.getByExamId(examId);
        List<ExamRecord> submitted = records.stream()
                .filter(r -> r.getScore() != null)
                .collect(Collectors.toList());

        analysis.put("totalCount", records.size());
        analysis.put("submittedCount", submitted.size());

        if (!submitted.isEmpty()) {
            DoubleSummaryStatistics stats = submitted.stream()
                    .mapToDouble(r -> r.getScore() != null ? r.getScore().doubleValue() : 0)
                    .summaryStatistics();

            analysis.put("avgScore", Math.round(stats.getAverage() * 100.0) / 100.0);
            analysis.put("maxScore", stats.getMax());
            analysis.put("minScore", stats.getMin());

            Exam exam = this.getById(examId);
            Paper paper = exam != null ? paperService.getById(exam.getPaperId()) : null;
            analysis.put("passScore", paper != null ? paper.getPassScore() : null);

            long passCount = submitted.stream().filter(r -> r.getPassed() != null && r.getPassed() == 1).count();
            analysis.put("passRate", Math.round((double) passCount / submitted.size() * 1000.0) / 10.0);

            List<Map<String, Object>> recordList = new ArrayList<>();
            for (ExamRecord record : records) {
                Student student = studentService.getById(record.getStudentId());
                Map<String, Object> info = new HashMap<>();
                info.put("recordId", record.getId());
                info.put("studentNo", student != null ? student.getStudentNo() : "");
                info.put("studentName", student != null ? student.getStudentName() : "未知");
                info.put("score", record.getScore());
                info.put("submitTime", record.getSubmitTime());
                info.put("submitType", record.getSubmitType());
                info.put("cutScreenCount", record.getCutScreenCount());
                recordList.add(info);
            }
            analysis.put("records", recordList);
        }

        return analysis;
    }
}