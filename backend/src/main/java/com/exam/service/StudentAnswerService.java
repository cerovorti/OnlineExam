package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.StudentAnswer;

import java.util.List;

public interface StudentAnswerService extends IService<StudentAnswer> {

    StudentAnswer saveOrUpdateAnswer(StudentAnswer answer);

    List<StudentAnswer> getAnswersByRecordId(Long examRecordId);

    void autoGradeForRecord(Long examRecordId);
}