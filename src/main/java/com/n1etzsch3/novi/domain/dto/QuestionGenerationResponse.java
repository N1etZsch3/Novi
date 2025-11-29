package com.n1etzsch3.novi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI出题响应 DTO
 * <p>
 * 代表AI出题服务返回的响应结果。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGenerationResponse {

    /**
     * 出题记录ID
     * <p>
     * 用于标识此次生成记录，以便后续查询历史记录详情。
     * </p>
     */
    private Long recordId;

    /**
     * AI生成的题目内容（JSON格式）
     * <p>
     * 包含完整的题目、选项、答案、解析等信息。
     * 具体JSON结构依题型而定。
     * </p>
     */
    private String questions;
}
