package com.n1etzsch3.novi.aiconfig.factory;

import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;
import com.n1etzsch3.novi.aiconfig.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * 动态 OpenAiChatModel 工厂
 * <p>
 * 根据数据库中的激活模型配置，动态创建 OpenAiChatModel 实例。
 * 支持模型热切换。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicChatModelFactory {

    private final AiModelConfigService aiModelConfigService;

    private volatile OpenAiChatModel cachedChatModel;
    private volatile Long cachedModelId;

    /**
     * 创建或获取 OpenAiChatModel 实例，使用当前激活的模型配置
     * <p>
     * 如果激活的模型没有变化，返回缓存的实例。
     * 如果模型已切换，创建新的实例。
     * </p>
     *
     * @return OpenAiChatModel 实例
     * @throws IllegalStateException 如果没有激活的模型配置
     */
    public synchronized OpenAiChatModel createChatModel() {
        AiModelConfig activeModel = aiModelConfigService.getActiveModel();

        if (activeModel == null) {
            log.error("No active AI model configuration found in database");
            throw new IllegalStateException("No active AI model found. Please configure one in ai_model_config table.");
        }

        // 如果模型已缓存且未变化，直接返回
        if (cachedChatModel != null && cachedModelId != null && cachedModelId.equals(activeModel.getId())) {
            log.debug("Returning cached ChatModel for: {}", activeModel.getModelName());
            return cachedChatModel;
        }

        log.info("Creating new ChatModel with configuration: {}", activeModel.getModelName());
        log.debug("Base URL: {}", activeModel.getBaseUrl());

        // 创建 OpenAiApi
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .baseUrl(activeModel.getBaseUrl())
                .apiKey(activeModel.getApiKey())
                .completionsPath(activeModel.getCompletionsPath());

        OpenAiApi openAiApi = apiBuilder.build();

        // 创建聊天选项
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(activeModel.getModelName())
                .build();

        // 创建 OpenAiChatModel
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(chatOptions)
                .build();

        // 记录是否需要启用thinking模式（这个标识在数据库中配置）
        // 注意：enable_thinking 需要在API请求时作为参数传递
        // 实际启用需要在调用API时传递，此处仅记录配置以便后续使用
        if (Boolean.TRUE.equals(activeModel.getEnableThinking())) {
            log.info("Model {} is configured to support thinking mode", activeModel.getModelName());
        }

        // 更新缓存
        cachedChatModel = chatModel;
        cachedModelId = activeModel.getId();

        log.info("Successfully created and cached ChatModel for: {}", activeModel.getModelName());
        return chatModel;
    }

    /**
     * 刷新模型缓存
     * <p>
     * 在模型切换后调用，强制下次获取时重新创建模型实例
     * </p>
     */
    public synchronized void refresh() {
        log.info("Refreshing ChatModel cache");
        cachedChatModel = null;
        cachedModelId = null;
    }
}
