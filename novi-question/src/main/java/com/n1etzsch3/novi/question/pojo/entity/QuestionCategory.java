package com.n1etzsch3.novi.question.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目分类实体类
 * <p>
 * 用于管理科目、题型等层级数据。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-02
 */
@Data
@TableName("question_category")
public class QuestionCategory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 父级ID (0表示顶级)
     */
    private Long parentId;

    /**
     * 类型 (1:科目, 2:题型, 3:套卷)
     */
    private Integer categoryType;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 自动组卷时的生成次数
     * 例如：阅读理解需要2篇文章，则设为2；普通题型设为1
     */
    private Integer generationCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 子分类列表 (非数据库字段)
     */
    @TableField(exist = false)
    private List<QuestionCategory> children;
}
