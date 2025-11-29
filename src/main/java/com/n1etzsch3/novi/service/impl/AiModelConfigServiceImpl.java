package com.n1etzsch3.novi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.n1etzsch3.novi.domain.po.AiModelConfig;
import com.n1etzsch3.novi.mapper.AiModelConfigMapper;
import com.n1etzsch3.novi.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI模型配置服务实现类
 * <p>
 * 实现模型配置的管理和切换逻辑。
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
    public AiModelConfig getActiveModel() {
        AiModelConfig activeModel = aiModelConfigMapper.selectOne(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getIsActive, true));

        if (activeModel == null) {
            log.warn("No active AI model found in database, please configure one.");
        }

        return activeModel;
    }

    @Override
    @Transactional
    public boolean switchModel(Long modelId) {
        // 1. 检查目标模型是否存在
        AiModelConfig targetModel = aiModelConfigMapper.selectById(modelId);
        if (targetModel == null) {
            log.error("Model with id {} not found", modelId);
            return false;
        }

        // 2. 将所有模型设置为非激活状态
        aiModelConfigMapper.update(null,
                new LambdaUpdateWrapper<AiModelConfig>()
                        .set(AiModelConfig::getIsActive, false));

        // 3. 激活目标模型
        aiModelConfigMapper.update(null,
                new LambdaUpdateWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getId, modelId)
                        .set(AiModelConfig::getIsActive, true));

        log.info("Successfully switched to model: {} (ID: {})", targetModel.getModelName(), modelId);
        return true;
    }

    @Override
    public List<AiModelConfig> listAllModels() {
        return aiModelConfigMapper.selectList(null);
    }

    @Override
    public boolean addModel(AiModelConfig config) {
        try {
            // 如果是第一个模型，自动设为激活状态
            long count = aiModelConfigMapper.selectCount(null);
            if (count == 0) {
                config.setIsActive(true);
            } else {
                config.setIsActive(false);
            }

            aiModelConfigMapper.insert(config);
            log.info("Added new AI model: {}", config.getModelName());
            return true;
        } catch (Exception e) {
            log.error("Failed to add model: {}", config.getModelName(), e);
            return false;
        }
    }

    @Override
    public boolean deleteModel(Long modelId) {
        try {
            AiModelConfig model = aiModelConfigMapper.selectById(modelId);
            if (model == null) {
                log.error("Model with id {} not found", modelId);
                return false;
            }

            // 不允许删除激活的模型
            if (Boolean.TRUE.equals(model.getIsActive())) {
                log.error("Cannot delete active model: {}", model.getModelName());
                return false;
            }

            aiModelConfigMapper.deleteById(modelId);
            log.info("Deleted model: {}", model.getModelName());
            return true;
        } catch (Exception e) {
            log.error("Failed to delete model with id: {}", modelId, e);
            return false;
        }
    }

    @Override
    public boolean updateModel(AiModelConfig config) {
        try {
            aiModelConfigMapper.updateById(config);
            log.info("Updated model: {}", config.getModelName());
            return true;
        } catch (Exception e) {
            log.error("Failed to update model: {}", config.getModelName(), e);
            return false;
        }
    }
}
