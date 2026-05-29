package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Question;
import com.exam.entity.WrongQuestion;
import com.exam.mapper.WrongQuestionMapper;
import com.exam.service.QuestionService;
import com.exam.service.WrongQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WrongQuestionServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionService {

    @Autowired
    private QuestionService questionService;

    @Override
    @Transactional
    public void collectWrongAnswer(Long studentId, Long questionId, Long examRecordId, String myAnswer, String correctAnswer) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getStudentId, studentId);
        wrapper.eq(WrongQuestion::getQuestionId, questionId);

        WrongQuestion existing = this.getOne(wrapper);
        if (existing != null) {
            int current = existing.getWrongCount() != null ? existing.getWrongCount() : 0;
            existing.setWrongCount(current + 1);
            existing.setMyAnswer(myAnswer);
            existing.setCorrectAnswer(correctAnswer);
            existing.setExamRecordId(examRecordId);
            existing.setMastered(0);
            this.updateById(existing);
        } else {
            WrongQuestion wq = new WrongQuestion();
            wq.setStudentId(studentId);
            wq.setQuestionId(questionId);
            wq.setExamRecordId(examRecordId);
            wq.setMyAnswer(myAnswer);
            wq.setCorrectAnswer(correctAnswer);
            wq.setWrongCount(1);
            wq.setMastered(0);
            this.save(wq);
        }
    }

    @Override
    public List<WrongQuestion> getByStudentId(Long studentId, Long subjectId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getStudentId, studentId);
        List<WrongQuestion> wrongQuestions = this.list(wrapper);

        if (subjectId != null) {
            List<Long> questionIds = wrongQuestions.stream()
                    .map(WrongQuestion::getQuestionId)
                    .collect(Collectors.toList());

            if (!questionIds.isEmpty()) {
                List<Question> questions = questionService.listByIds(questionIds);
                List<Long> filteredIds = questions.stream()
                        .filter(q -> q.getSubjectId().equals(subjectId))
                        .map(Question::getId)
                        .collect(Collectors.toList());

                wrongQuestions = wrongQuestions.stream()
                        .filter(wq -> filteredIds.contains(wq.getQuestionId()))
                        .collect(Collectors.toList());
            }
        }

        return wrongQuestions;
    }
}