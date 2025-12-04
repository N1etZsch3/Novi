package com.n1etzsch3.novi.aiconfig.factory;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.n1etzsch3.novi.aiconfig.advisor.ReasoningContentAdvisor;
import com.n1etzsch3.novi.aiconfig.service.AiModelConfigService;
import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Dynamic ChatModel Factory
 * <p>
 * Creates DashScopeChatModel instances based on active database configuration.
 * Supports hot-reloading.
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

    private volatile ChatModel cachedChatModel;
    private volatile Long cachedModelId;

    /**
     * Create or get ChatModel instance using active configuration
     *
     * @return ChatModel instance
     * @throws IllegalStateException if no active model found
     */
    public synchronized ChatModel createChatModel() {
        AiModelConfig activeModel = aiModelConfigService.getActiveModel();

        if (activeModel == null) {
            log.error("No active AI model configuration found in database");
            throw new IllegalStateException("No active AI model found. Please configure one in ai_model_config table.");
        }

        // Return cached instance if model hasn't changed
        if (cachedChatModel != null && cachedModelId != null && cachedModelId.equals(activeModel.getId())) {
            log.debug("Returning cached ChatModel for: {}", activeModel.getModelName());
            return cachedChatModel;
        }

        log.info("Creating new DashScopeChatModel with configuration: {}", activeModel.getModelName());

        ChatModel chatModel = createDashScopeChatModel(activeModel);

        // Update cache
        cachedChatModel = chatModel;
        cachedModelId = activeModel.getId();

        log.info("Successfully created and cached ChatModel for: {}", activeModel.getModelName());
        return chatModel;
    }

    private ChatModel createDashScopeChatModel(AiModelConfig activeModel) {
        // 1. Create DashScopeApi
        // Note: DashScopeApi constructor might vary, usually takes apiKey.
        // If using spring-ai-alibaba-starter, we might need to check available
        // constructors or builders.
        // Assuming standard usage: new DashScopeApi(apiKey) or builder.
        // Since we are manually creating it, we need to be careful with the API class.

        // Using builder if available, or constructor.
        // Based on common Spring AI patterns:
        // Create DashScopeApi using builder
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(activeModel.getApiKey())
                .build();

        // Create DashScopeChatOptions
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(activeModel.getModelName())
                .build();

        // Create DashScopeChatModel using builder
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * Refresh model cache
     */
    public synchronized void refresh() {
        log.info("Refreshing ChatModel cache");
        cachedChatModel = null;
        cachedModelId = null;
    }
}
