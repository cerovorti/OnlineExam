package com.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.ExamExtension;
import com.exam.mapper.ExamExtensionMapper;
import com.exam.service.ExamExtensionService;
import org.springframework.stereotype.Service;

@Service
public class ExamExtensionServiceImpl extends ServiceImpl<ExamExtensionMapper, ExamExtension> implements ExamExtensionService {
}
