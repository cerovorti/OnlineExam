package com.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.Subject;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping
    public Result<Page<Subject>> getSubjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<Subject> pageParam = new Page<>(page, pageSize);
        Page<Subject> result = subjectService.page(pageParam);
        return Result.success(result);
    }

    @PostMapping
    public Result<Void> createSubject(@RequestBody Subject subject) {
        subjectService.save(subject);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateSubject(@PathVariable Long id, @RequestBody Subject subject) {
        subject.setId(id);
        subjectService.updateById(subject);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        subjectService.removeById(id);
        return Result.success();
    }
}
