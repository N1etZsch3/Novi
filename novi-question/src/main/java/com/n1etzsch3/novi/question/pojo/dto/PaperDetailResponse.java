package com.n1etzsch3.novi.question.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 套卷详情响应 DTO
 * <p>
 * 用于返回套卷的完整详细信息。
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
public class PaperDetailResponse {

    /**
     * 套卷ID
     */
    private Long id;

    /**
     * 套卷名称
     */
    private String paperName;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 总题目数量
     */
    private Integer totalQuestions;

    /**
     * 是否启用深度思考
     */
    private Boolean enableThinking;

    /**
     * 题目明细列表
     */
    private List<QuestionTypeDetail> details;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 题型明细内部类
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTypeDetail {
        /**
         * 题型编码
         */
        private String questionType;

        /**
         * 题型名称
         */
        private String questionTypeName;

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
         * 显示顺序
         */
        private Integer order;

        /**
         * 生成的题目（JSON字符串）
         */
        private String questions;
    }
}
