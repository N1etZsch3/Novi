package com.n1etzsch3.novi.service;

public interface AiPromptConfigService {

    /**
     * 获取系统提示词模板
     */
    String getSystemPromptTemplate();

    /**
     * 获取性格描述
     * 
     * @param personalityKey 性格Key (e.g., "personality_default")
     */
    String getPersonalityDescription(String personalityKey);
}
