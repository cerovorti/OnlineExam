package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.Paper;
import com.exam.entity.Question;
import com.exam.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/papers")
public class PaperController extends BaseController {

    @Autowired
    private PaperService paperService;

    @GetMapping
    public Result<Page<Paper>> getPapers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Paper> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        Long userId = getCurrentUserId(request);
        wrapper.eq(Paper::getCreatorId, userId);
        wrapper.orderByDesc(Paper::getCreatedAt);
        Page<Paper> result = paperService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping
    public Result<Paper> createPaper(@RequestBody Paper paper, HttpServletRequest request) {
        paper.setCreatorId(getCurrentUserId(request));
        paper.setMode("manual");
        if (paper.getTotalScore() == null) paper.setTotalScore(0);
        if (paper.getQuestionCount() == null) paper.setQuestionCount(0);
        paperService.save(paper);
        return Result.success(paper);
    }

    @PutMapping("/{id}")
    public Result<Void> updatePaper(@PathVariable Long id, @RequestBody Paper paper) {
        paper.setId(id);
        paperService.updateById(paper);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePaper(@PathVariable Long id) {
        paperService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}/preview")
    public Result<Map<String, Object>> previewPaper(@PathVariable Long id) {
        Paper paper = paperService.getById(id);
        if (paper == null) {
            return Result.error("试卷不存在");
        }

        List<Question> questions = paperService.getPaperQuestions(id);

        Map<String, Object> result = new HashMap<>();
        result.put("paper", paper);
        result.put("questions", questions);
        return Result.success(result);
    }
}