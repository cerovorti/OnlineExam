package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("papers")
public class Paper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long subjectId;
    private Long creatorId;
    private Integer totalScore;
    private Integer passScore;
    private Integer duration;
    private String mode;
    private String config;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
