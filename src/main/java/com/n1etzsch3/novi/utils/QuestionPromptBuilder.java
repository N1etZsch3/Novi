package com.n1etzsch3.novi.utils;

import com.n1etzsch3.novi.domain.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.domain.po.AiPromptConfig;
import com.n1etzsch3.novi.domain.po.QuestionExample;
import com.n1etzsch3.novi.mapper.AiPromptConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * AI出题提示词构建器
 * <p>
 * 负责根据请求参数和示例题目动态构建发送给AI的提示词。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Component
@RequiredArgsConstructor
public class QuestionPromptBuilder {

    private final AiPromptConfigMapper aiPromptConfigMapper;

    /**
     * 构建出题提示词
     *
     * @param request  出题请求
     * @param examples 示例题目列表
     * @return 构建好的提示词字符串
     */
    public String buildPrompt(QuestionGenerationRequest request, List<QuestionExample> examples) {
        // 1. 尝试从数据库获取提示词模板
        String templateKey = getTemplateKey(request.getQuestionType());
        AiPromptConfig config = aiPromptConfigMapper.selectById(templateKey);

        if (config != null) {
            return buildFromTemplate(config.getConfigValue(), request, examples);
        }

        // 2. 降级逻辑：如果数据库没有配置，使用默认硬编码逻辑 (或者抛出异常)
        return buildDefaultPrompt(request, examples);
    }

    private String getTemplateKey(String questionType) {
        // 目前仅支持语法填空，后续可以扩展
        if ("语法填空".equals(questionType)) {
            return "prompt:question:grammar_fill_blank";
        }
        return "prompt:question:default";
    }

    private String buildFromTemplate(String template, QuestionGenerationRequest request,
            List<QuestionExample> examples) {
        String prompt = template;

        // 替换基本参数
        prompt = prompt.replace("{subject}", request.getSubject());
        prompt = prompt.replace("{quantity}", String.valueOf(request.getQuantity()));
        prompt = prompt.replace("{difficulty}", getDifficultyDescription(request.getDifficulty()));

        // 替换主题 (如果有)
        if (StringUtils.hasText(request.getTheme())) {
            // 模板中可能没有预留theme占位符，这里简单追加或者假设模板支持
            // 既然用户给的模板没有{theme}，我们暂时忽略，或者追加在Constraints里
            // 为了严谨，我们假设模板里应该有{theme}，但用户给的模板没有。
            // 我们可以在Task部分追加
            if (prompt.contains("{theme}")) {
                prompt = prompt.replace("{theme}", request.getTheme());
            } else {
                // 简单插入到Task描述后
                prompt = prompt.replace("# Task", "# Task\n主题：" + request.getTheme());
            }
        }

        // 构建示例字符串
        StringBuilder examplesBuilder = new StringBuilder();
        if (examples != null && !examples.isEmpty()) {
            for (int i = 0; i < examples.size(); i++) {
                QuestionExample example = examples.get(i);
                examplesBuilder.append("示例 ").append(i + 1).append(":\n");
                examplesBuilder.append(example.getContent()).append("\n\n");
            }
        } else {
            examplesBuilder.append("（暂无示例）");
        }

        prompt = prompt.replace("{examples}", examplesBuilder.toString());

        return prompt;
    }

    private String buildDefaultPrompt(QuestionGenerationRequest request, List<QuestionExample> examples) {
        StringBuilder prompt = new StringBuilder();

        // 1. 角色设定与任务描述
        prompt.append("你是一位专业的").append(request.getSubject()).append("出题专家。");
        prompt.append("请根据以下要求生成").append(request.getQuantity()).append("道");
        prompt.append(request.getDifficulty()).append("难度的");
        prompt.append("【").append(request.getQuestionType()).append("】题目。");

        if (StringUtils.hasText(request.getTheme())) {
            prompt.append("题目主题应围绕：").append(request.getTheme()).append("。");
        }
        prompt.append("\n\n");

        // 2. 格式要求
        prompt.append("### 输出格式要求\n");
        prompt.append("1. 请严格按照JSON数组格式输出，即使只有一道题，也必须包裹在[]中。\n");
        prompt.append("2. 不要包含任何Markdown标记（如```json），直接返回JSON字符串。\n");
        prompt.append("3. ⚠️重要：确保所有字符串值内部的双引号都使用反斜杠转义（例如 \" -> \\\"）。\n");
        prompt.append("4. JSON结构必须包含以下字段：\n");
        prompt.append("- content: 题目内容（如果是选择题，包含选项）\n");
        prompt.append("- answer: 正确答案\n");
        prompt.append("- analysis: 解析\n");
        prompt.append("- difficulty: 难度（simple/medium/hard）\n");
        prompt.append("- type: 题型\n\n");

        // 3. 示例学习 (Few-Shot)
        if (examples != null && !examples.isEmpty()) {
            prompt.append("### 参考示例\n");
            prompt.append("请参考以下题目的风格和格式：\n");
            for (int i = 0; i < examples.size(); i++) {
                QuestionExample example = examples.get(i);
                prompt.append("示例 ").append(i + 1).append(":\n");
                prompt.append(example.getContent()).append("\n\n");
            }
        }

        // 4. 约束条件
        prompt.append("### 约束条件\n");
        prompt.append("1. 题目必须符合").append(request.getSubject()).append("的考试大纲要求。\n");
        prompt.append("2. 难度必须严格控制在").append(request.getDifficulty()).append("水平。\n");
        prompt.append("3. 确保题目内容准确无误，解析详细易懂。\n");
        prompt.append("4. 生成数量必须为").append(request.getQuantity()).append("道。\n");

        return prompt.toString();
    }

    private String getDifficultyDescription(String level) {
        if (level == null)
            return "词汇量控制在 3500 词以内，词汇范围大概是高考英语词汇范围，句子结构适中，符合专升本学生标准水平。";
        switch (level.toLowerCase()) {
            case "simple":
                return "难度为simple，词汇量控制在 2500 词以内，句子结构简单，主要考察基础语法，符合专升本英语基础水平。";
            case "hard":
                return "难度为hard，词汇量控制在 4500 词以内，增加长难句和生僻词，20+句话，考察综合语言运用能力，语法知识点更难，难度略高于专升本平均水平。";
            case "medium":
            default:
                return "难度为medium，词汇量控制在 4000 词以内，词汇范围大概是高考英语词汇范围，句子结构适中，大概十五句话，可以有几句长难句，符合专升本学生标准水平。";
        }
    }
}
