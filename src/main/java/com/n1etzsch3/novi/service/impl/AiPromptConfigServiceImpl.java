package com.n1etzsch3.novi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.mapper.AiPromptConfigMapper;
import com.n1etzsch3.novi.domain.po.AiPromptConfig;
import com.n1etzsch3.novi.service.AiPromptConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 提示词配置服务实现类
 * <p>
 * 实现管理和检索 AI 提示词配置的逻辑。
 * 包含默认性格和语气的回退机制。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiPromptConfigServiceImpl implements AiPromptConfigService {

    private final AiPromptConfigMapper aiPromptConfigMapper;

    @Override
    public String getSystemPromptTemplate() {
        AiPromptConfig config = aiPromptConfigMapper.selectById("system_prompt_template");
        if (config != null) {
            return config.getConfigValue();
        }
        log.warn("System prompt config 'system_prompt_template' not found, using default.");
        return "You are a helpful assistant.";
    }

    @Override
    public String getConfigValue(String key) {
        AiPromptConfig config = aiPromptConfigMapper.selectById(key);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getPersonalityDescription(String personalityKey) {
        AiPromptConfig config = aiPromptConfigMapper.selectById(personalityKey);
        if (config != null) {
            return config.getConfigValue();
        }
        // 如果未找到特定性格，则回退到默认性格
        if (!"personality_default".equals(personalityKey)) {
            config = aiPromptConfigMapper.selectById("personality_default");
            if (config != null) {
                return config.getConfigValue();
            }
        }
        return "随性自然，说话直爽。";
    }

    @Override
    public String getToneStyleDescription(String toneStyleKey) {
        AiPromptConfig config = aiPromptConfigMapper.selectById(toneStyleKey);
        if (config != null) {
            return config.getConfigValue();
        }
        // 回退到默认语气
        if (!"tone_default".equals(toneStyleKey)) {
            config = aiPromptConfigMapper.selectById("tone_default");
            if (config != null) {
                return config.getConfigValue();
            }
        }
        return "正常语气";
    }

    @Override
    public void addConfig(AiPromptConfig config) {
        if (aiPromptConfigMapper.selectById(config.getConfigKey()) != null) {
            log.warn("Attempted to add existing config key: {}", config.getConfigKey());
            throw new RuntimeException("Config key already exists: " + config.getConfigKey());
        }
        aiPromptConfigMapper.insert(config);
        log.info("Added new prompt config: {}", config.getConfigKey());
    }

    @Override
    public void removeConfig(String configKey) {
        aiPromptConfigMapper.deleteById(configKey);
        log.info("Removed prompt config: {}", configKey);
    }

    @Override
    public java.util.List<AiPromptConfig> listConfigsByType(Integer type) {
        return aiPromptConfigMapper.selectList(
                new LambdaQueryWrapper<AiPromptConfig>()
                        .eq(AiPromptConfig::getConfigType, type));
    }
}
