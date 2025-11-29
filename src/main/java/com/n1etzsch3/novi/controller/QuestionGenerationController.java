package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.domain.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationResponse;
import com.n1etzsch3.novi.domain.dto.QuestionHistoryItem;
import com.n1etzsch3.novi.domain.dto.Result;
import com.n1etzsch3.novi.service.QuestionGenerationService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI出题控制器
 * <p>
 * 提供AI出题相关接口，包括生成题目、查询历史记录等。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionGenerationController {

    private final QuestionGenerationService questionGenerationService;

    /**
     * 生成题目
     *
     * @param request 出题请求参数
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<QuestionGenerationResponse> generateQuestions(
            @RequestBody @Validated QuestionGenerationRequest request) {
        Long userId = LoginUserContext.getUserId();
        log.info("Received question generation request from user: {}", userId);
        QuestionGenerationResponse response = questionGenerationService.generateQuestions(userId, request);
        return Result.success(response);
    }

    /**
     * 获取出题历史记录列表
     *
     * @return 历史记录列表
     */
    @GetMapping("/history")
    public Result<List<QuestionHistoryItem>> getGenerationHistory() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(questionGenerationService.getGenerationHistory(userId));
    }

    /**
     * 获取单条出题记录详情
     *
     * @param recordId 记录ID
     * @return 记录详情
     */
    @GetMapping("/history/{recordId}")
    public Result<QuestionGenerationResponse> getRecordDetail(@PathVariable Long recordId) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(questionGenerationService.getRecordDetail(recordId, userId));
    }

    /**
     * 删除出题记录
     *
     * @param recordId 记录ID
     * @return 成功提示
     */
    @DeleteMapping("/history/{recordId}")
    public Result<Void> deleteRecord(@PathVariable Long recordId) {
        Long userId = LoginUserContext.getUserId();
        questionGenerationService.deleteGenerationRecord(recordId, userId);
        return Result.success(null);
    }

    /**
     * 批量删除出题记录
     *
     * @param recordIds 记录ID列表
     * @return 成功提示
     */
    @DeleteMapping("/history")
    public Result<Void> deleteRecords(@RequestBody List<Long> recordIds) {
        Long userId = LoginUserContext.getUserId();
        questionGenerationService.deleteGenerationRecords(recordIds, userId);
        return Result.success(null);
    }
}
