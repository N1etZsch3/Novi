package com.n1etzsch3.novi.utils;

import com.n1etzsch3.novi.domain.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.domain.po.QuestionExample;
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
public class QuestionPromptBuilder {

    /**
     * 构建出题提示词
     *
     * @param request  出题请求
     * @param examples 示例题目列表
     * @return 构建好的提示词字符串
     */
    public String buildPrompt(QuestionGenerationRequest request, List<QuestionExample> examples) {
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
        prompt.append("请严格按照JSON数组格式输出，不要包含任何Markdown标记（如```json），直接返回JSON字符串。\n");
        prompt.append("JSON结构必须包含以下字段：\n");
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
}
