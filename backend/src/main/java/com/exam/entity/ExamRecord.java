package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_records")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private Long paperId;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private String status;
    private BigDecimal score;
    private Integer passed;
    private Integer cutScreenCount;
    private String submitType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
