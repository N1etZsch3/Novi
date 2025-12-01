package com.n1etzsch3.novi.question.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "出题历史记录项")
public class QuestionHistoryItem {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID", example = "1001")
    private Long id;

    /**
     * 科目
     */
    @Schema(description = "科目", example = "湖北专升本英语")
    private String subject;

    /**
     * 题型
     */
    @Schema(description = "题型", example = "选择题")
    private String questionType;

    /**
     * 主题（可选）
     */
    @Schema(description = "主题", example = "虚拟语气")
    private String theme;

    /**
     * 难度
     */
    @Schema(description = "难度", example = "medium")
    private String difficulty;

    /**
     * 题目数量
     */
    @Schema(description = "题目数量", example = "5")
    private Integer quantity;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
