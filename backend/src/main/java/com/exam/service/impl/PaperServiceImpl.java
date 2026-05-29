package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.mapper.PaperMapper;
import com.exam.service.PaperQuestionService;
import com.exam.service.PaperService;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private QuestionService questionService;

    @Override
    public List<Question> getPaperQuestions(Long paperId) {
        List<PaperQuestion> paperQuestions = paperQuestionService.list(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId)
                        .orderByAsc(PaperQuestion::getSortOrder)
        );

        if (paperQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> questionIds = paperQuestions.stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toList());

        List<Question> questions = questionService.listByIds(questionIds);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<Question> orderedQuestions = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            Question q = questionMap.get(pq.getQuestionId());
            if (q != null) {
                q.setScore(pq.getScore());
                orderedQuestions.add(q);
            }
        }

        return orderedQuestions;
    }

    @Override
    @Transactional
    public Paper createPaperWithQuestions(Paper paper, List<Long> questionIds, List<Integer> scores) {
        if (paper.getTotalScore() == null) paper.setTotalScore(0);
        if (paper.getQuestionCount() == null) paper.setQuestionCount(0);
        this.save(paper);

        int totalScore = 0;
        for (int i = 0; i < questionIds.size(); i++) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paper.getId());
            pq.setQuestionId(questionIds.get(i));
            pq.setSortOrder(i + 1);
            Integer score = (scores != null && i < scores.size()) ? scores.get(i) : 1;
            pq.setScore(score);
            paperQuestionService.save(pq);
            totalScore += score;
        }

        paper.setTotalScore(totalScore);
        paper.setQuestionCount(questionIds.size());
        this.updateById(paper);

        return paper;
    }
}