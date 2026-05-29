package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Question;
import com.exam.entity.StudentAnswer;
import com.exam.mapper.StudentAnswerMapper;
import com.exam.service.QuestionService;
import com.exam.service.StudentAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentAnswerServiceImpl extends ServiceImpl<StudentAnswerMapper, StudentAnswer> implements StudentAnswerService {

    @Autowired
    private QuestionService questionService;

    @Override
    @Transactional
    public StudentAnswer saveOrUpdateAnswer(StudentAnswer answer) {
        LambdaQueryWrapper<StudentAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentAnswer::getExamRecordId, answer.getExamRecordId());
        wrapper.eq(StudentAnswer::getQuestionId, answer.getQuestionId());
        StudentAnswer existing = this.getOne(wrapper);

        if (existing != null) {
            existing.setAnswer(answer.getAnswer());
            this.updateById(existing);
            return existing;
        } else {
            this.save(answer);
            return answer;
        }
    }

    @Override
    public List<StudentAnswer> getAnswersByRecordId(Long examRecordId) {
        LambdaQueryWrapper<StudentAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentAnswer::getExamRecordId, examRecordId);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public void autoGradeForRecord(Long examRecordId) {
        List<StudentAnswer> answers = getAnswersByRecordId(examRecordId);
        double totalScore = 0;

        for (StudentAnswer answer : answers) {
            Question question = questionService.getById(answer.getQuestionId());
            if (question == null) {
                continue;
            }

            boolean isCorrect = gradeAnswer(question, answer.getAnswer());
            double questionScore = isCorrect ? getQuestionScore(answer, question) : 0;
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setScore(BigDecimal.valueOf(questionScore));
            this.updateById(answer);

            totalScore += questionScore;
        }
    }

    private boolean gradeAnswer(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return false;
        }

        String type = question.getType();
        String correctAnswer = question.getAnswer();

        if ("single".equals(type)) {
            return cleanAnswer(studentAnswer).equalsIgnoreCase(cleanAnswer(correctAnswer));
        } else if ("multiple".equals(type)) {
            return compareMultipleChoice(cleanAnswer(studentAnswer), cleanAnswer(correctAnswer));
        } else if ("judge".equals(type)) {
            return cleanAnswer(studentAnswer).equals(cleanAnswer(correctAnswer));
        } else if ("fill".equals(type)) {
            return cleanAnswer(studentAnswer).equalsIgnoreCase(cleanAnswer(correctAnswer));
        }

        return false;
    }

    private boolean compareMultipleChoice(String studentAnswer, String correctAnswer) {
        Set<String> studentSet = new HashSet<>(Arrays.asList(studentAnswer.replaceAll("[^A-Za-z]", "").split("")));
        Set<String> correctSet = new HashSet<>(Arrays.asList(correctAnswer.replaceAll("[^A-Za-z]", "").split("")));
        return studentSet.equals(correctSet) && !studentSet.isEmpty();
    }

    private String cleanAnswer(String answer) {
        if (answer == null) return "";
        return answer.trim().replaceAll("\\s+", "");
    }

    private double getQuestionScore(StudentAnswer answer, Question question) {
        if (answer.getScore() != null && answer.getScore().compareTo(BigDecimal.ZERO) > 0) {
            return answer.getScore().doubleValue();
        }
        return question.getScore() != null ? question.getScore() : 0;
    }
}