package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.WrongQuestion;

import java.util.List;

public interface WrongQuestionService extends IService<WrongQuestion> {

    void collectWrongAnswer(Long studentId, Long questionId, Long examRecordId, String myAnswer, String correctAnswer);

    List<WrongQuestion> getByStudentId(Long studentId, Long subjectId);
}