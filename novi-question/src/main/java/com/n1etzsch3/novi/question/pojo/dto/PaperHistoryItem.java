package com.n1etzsch3.novi.question.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 套卷历史记录列表项 DTO
 * <p>
 * 用于套卷历史列表的显示。
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
public class PaperHistoryItem {

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
     * 创建时间
     */
    private LocalDateTime createdAt;
}
