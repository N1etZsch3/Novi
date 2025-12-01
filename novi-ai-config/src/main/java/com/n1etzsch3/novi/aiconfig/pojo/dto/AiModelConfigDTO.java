package com.n1etzsch3.novi.aiconfig.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 模型配置 DTO
 * <p>
 * 用于前端展示，只包含安全的字段，不暴露 API Key 等敏感信息
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 模型配置 DTO")
public class AiModelConfigDTO {

    /**
     * 模型ID
     */
    @Schema(description = "模型ID", example = "1")
    private Long id;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "gpt-4o")
    private String modelName;

    /**
     * 模型描述
     */
    @Schema(description = "模型描述", example = "OpenAI GPT-4o model")
    private String description;

    /**
     * 是否为当前激活模型（0:否，1:是）
     */
    @Schema(description = "是否激活", example = "true")
    private Boolean isActive;
}
