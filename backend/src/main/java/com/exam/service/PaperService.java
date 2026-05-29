package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Paper;
import com.exam.entity.Question;

import java.util.List;

public interface PaperService extends IService<Paper> {

    List<Question> getPaperQuestions(Long paperId);

    Paper createPaperWithQuestions(Paper paper, List<Long> questionIds, List<Integer> scores);
}