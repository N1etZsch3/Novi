package com.n1etzsch3.novi.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 提示词配置实体类
 * <p>
 * 代表 AI 提示词的配置条目，包括系统提示词、
 * 性格和语气风格。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Data
public class AiPromptConfig {
    /**
     * 主键。
     */
    private Long id;

    /**
     * 配置 Key (例如: "personality_default")。
     */
    private String configKey;

    /**
     * 配置值 (例如: 实际的提示词文本)。
     */
    private String configValue;

    /**
     * 配置描述。
     */
    private String description;

    /**
     * 配置类型 (0:系统, 1:性格, 2:语气风格)。
     */
    private Integer configType;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updateTime;
}
