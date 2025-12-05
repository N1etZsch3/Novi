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
 * 套卷生成记录实体类
 * <p>
 * 对应数据库表 `paper_generation_record`。
 * 用于存储用户生成的整套试卷元数据。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("paper_generation_record")
public class PaperGenerationRecord {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 科目ID（关联 question_category 表）
     */
    private Long subjectId;

    /**
     * 套卷名称
     */
    private String paperName;

    /**
     * 总题目数量
     */
    private Integer totalQuestions;

    /**
     * 是否启用深度思考（0:否，1:是）
     */
    private Boolean enableThinking;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
