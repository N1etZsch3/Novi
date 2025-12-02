package com.n1etzsch3.novi.question.controller;

import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.question.pojo.entity.QuestionCategory;
import com.n1etzsch3.novi.question.service.QuestionCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目分类控制器
 *
 * @author N1etzsch3
 * @since 2025-12-02
 */
@RestController
@RequestMapping("/api/question/categories")
@RequiredArgsConstructor
public class QuestionCategoryController {

    private final QuestionCategoryService questionCategoryService;

    /**
     * 获取分类树
     *
     * @return 树形结构的分类列表
     */
    @GetMapping("/tree")
    public Result<List<QuestionCategory>> getTree() {
        return Result.success(questionCategoryService.getCategoryTree());
    }

    /**
     * 获取所有科目
     *
     * @return 科目列表
     */
    @GetMapping("/subjects")
    public Result<List<QuestionCategory>> getSubjects() {
        return Result.success(questionCategoryService.getSubjects());
    }

    /**
     * 获取指定科目下的题型
     *
     * @param subjectId 科目ID
     * @return 题型列表
     */
    @GetMapping("/types")
    public Result<List<QuestionCategory>> getQuestionTypes(@RequestParam Long subjectId) {
        return Result.success(questionCategoryService.getQuestionTypes(subjectId));
    }
}
