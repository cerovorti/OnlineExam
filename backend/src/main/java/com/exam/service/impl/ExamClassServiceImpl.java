package com.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.ExamClass;
import com.exam.mapper.ExamClassMapper;
import com.exam.service.ExamClassService;
import org.springframework.stereotype.Service;

@Service
public class ExamClassServiceImpl extends ServiceImpl<ExamClassMapper, ExamClass> implements ExamClassService {
}
