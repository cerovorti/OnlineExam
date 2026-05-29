package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Exam;
import com.exam.entity.ExamRecord;
import com.exam.entity.Question;

import java.util.List;
import java.util.Map;

public interface ExamService extends IService<Exam> {

    List<Exam> getStudentExams(Long studentId, String status);

    List<Question> getExamQuestions(Long examId, Long studentId);

    ExamRecord submitExam(Long examId, Long studentId, String submitType, Integer cutScreenCount);

    Map<String, Object> getExamAnalysis(Long examId);
}