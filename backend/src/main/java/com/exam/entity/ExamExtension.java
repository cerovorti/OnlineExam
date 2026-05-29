package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_extensions")
public class ExamExtension {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examRecordId;
    private Integer extendMinutes;
    private String reason;
    private Long operatorId;
    private LocalDateTime createdAt;
}
