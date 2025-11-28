package com.n1etzsch3.novi.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 出题历史记录实体类
 * <p>
 * 对应数据库表 `question_generation_record`。
 * 存储用户的AI出题请求和生成结果。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("question_generation_record")
public class QuestionGenerationRecord {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联user_account表）
     */
    private Long userId;

    /**
     * 科目
     */
    private String subject;

    /**
     * 题型
     */
    private String questionType;

    /**
     * 主题（部分题型需要，可为空）
     */
    private String theme;

    /**
     * 难度（simple/medium/hard）
     */
    private String difficulty;

    /**
     * 题目数量
     */
    private Integer quantity;

    /**
     * AI生成的题目内容（JSON格式）
     */
    private String generatedQuestions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
