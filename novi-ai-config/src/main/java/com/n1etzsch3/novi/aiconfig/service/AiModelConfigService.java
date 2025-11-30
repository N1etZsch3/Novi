package com.n1etzsch3.novi.aiconfig.service;

import com.n1etzsch3.novi.aiconfig.pojo.dto.AiModelConfigDTO;
import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;

import java.util.List;

/**
 * AI模型配置服务接口
 * <p>
 * 提供模型查询和切换功能，不提供模型的增删改功能。
 * 模型配置由系统管理员在数据库中直接管理。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
public interface AiModelConfigService {

    /**
     * 获取当前激活的模型配置（内部使用）
     * <p>
     * 此方法返回完整的配置信息，包含 API Key 等敏感信息，
     * 仅供系统内部组件（如 DynamicChatModelFactory）使用，
     * 不应暴露给外部API。
     * </p>
     *
     * @return 当前激活的模型配置，如果没有则返回 null
     */
    AiModelConfig getActiveModel();

    /**
     * 获取当前激活的模型配置（DTO）
     * <p>
     * 仅返回安全字段，不包含 API Key 等敏感信息
     * </p>
     *
     * @return 当前激活的模型配置DTO，如果没有则返回 null
     */
    AiModelConfigDTO getActiveModelDTO();

    /**
     * 根据模型名称切换到指定的模型
     *
     * @param modelName 要激活的模型名称
     * @return 切换是否成功
     */
    boolean switchModelByName(String modelName);

    /**
     * 列出所有可用的模型配置（DTO）
     * <p>
     * 仅返回安全字段，不包含 API Key 等敏感信息
     * </p>
     *
     * @return 所有模型配置DTO列表
     */
    List<AiModelConfigDTO> listAllModelsDTO();
}
