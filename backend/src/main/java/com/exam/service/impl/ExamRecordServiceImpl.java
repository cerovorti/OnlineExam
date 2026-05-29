package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.ExamRecord;
import com.exam.mapper.ExamRecordMapper;
import com.exam.service.ExamRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Override
    @Transactional
    public ExamRecord startExam(Long examId, Long studentId, Long paperId) {
        ExamRecord existing = getByExamAndStudent(examId, studentId);
        if (existing != null && "in_progress".equals(existing.getStatus())) {
            return existing;
        }

        if (existing != null && "submitted".equals(existing.getStatus())) {
            return null;
        }

        if (existing != null && "not_started".equals(existing.getStatus())) {
            existing.setStartTime(LocalDateTime.now());
            existing.setStatus("in_progress");
            existing.setCutScreenCount(0);
            this.updateById(existing);
            return existing;
        }

        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setStudentId(studentId);
        record.setPaperId(paperId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("in_progress");
        record.setCutScreenCount(0);
        this.save(record);
        return record;
    }

    @Override
    public ExamRecord getByExamAndStudent(Long examId, Long studentId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getExamId, examId);
        wrapper.eq(ExamRecord::getStudentId, studentId);
        return this.getOne(wrapper);
    }

    @Override
    public List<ExamRecord> getByExamId(Long examId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getExamId, examId);
        return this.list(wrapper);
    }
}