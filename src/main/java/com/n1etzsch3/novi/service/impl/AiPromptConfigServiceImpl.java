package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.mapper.AiPromptConfigMapper;
import com.n1etzsch3.novi.pojo.entity.AiPromptConfig;
import com.n1etzsch3.novi.service.AiPromptConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiPromptConfigServiceImpl implements AiPromptConfigService {

    private final AiPromptConfigMapper aiPromptConfigMapper;

    @Override
    public String getSystemPromptTemplate() {
        AiPromptConfig config = aiPromptConfigMapper.findByKey("system_prompt_template");
        if (config != null) {
            return config.getConfigValue();
        }
        log.warn("未找到系统提示词配置 system_prompt_template，使用默认值");
        return "You are a helpful assistant.";
    }

    @Override
    public String getPersonalityDescription(String personalityKey) {
        AiPromptConfig config = aiPromptConfigMapper.findByKey(personalityKey);
        if (config != null) {
            return config.getConfigValue();
        }
        // 如果找不到特定的，尝试找默认的
        if (!"personality_default".equals(personalityKey)) {
            config = aiPromptConfigMapper.findByKey("personality_default");
            if (config != null) {
                return config.getConfigValue();
            }
        }
        return "随性自然，说话直爽。";
    }
}
