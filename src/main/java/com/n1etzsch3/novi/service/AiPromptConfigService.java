package com.n1etzsch3.novi.service;

/**
 * AI 提示词配置服务接口
 * <p>
 * 定义管理 AI 提示词配置的操作，包括系统提示词、
 * 性格和语气风格。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
public interface AiPromptConfigService {

    /**
     * 获取系统提示词模板。
     *
     * @return 系统提示词模板字符串。
     */
    String getSystemPromptTemplate();

    /**
     * 根据上下文类型获取对应的系统提示词模板
     * <p>
     * 用于不同场景下的智能提示词切换。
     * </p>
     *
     * @param contextType 上下文类型
     * @return 对应场景的系统提示词模板
     */
    String getSystemPromptByContext(com.n1etzsch3.novi.enums.PromptContextType contextType);

    /**
     * 根据 Key 获取配置值 (无回退)。
     *
     * @param key 配置 Key。
     * @return 配置值，如果不存在则返回 null。
     */
    String getConfigValue(String key);

    /**
     * 获取特定性格的描述。
     *
     * @param personalityKey 性格的 Key (例如: "personality_default")。
     * @return 性格描述。
     */
    String getPersonalityDescription(String personalityKey);

    /**
     * 获取特定语气风格的描述。
     *
     * @param toneStyleKey 语气风格的 Key (例如: "tone_humorous")。
     * @return 语气风格描述。
     */
    String getToneStyleDescription(String toneStyleKey);

    /**
     * 添加新配置。
     *
     * @param config 要添加的配置对象。
     */
    void addConfig(com.n1etzsch3.novi.domain.po.AiPromptConfig config);

    /**
     * 根据 Key 删除配置。
     *
     * @param configKey 要删除的配置的 Key。
     */
    void removeConfig(String configKey);

    /**
     * 根据类型列出配置。
     *
     * @param type 配置类型 (0:系统, 1:性格, 2:语气风格)。
     * @return 匹配类型的配置列表。
     */
    java.util.List<com.n1etzsch3.novi.domain.po.AiPromptConfig> listConfigsByType(Integer type);
}
