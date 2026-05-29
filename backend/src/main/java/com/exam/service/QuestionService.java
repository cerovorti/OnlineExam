package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Question;

import java.util.List;

public interface QuestionService extends IService<Question> {

    List<Question> getBySubjectId(Long subjectId);
}