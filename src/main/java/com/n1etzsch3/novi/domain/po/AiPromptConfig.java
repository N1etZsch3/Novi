package com.n1etzsch3.novi.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 提示词配置实体类
 * <p>
 * 代表 AI 提示词的配置条目，包括系统提示词、
 * 性格和语气风格。
 * </p>
 * <p>
 * 对应数据库表 `ai_prompt_config`。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Data
@TableName("ai_prompt_config")
public class AiPromptConfig {

    /**
     * 配置 Key (主键)
     */
    @TableId(type = IdType.INPUT)
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
