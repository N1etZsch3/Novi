package com.n1etzsch3.novi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class AiModelConfigDTO {

    /**
     * 模型ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 是否为当前激活模型（0:否，1:是）
     */
    private Boolean isActive;
}
