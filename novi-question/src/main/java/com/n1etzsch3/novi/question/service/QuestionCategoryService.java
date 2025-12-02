package com.n1etzsch3.novi.question.service;

import com.n1etzsch3.novi.question.pojo.entity.QuestionCategory;

import java.util.List;

/**
 * 题目分类服务接口
 *
 * @author N1etzsch3
 * @since 2025-12-02
 */
public interface QuestionCategoryService {

    /**
     * 获取分类树
     *
     * @return 树形结构的分类列表
     */
    List<QuestionCategory> getCategoryTree();

    /**
     * 获取所有科目
     *
     * @return 科目列表
     */
    List<QuestionCategory> getSubjects();

    /**
     * 获取指定科目下的题型
     *
     * @param subjectId 科目ID
     * @return 题型列表
     */
    List<QuestionCategory> getQuestionTypes(Long subjectId);
}
