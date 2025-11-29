package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.domain.po.AiModelConfig;

import java.util.List;

/**
 * AI模型配置服务接口
 * <p>
 * 用于管理AI模型配置，支持模型热切换。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
public interface AiModelConfigService {

    /**
     * 获取当前激活的模型配置
     *
     * @return 当前激活的模型配置，如果没有则返回 null
     */
    AiModelConfig getActiveModel();

    /**
     * 切换到指定的模型
     *
     * @param modelId 要激活的模型ID
     * @return 切换是否成功
     */
    boolean switchModel(Long modelId);

    /**
     * 列出所有可用的模型配置
     *
     * @return 所有模型配置列表
     */
    List<AiModelConfig> listAllModels();

    /**
     * 添加新的模型配置
     *
     * @param config 模型配置
     * @return 添加是否成功
     */
    boolean addModel(AiModelConfig config);

    /**
     * 删除指定的模型配置
     *
     * @param modelId 模型ID
     * @return 删除是否成功
     */
    boolean deleteModel(Long modelId);

    /**
     * 更新模型配置
     *
     * @param config 模型配置
     * @return 更新是否成功
     */
    boolean updateModel(AiModelConfig config);
}
