package com.n1etzsch3.novi.aiconfig.factory;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.n1etzsch3.novi.aiconfig.service.AiModelConfigService;
import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

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

        log.info("Creating new ChatModel with configuration: {}", activeModel.getModelName());

        ChatModel chatModel;
        // Determine whether to use DashScope or OpenAI based on model configuration
        if (isDashScopeModel(activeModel)) {
            chatModel = createDashScopeChatModel(activeModel);
        } else {
            chatModel = createOpenAiChatModel(activeModel);
        }

        // Update cache
        cachedChatModel = chatModel;
        cachedModelId = activeModel.getId();

        log.info("Successfully created and cached ChatModel for: {}", activeModel.getModelName());
        return chatModel;
    }

    /**
     * Create ChatModel by model name (for per-request model selection).
     * <p>
     * Unlike the no-arg version, this does NOT cache the model instance
     * since it's meant for per-request usage where different requests may
     * use different models.
     * </p>
     *
     * @param modelName the model name to use, if null/blank falls back to active
     *                  model
     * @return ChatModel instance
     */
    public ChatModel createChatModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return createChatModel(); // fallback to active model
        }

        AiModelConfig config = aiModelConfigService.getModelByName(modelName);
        if (config == null) {
            log.warn("Requested model '{}' not found, falling back to active model", modelName);
            return createChatModel();
        }

        log.debug("Creating ChatModel for requested model: {}", modelName);
        if (isDashScopeModel(config)) {
            return createDashScopeChatModel(config);
        } else {
            return createOpenAiChatModel(config);
        }
    }

    private boolean isDashScopeModel(AiModelConfig config) {
        // Check if using OpenAI-compatible mode first
        // compatible-mode URLs should use OpenAiCompatibleChatModel, not
        // DashScopeChatModel
        // because DashScopeChatModel uses internal native API paths and ignores custom
        // base_url
        if (config.getBaseUrl() != null && config.getBaseUrl().contains("compatible-mode")) {
            log.debug("Detected compatible-mode URL, will use OpenAI-compatible client: {}", config.getBaseUrl());
            return false;
        }

        // Simple heuristic: check if the model name or description implies
        // DashScope/Qwen - use native DashScope API
        // Or if the base URL matches DashScope's URL (but not compatible-mode)
        if (config.getBaseUrl() != null && config.getBaseUrl().contains("dashscope")) {
            return true;
        }
        if (config.getModelName() != null && (config.getModelName().toLowerCase().contains("qwen")
                || config.getModelName().toLowerCase().contains("llama"))) {
            // This is a loose check, but typically DashScope provides Qwen.
            // A better way would be adding a 'provider' column to the database.
            // For now, let's assume if it's NOT explicitly OpenAI-like (or if it matches
            // DashScope traits), it's DashScope?
            // Actually, safe bet: if base URL is dashscope, use DashScope.
            // If base URL has 'volces' (Doubao), use OpenAI.
            return true;
        }
        return false;
    }

    private ChatModel createDashScopeChatModel(AiModelConfig activeModel) {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(activeModel.getApiKey())
                .build();

        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(activeModel.getModelName())
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .build();
    }

    private ChatModel createOpenAiChatModel(AiModelConfig activeModel) {
        log.info("Creating OpenAI-compatible ChatModel for: {}, Base URL: {}", activeModel.getModelName(),
                activeModel.getBaseUrl());

        // Use custom WebClient-based implementation to avoid spring-ai-openai version
        // conflicts
        return new com.n1etzsch3.novi.aiconfig.client.OpenAiCompatibleChatModel(
                activeModel.getBaseUrl(),
                activeModel.getApiKey(),
                activeModel.getModelName(),
                activeModel.getCompletionsPath(),
                activeModel.getEnableThinking() != null && activeModel.getEnableThinking());
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
