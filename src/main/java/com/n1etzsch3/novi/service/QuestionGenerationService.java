package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.domain.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationResponse;
import com.n1etzsch3.novi.domain.dto.QuestionHistoryItem;

import java.util.List;

/**
 * AI出题服务接口
 * <p>
 * 负责根据配置生成题目，并管理出题历史记录。
 * </p>
 * <p>
 * 在调用AI生成题目时，会使用
 * {@link com.n1etzsch3.novi.enums.PromptContextType#PROFESSIONAL_QUESTION_GEN}
 * 上下文，以获得专业、严谨的出题提示词。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
public interface QuestionGenerationService {

    /**
     * 生成题目
     *
     * @param userId  用户ID
     * @param request 出题请求配置
     * @return 出题响应，包含生成的题目和记录ID
     */
    QuestionGenerationResponse generateQuestions(Long userId, QuestionGenerationRequest request);

    /**
     * 获取用户的出题历史记录列表
     *
     * @param userId 用户ID
     * @return 历史记录列表
     */
    List<QuestionHistoryItem> getGenerationHistory(Long userId);

    /**
     * 获取单条出题记录详情
     *
     * @param recordId 记录ID
     * @param userId   用户ID（用于权限验证）
     * @return 出题响应，包含完整题目
     */
    QuestionGenerationResponse getRecordDetail(Long recordId, Long userId);

    /**
     * 删除出题记录
     *
     * @param recordId 记录ID
     * @param userId   用户ID
     */
    void deleteGenerationRecord(Long recordId, Long userId);
}
