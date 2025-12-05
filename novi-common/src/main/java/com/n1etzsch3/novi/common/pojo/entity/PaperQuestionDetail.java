package com.n1etzsch3.novi.common.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 套卷题目明细实体类
 * <p>
 * 对应数据库表 `paper_question_detail`。
 * 用于存储套卷中每个题型的详细信息和生成的题目内容。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("paper_question_detail")
public class PaperQuestionDetail {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套卷ID（关联 paper_generation_record 表）
     */
    private Long paperId;

    /**
     * 题型编码（如：grammar_fill_blank）
     */
    private String questionType;

    /**
     * 题型名称（如：语法填空）
     */
    private String questionTypeName;

    /**
     * 难度（simple/medium/hard）
     */
    private String difficulty;

    /**
     * 题目数量
     */
    private Integer quantity;

    /**
     * 主题（可选，部分题型需要）
     */
    private String theme;

    /**
     * 生成的题目内容（JSON格式）
     */
    private String generatedQuestions;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
