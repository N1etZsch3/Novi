package com.n1etzsch3.novi.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.question.pojo.entity.QuestionCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目分类 Mapper 接口
 *
 * @author N1etzsch3
 * @since 2025-12-02
 */
@Mapper
public interface QuestionCategoryMapper extends BaseMapper<QuestionCategory> {
}
