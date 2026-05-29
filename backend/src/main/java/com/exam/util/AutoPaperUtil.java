package com.exam.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.entity.Question;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AutoPaperUtil {

    @Autowired
    private QuestionService questionService;

    public List<Question> generatePaper(Long subjectId, String difficultyRatio, List<String> knowledgePoints) {
        List<Question> allQuestions = new ArrayList<>();
        
        // 按知识点筛选题目
        if (knowledgePoints != null && !knowledgePoints.isEmpty()) {
            for (String point : knowledgePoints) {
                LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Question::getSubjectId, subjectId);
                wrapper.like(Question::getKnowledgePoints, point);
                List<Question> questions = questionService.list(wrapper);
                allQuestions.addAll(questions);
            }
        } else {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Question::getSubjectId, subjectId);
            allQuestions = questionService.list(wrapper);
        }
        
        // 按难度比例筛选
        List<Question> easyQuestions = new ArrayList<>();
        List<Question> mediumQuestions = new ArrayList<>();
        List<Question> hardQuestions = new ArrayList<>();
        
        for (Question q : allQuestions) {
            if ("easy".equals(q.getDifficulty())) {
                easyQuestions.add(q);
            } else if ("medium".equals(q.getDifficulty())) {
                mediumQuestions.add(q);
            } else if ("hard".equals(q.getDifficulty())) {
                hardQuestions.add(q);
            }
        }
        
        // 解析难度比例 (例如 "30,70" 表示易30%，难70%)
        String[] ratios = difficultyRatio.split(",");
        int easyPercent = Integer.parseInt(ratios[0]);
        int hardPercent = Integer.parseInt(ratios[1]);
        
        // 随机打乱题目顺序
        Collections.shuffle(easyQuestions);
        Collections.shuffle(mediumQuestions);
        Collections.shuffle(hardQuestions);
        
        // 按比例选取题目
        List<Question> selectedQuestions = new ArrayList<>();
        int totalCount = allQuestions.size();
        
        int easyCount = totalCount * easyPercent / 100;
        int hardCount = totalCount * hardPercent / 100;
        int mediumCount = totalCount - easyCount - hardCount;
        
        // 添加简单题目
        for (int i = 0; i < Math.min(easyCount, easyQuestions.size()); i++) {
            selectedQuestions.add(easyQuestions.get(i));
        }
        
        // 添加中等题目
        for (int i = 0; i < Math.min(mediumCount, mediumQuestions.size()); i++) {
            selectedQuestions.add(mediumQuestions.get(i));
        }
        
        // 添加困难题目
        for (int i = 0; i < Math.min(hardCount, hardQuestions.size()); i++) {
            selectedQuestions.add(hardQuestions.get(i));
        }
        
        // 再次打乱最终题目顺序
        Collections.shuffle(selectedQuestions);
        
        return selectedQuestions;
    }

    public List<Question> generatePaperByConfig(Long subjectId, List<QuestionConfig> configs) {
        List<Question> selectedQuestions = new ArrayList<>();
        
        for (QuestionConfig config : configs) {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Question::getSubjectId, subjectId);
            wrapper.eq(Question::getType, config.getType());
            
            if (config.getDifficulty() != null && !config.getDifficulty().isEmpty()) {
                wrapper.eq(Question::getDifficulty, config.getDifficulty());
            }
            
            List<Question> questions = questionService.list(wrapper);
            Collections.shuffle(questions);
            
            int count = Math.min(config.getCount(), questions.size());
            for (int i = 0; i < count; i++) {
                Question q = questions.get(i);
                q.setScore(config.getScore());
                selectedQuestions.add(q);
            }
        }
        
        return selectedQuestions;
    }

    public static class QuestionConfig {
        private String type;
        private Integer count;
        private Integer score;
        private String difficulty;

        public QuestionConfig() {}

        public QuestionConfig(String type, Integer count, Integer score, String difficulty) {
            this.type = type;
            this.count = count;
            this.score = score;
            this.difficulty = difficulty;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }
    }
}
