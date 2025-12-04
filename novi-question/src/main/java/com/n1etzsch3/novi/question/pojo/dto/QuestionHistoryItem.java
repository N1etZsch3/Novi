package com.n1etzsch3.novi.question.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 出题历史记录列表项 DTO
 * <p>
 * 用于历史记录列表展示，包含摘要信息。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionHistoryItem {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 科目
     */
    private String subject;

    /**
     * 题型
     */
    private String questionType;

    /**
     * 主题（可选）
     */
    private String theme;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 题目数量
     */
    private Integer quantity;

    /**
     * 是否使用了深度思考
     */
    private Boolean enableThinking;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
