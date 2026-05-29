package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.Question;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/questions")
public class QuestionController extends BaseController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public Result<Page<Question>> getQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String knowledgePoints) {

        Page<Question> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        if (type != null && !type.isEmpty()) {
            wrapper.eq(Question::getType, type);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
        }
        if (knowledgePoints != null && !knowledgePoints.isEmpty()) {
            wrapper.like(Question::getKnowledgePoints, knowledgePoints);
        }

        wrapper.orderByDesc(Question::getCreatedAt);

        Page<Question> result = questionService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping
    public Result<Void> addQuestion(@RequestBody Question question, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        question.setCreatorId(userId);
        questionService.save(question);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.updateById(question);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.removeById(id);
        return Result.success();
    }

    @PostMapping("/import")
    public Result<Void> importQuestions(@RequestParam("file") MultipartFile file,
                                         HttpServletRequest request) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        try {
            org.apache.poi.ss.usermodel.Workbook wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            Long userId = getCurrentUserId(request);
            int saved = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                String type = getCellVal(row, 0);
                String title = getCellVal(row, 1);
                String options = getCellVal(row, 2);
                String answer = getCellVal(row, 3);
                String difficulty = getCellVal(row, 4);
                String knowledgePoints = getCellVal(row, 5);
                String scoreStr = getCellVal(row, 6);
                Long subjectId = Long.valueOf(getCellVal(row, 7));

                if (title.isEmpty()) continue;

                Question q = new Question();
                q.setType(type);
                q.setTitle(title);
                q.setOptions(options.isEmpty() ? null : options);
                q.setAnswer(answer);
                q.setDifficulty(difficulty.isEmpty() ? "medium" : difficulty);
                q.setKnowledgePoints(knowledgePoints.isEmpty() ? null : knowledgePoints);
                q.setScore(scoreStr.isEmpty() ? 1 : Integer.parseInt(scoreStr));
                q.setSubjectId(subjectId);
                q.setCreatorId(userId);
                questionService.save(q);
                saved++;
            }
            wb.close();
            return Result.success();
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=question_template.csv");
        response.getWriter().write('\uFEFF');
        response.getWriter().write("题型,题目,选项,答案,难度,知识点,分值,科目ID\n");
        response.getWriter().write("single,Java的保留字是什么？,\"A.include|B.define|C.goto|D.NULL\",C,easy,Java基础,2,1\n");
        response.getWriter().flush();
    }

    private String getCellVal(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return "";
        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}