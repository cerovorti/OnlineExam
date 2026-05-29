package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wrong_questions")
public class WrongQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long questionId;
    private Long examRecordId;
    private String myAnswer;
    private String correctAnswer;
    private Integer wrongCount;
    private Integer mastered;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
