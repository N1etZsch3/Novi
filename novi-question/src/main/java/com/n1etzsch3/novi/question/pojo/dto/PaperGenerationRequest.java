package com.n1etzsch3.novi.question.pojo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 套卷生成请求 DTO
 * <p>
 * 代表前端发送的组卷配置请求。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaperGenerationRequest {

    /**
     * 科目ID
     */
    @NotNull(message = "科目ID不能为空")
    private Long subjectId;

    /**
     * 题型配置列表
     * 为空或null时自动查询科目下所有题型（自动模式）
     * 不为空时按指定配置生成（手动模式）
     */
    @Valid
    private List<PaperConfigItem> paperConfig;

    /**
     * 指定使用的AI模型名称（可选）
     * 为空时使用数据库中的激活模型
     */
    private String model;

    /**
     * 是否启用深度思考模式（可选，默认false）
     */
    private Boolean enableThinking = false;
}
