package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exams")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long paperId;
    private Long creatorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notice;
    private Integer antiCheatEnabled;
    private Integer maxCutScreen;
    private Integer shuffleQuestions;
    private Integer shuffleOptions;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
