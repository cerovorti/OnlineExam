package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.ExamRecord;

import java.util.List;

public interface ExamRecordService extends IService<ExamRecord> {

    ExamRecord startExam(Long examId, Long studentId, Long paperId);

    ExamRecord getByExamAndStudent(Long examId, Long studentId);

    List<ExamRecord> getByExamId(Long examId);
}