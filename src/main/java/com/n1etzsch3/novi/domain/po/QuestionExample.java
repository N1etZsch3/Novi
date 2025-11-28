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
 * 题库示例实体类
 * <p>
 * 对应数据库表 `question_example`。
 * 存储各科目各题型的示例题目，供AI进行few-shot学习。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("question_example")
public class QuestionExample {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 科目（如：湖北专升本英语、高等数学等）
     */
    private String subject;

    /**
     * 题型（如：语法填空、阅读理解、选择题等）
     */
    private String questionType;

    /**
     * 难度（simple/medium/hard）
     */
    private String difficulty;

    /**
     * 题目内容（JSON格式，包含题目、选项、答案、解析等）
     */
    private String content;

    /**
     * 题目描述或备注
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
