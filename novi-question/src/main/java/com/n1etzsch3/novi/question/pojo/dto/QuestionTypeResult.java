package com.n1etzsch3.novi.question.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 题型生成结果（内部使用）
 * <p>
 * 用于封装单个题型的生成结果，仅在 Service 层内部使用。
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
public class QuestionTypeResult {

    /**
     * 题型编码
     */
    private String questionTypeCode;

    /**
     * 题型名称
     */
    private String questionTypeName;

    /**
     * 显示顺序
     */
    private Integer order;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 题目数量
     */
    private Integer quantity;

    /**
     * 主题
     */
    private String theme;

    /**
     * 生成的题目（JSON字符串）
     */
    private String questionsJson;

    /**
     * 是否生成成功
     */
    private Boolean success;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;
}
