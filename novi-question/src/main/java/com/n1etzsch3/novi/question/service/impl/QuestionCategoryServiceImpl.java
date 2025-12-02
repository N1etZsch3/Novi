package com.n1etzsch3.novi.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.question.mapper.QuestionCategoryMapper;
import com.n1etzsch3.novi.question.pojo.entity.QuestionCategory;
import com.n1etzsch3.novi.question.service.QuestionCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目分类服务实现类
 *
 * @author N1etzsch3
 * @since 2025-12-02
 */
@Service
@RequiredArgsConstructor
public class QuestionCategoryServiceImpl implements QuestionCategoryService {

    private final QuestionCategoryMapper questionCategoryMapper;

    @Override
    public List<QuestionCategory> getCategoryTree() {
        // 1. 获取所有分类
        List<QuestionCategory> allCategories = questionCategoryMapper.selectList(
                new LambdaQueryWrapper<QuestionCategory>()
                        .orderByAsc(QuestionCategory::getSortOrder));

        // 2. 构建树形结构
        Map<Long, List<QuestionCategory>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(QuestionCategory::getParentId));

        List<QuestionCategory> roots = parentMap.getOrDefault(0L, new ArrayList<>());
        buildTree(roots, parentMap);

        return roots;
    }

    private void buildTree(List<QuestionCategory> nodes, Map<Long, List<QuestionCategory>> parentMap) {
        for (QuestionCategory node : nodes) {
            List<QuestionCategory> children = parentMap.get(node.getId());
            if (children != null) {
                node.setChildren(children);
                buildTree(children, parentMap);
            }
        }
    }

    @Override
    public List<QuestionCategory> getSubjects() {
        return questionCategoryMapper.selectList(
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getCategoryType, 1) // 1: 科目
                        .orderByAsc(QuestionCategory::getSortOrder));
    }

    @Override
    public List<QuestionCategory> getQuestionTypes(Long subjectId) {
        return questionCategoryMapper.selectList(
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getParentId, subjectId)
                        .eq(QuestionCategory::getCategoryType, 2) // 2: 题型
                        .orderByAsc(QuestionCategory::getSortOrder));
    }
}
