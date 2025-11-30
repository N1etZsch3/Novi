package com.n1etzsch3.novi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 提示词上下文类型枚举
 * <p>
 * 定义不同使用场景下的提示词类型，用于智能切换系统提示词。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Getter
public enum PromptContextType {

    /**
     * 好友聊天场景 - 使用个性化、轻松的提示词
     */
    FRIENDLY_CHAT("friendly", "好友聊天"),

    /**
     * 专业出题场景 - 使用专业、严谨的提示词
     */
    PROFESSIONAL_QUESTION_GEN("professional", "专业出题"),

    /**
     * 专业分析场景 - 使用专业、客观的提示词
     */
    PROFESSIONAL_ANALYSIS("analysis", "专业分析"),

    /**
     * 教学辅导场景 - 使用耐心、详细的提示词
     */
    TEACHING("teaching", "教学辅导");

    private final String code;
    private final String description;

    PromptContextType(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
