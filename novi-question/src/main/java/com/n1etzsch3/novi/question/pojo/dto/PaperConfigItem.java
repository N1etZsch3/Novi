package com.n1etzsch3.novi.question.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 套卷题型配置项 DTO
 * <p>
 * 表示套卷中单个题型的配置信息。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperConfigItem {

    /**
     * 题型编码（如：grammar_fill_blank）
     */
    @NotBlank(message = "题型编码不能为空")
    private String questionTypeCode;

    /**
     * 题目数量
     */
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量必须至少为1")
    private Integer quantity;

    /**
     * 难度级别（simple/medium/hard）
     */
    @NotBlank(message = "难度不能为空")
    private String difficulty;

    /**
     * 主题（可选，部分题型需要）
     */
    private String theme;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    @Min(value = 1, message = "显示顺序必须从1开始")
    private Integer order;
}
