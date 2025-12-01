package com.n1etzsch3.novi.question.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI出题请求 DTO
 * <p>
 * 代表前端发送的出题配置请求。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI出题请求")
public class QuestionGenerationRequest {

    /**
     * 科目（如：湖北专升本英语、高等数学等）
     */
    @NotBlank(message = "科目不能为空")
    @Schema(description = "科目", example = "湖北专升本英语")
    private String subject;

    /**
     * 题型（如：语法填空、阅读理解、选择题等）
     */
    @NotBlank(message = "题型不能为空")
    @Schema(description = "题型", example = "选择题")
    private String questionType;

    /**
     * 主题（部分题型需要，如作文主题等）
     * <p>
     * 可选字段，某些题型如"语法填空"可能不需要指定主题。
     * </p>
     */
    @Schema(description = "主题 (可选)", example = "虚拟语气")
    private String theme;

    /**
     * 难度级别
     * <p>
     * 取值范围：simple（简单）、medium（中等）、hard（困难）
     * </p>
     */
    @NotBlank(message = "难度不能为空")
    @Schema(description = "难度 (simple, medium, hard)", example = "medium")
    private String difficulty;

    /**
     * 题目数量
     * <p>
     * 必须大于0。
     * </p>
     */
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量必须至少为1")
    @Schema(description = "题目数量", example = "5")
    private Integer quantity;
}
