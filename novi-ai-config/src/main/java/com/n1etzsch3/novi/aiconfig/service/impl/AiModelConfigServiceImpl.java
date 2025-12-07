package com.n1etzsch3.novi.aiconfig.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.n1etzsch3.novi.aiconfig.pojo.dto.AiModelConfigDTO;
import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;
import com.n1etzsch3.novi.aiconfig.mapper.AiModelConfigMapper;
import com.n1etzsch3.novi.aiconfig.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI模型配置服务实现类
 * <p>
 * 提供模型查询和切换功能。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiModelConfigServiceImpl implements AiModelConfigService {

    private final AiModelConfigMapper aiModelConfigMapper;

    @Override
    public AiModelConfigDTO getActiveModelDTO() {
        AiModelConfig activeModel = getActiveModel();
        return convertToDTO(activeModel);
    }

    @Override
    public List<AiModelConfigDTO> listAllModelsDTO() {
        List<AiModelConfig> models = aiModelConfigMapper.selectList(null);
        return models.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean switchModelByName(String modelName) {
        // 0. 获取当前激活的模型（用于日志）
        AiModelConfig currentActiveModel = getActiveModel();
        String oldModelName = (currentActiveModel != null) ? currentActiveModel.getModelName() : "None";

        // 1. 检查目标模型是否存在
        Long count = aiModelConfigMapper.selectCount(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getModelName, modelName));

        if (count == 0) {
            log.error("Model with name {} not found", modelName);
            return false;
        }

        // 2. 原子性切换模型
        // 使用 CASE WHEN 语句一次性更新所有行，避免并发导致的多个激活模型问题
        aiModelConfigMapper.activateModelByName(modelName);

        log.info("Successfully switched model from [{}] to [{}]", oldModelName, modelName);
        return true;
    }

    /**
     * 获取当前激活的模型配置（内部使用）
     * <p>
     * 此方法返回完整的配置信息，仅供系统内部组件使用
     * </p>
     *
     * @return 当前激活的模型配置，如果没有则返回 null
     */
    @Override
    public AiModelConfig getActiveModel() {
        // Use selectList instead of selectOne to prevent TooManyResultsException
        List<AiModelConfig> activeModels = aiModelConfigMapper.selectList(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getIsActive, true));

        if (activeModels == null || activeModels.isEmpty()) {
            log.warn("No active AI model found in database. Attempting to auto-activate the first available model.");

            // Query for any model
            List<AiModelConfig> allModels = aiModelConfigMapper
                    .selectList(new LambdaQueryWrapper<AiModelConfig>().last("LIMIT 1"));

            if (allModels != null && !allModels.isEmpty()) {
                AiModelConfig fallbackModel = allModels.get(0);
                log.info("Auto-activating model: {}", fallbackModel.getModelName());

                // Activate this model
                fallbackModel.setIsActive(true);
                aiModelConfigMapper.updateById(fallbackModel);

                // Ensure others are deactivated if relying on activateModelByName logic
                // elsewhere?
                // But for safety, let's just use the mapper's atomic activation if possible.
                // Or just updating this one is enough if we assume others are 0.
                // Better use activateModelByName to be clean, but that requires name.
                aiModelConfigMapper.activateModelByName(fallbackModel.getModelName());

                // Refetch to be sure or just return modified object (updated active status)
                // Returning modified object is faster but might miss DB triggers (unlikely
                // here).
                // Let's just return the object with true.
                fallbackModel.setIsActive(true);
                return fallbackModel;
            }

            log.error("No AI models configured in database at all.");
            return null;
        }

        if (activeModels.size() > 1) {
            log.warn("Found {} active AI models in database. Using the first one: {}",
                    activeModels.size(), activeModels.get(0).getModelName());
        }

        return activeModels.get(0);
    }

    @Override
    public AiModelConfig getModelByName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return null;
        }
        return aiModelConfigMapper.selectOne(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getModelName, modelName));
    }

    /**
     * 将 AiModelConfig 转换为 AiModelConfigDTO
     * <p>
     * 只包含安全字段，不暴露 API Key 等敏感信息
     * </p>
     *
     * @param config 模型配置
     * @return 模型配置DTO，如果config为null则返回null
     */
    private AiModelConfigDTO convertToDTO(AiModelConfig config) {
        if (config == null) {
            return null;
        }
        return new AiModelConfigDTO(
                config.getId(),
                config.getModelName(),
                config.getDescription(),
                config.getIsActive(),
                config.getEnableThinking());
    }
}
