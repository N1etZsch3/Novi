package com.n1etzsch3.novi.question.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 套卷生成响应 DTO
 * <p>
 * 用于 SSE 事件推送的数据结构。
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
public class PaperGenerationResponse {

    /**
     * 事件类型（question/error/complete）
     */
    private String eventType;

    /**
     * 题型编码
     */
    private String questionType;

    /**
     * 题型名称
     */
    private String questionTypeName;

    /**
     * 显示顺序
     */
    private Integer order;

    /**
     * 题目列表（JSON字符串或已解析的对象列表）
     */
    private String questions;

    /**
     * 错误信息（当 eventType=error 时使用）
     */
    private String error;

    /**
     * 套卷ID（当 eventType=complete 时使用）
     */
    private Long paperId;

    /**
     * 总题目数（当 eventType=complete 时使用）
     */
    private Integer totalQuestions;

    /**
     * 成功数量（当 eventType=complete 时使用）
     */
    private Integer successCount;

    /**
     * 失败数量（当 eventType=complete 时使用）
     */
    private Integer failedCount;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}
