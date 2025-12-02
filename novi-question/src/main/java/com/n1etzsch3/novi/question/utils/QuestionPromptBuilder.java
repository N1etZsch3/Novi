package com.n1etzsch3.novi.question.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.aiconfig.mapper.AiPromptConfigMapper;
import com.n1etzsch3.novi.common.pojo.entity.AiPromptConfig;
import com.n1etzsch3.novi.common.pojo.entity.QuestionExample;
import com.n1etzsch3.novi.question.mapper.QuestionCategoryMapper;
import com.n1etzsch3.novi.question.pojo.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.question.pojo.entity.QuestionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI出题提示词构建器 (元数据驱动版)
 * <p>
 * 负责根据请求参数和示例题目动态构建发送给AI的提示词。
 * 所有配置（模板、难度描述）均从数据库动态加载，支持热重载。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionPromptBuilder {

    private final AiPromptConfigMapper aiPromptConfigMapper;
    private final QuestionCategoryMapper questionCategoryMapper;

    /**
     * 构建出题提示词 (完全动态化)
     */
    public String buildPrompt(QuestionGenerationRequest request, List<QuestionExample> examples) {

        // 1. 动态获取 Code (实现热重载的关键)
        // 建议加上 @Cacheable 缓存，避免频繁查库 (后续优化)
        String subjectCode = getCategoryCodeByName(request.getSubject(), 1); // 1=科目
        String typeCode = getCategoryCodeByName(request.getQuestionType(), 2); // 2=题型

        if (subjectCode == null || typeCode == null) {
            throw new IllegalArgumentException(
                    "未知的科目或题型，请检查后台分类配置。Subject: " + request.getSubject() + ", Type: " + request.getQuestionType());
        }

        // 2. 动态获取提示词模板
        // Key 格式: prompt:{subject}:{type}
        String templateKey = String.format("prompt:%s:%s", subjectCode, typeCode);
        String template = getConfigValue(templateKey, null);

        if (template == null) {
            throw new IllegalStateException("未找到对应的提示词模板，请检查 ai_prompt_config 表。Key: " + templateKey);
        }

        // 3. 动态获取难度描述
        // Key 格式: desc:difficulty:{subject}:{type}:{difficulty}
        String difficultyKey = String.format("desc:difficulty:%s:%s:%s",
                subjectCode, typeCode, request.getDifficulty());
        // 如果找不到特定的描述，可以降级去查一个通用的，或者返回一个默认字符串
        String difficultyDesc = getConfigValue(difficultyKey, "难度适中，符合考试大纲要求。");

        // 4. 组装参数
        Map<String, String> variables = new HashMap<>();
        variables.put("subject", request.getSubject()); // 中文名称
        variables.put("theme", StringUtils.hasText(request.getTheme()) ? request.getTheme() : "综合考点");
        variables.put("difficulty", difficultyDesc); // 这里填入的是查出来的具体描述文本
        variables.put("quantity", String.valueOf(request.getQuantity()));
        variables.put("examples", buildExamplesString(examples));

        // 5. 替换并返回
        return replaceVariables(template, variables);
    }

    /**
     * 根据名称查询 Code (查 question_category 表)
     * 实际生产中应配合 Redis 或 Caffeine 缓存
     */
    private String getCategoryCodeByName(String name, Integer type) {
        QuestionCategory category = questionCategoryMapper.selectOne(
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getName, name)
                        .eq(QuestionCategory::getCategoryType, type)
                        .last("LIMIT 1"));
        return category != null ? category.getCode() : null;
    }

    /**
     * 获取配置值辅助方法
     */
    private String getConfigValue(String key, String defaultValue) {
        AiPromptConfig config = aiPromptConfigMapper.selectById(key);
        if (config != null && StringUtils.hasText(config.getConfigValue())) {
            return config.getConfigValue();
        }
        if (defaultValue == null) {
            return null;
        }
        log.warn("Config not found for key: {}, using default.", key);
        return defaultValue;
    }

    private String buildExamplesString(List<QuestionExample> examples) {
        if (examples == null || examples.isEmpty()) {
            return "（暂无参考示例，请严格遵循 JSON 格式要求）";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < examples.size(); i++) {
            sb.append("示例 ").append(i + 1).append(":\n");
            sb.append(examples.get(i).getContent()).append("\n\n");
        }
        return sb.toString();
    }

    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            // 使用 replace 而不是 replaceAll 避免正则转义问题
            // 简单防御：防止 null 值
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace("{" + entry.getKey() + "}", value);
        }
        return result;
    }
}
