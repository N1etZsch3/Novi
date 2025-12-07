package com.n1etzsch3.novi.aiconfig.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom OpenAI-compatible ChatModel implementation using WebClient.
 * This avoids the Spring AI version conflict by not using spring-ai-openai
 * module.
 * 
 * @author N1etzsch3
 */
@Slf4j
public class OpenAiCompatibleChatModel implements ChatModel {

    private final WebClient webClient;
    private final String model;
    private final String apiKey;
    private final String completionsPath;
    private final boolean enableThinking;

    public OpenAiCompatibleChatModel(String baseUrl, String apiKey, String model, String completionsPath,
            boolean enableThinking) {
        this.apiKey = apiKey;
        this.model = model;
        // Use database-configured path, fall back to OpenAI standard if not provided
        this.completionsPath = (completionsPath != null && !completionsPath.isBlank()) ? completionsPath
                : "/v1/chat/completions";
        this.enableThinking = enableThinking;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        log.info("Created OpenAI-compatible ChatModel for model: {}, baseUrl: {}, path: {}, enableThinking: {}", model,
                baseUrl,
                this.completionsPath, this.enableThinking);
    }

    public OpenAiCompatibleChatModel(String baseUrl, String apiKey, String model, String completionsPath) {
        this(baseUrl, apiKey, model, completionsPath, false);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Map<String, Object> request = buildRequest(prompt, false);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(completionsPath)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseResponse(response);
        } catch (Exception e) {
            log.error("Error calling OpenAI-compatible API", e);
            throw new RuntimeException("Failed to call OpenAI-compatible API: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        Map<String, Object> request = buildRequest(prompt, true);

        return webClient.post()
                .uri(completionsPath)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchangeToFlux(response -> {
                    if (response.statusCode().isError()) {
                        return response.createException().flatMapMany(Flux::error);
                    }
                    log.info("Stream Connected. Status: {}, Headers: {}", response.statusCode(),
                            response.headers().asHttpHeaders());
                    return response.bodyToFlux(String.class);
                })
                .doOnSubscribe(s -> log.info("Stream subscription started"))
                .filter(chunk -> {
                    String trimmed = chunk.trim();
                    return !trimmed.isEmpty() && !"[DONE]".equals(trimmed);
                })
                .map(chunk -> {
                    String trimmed = chunk.trim();
                    // In case some implementations still send "data:" prefix
                    if (trimmed.startsWith("data:")) {
                        return trimmed.substring(5).trim();
                    }
                    return trimmed;
                })
                .<ChatResponse>handle((chunk, sink) -> {
                    ChatResponse response = parseStreamChunk(chunk);
                    if (response != null) {
                        sink.next(response);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error in stream", e);
                    return Flux.error(new RuntimeException("Stream error: " + e.getMessage(), e));
                });
    }

    private Map<String, Object> buildRequest(Prompt prompt, boolean stream) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("stream", stream);

        // Add enable_thinking parameter if enabled
        if (enableThinking) {
            // Some providers might strip unknown top-level parameters, but for
            // OpenAI-compatible
            // implementations that support thinking, this is usually where it goes (or in
            // extra_body which is flattened here)
            // However, the user example showed `extra_body`. In OpenAI python client
            // `extra_body` merges into top level.
            // But specifically for this API, let's try putting it at top level as that's
            // standard for custom params.
            request.put("enable_thinking", true);
        }

        List<Map<String, String>> messages = prompt.getInstructions().stream()
                .map(this::convertMessage)
                .collect(Collectors.toList());
        request.put("messages", messages);

        return request;
    }

    private Map<String, String> convertMessage(Message message) {
        Map<String, String> msg = new HashMap<>();
        if (message instanceof UserMessage) {
            msg.put("role", "user");
            msg.put("content", message.getText());
        } else if (message instanceof AssistantMessage) {
            msg.put("role", "assistant");
            msg.put("content", message.getText());
        } else {
            msg.put("role", "system");
            msg.put("content", message.getText());
        }
        return msg;
    }

    private ChatResponse parseResponse(Map<String, Object> response) {
        if (response == null) {
            return new ChatResponse(Collections.emptyList());
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new ChatResponse(Collections.emptyList());
        }

        Map<String, Object> choice = choices.get(0);
        Map<String, Object> messageData = (Map<String, Object>) choice.get("message");
        String content = messageData != null ? (String) messageData.get("content") : "";
        Object reasoningContent = messageData != null ? messageData.get("reasoning_content") : null;

        Map<String, Object> metadata = new HashMap<>();
        if (reasoningContent != null) {
            metadata.put("reasoning_content", reasoningContent);
        }

        AssistantMessage assistantMessage = new AssistantMessage(content, metadata);
        Generation generation = new Generation(assistantMessage);

        return new ChatResponse(Collections.singletonList(generation));
    }

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    private ChatResponse parseStreamChunk(String json) {
        try {
            // Log raw chunk for debugging
            // log.debug("Raw stream chunk: {}", json);

            // Check for [DONE] message
            if ("[DONE]".equals(json.trim())) {
                return null;
            }

            // Parse JSON using ObjectMapper
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(json);

            // Navigate to choices[0].delta.content
            if (rootNode.has("choices") && rootNode.get("choices").isArray() && rootNode.get("choices").size() > 0) {
                com.fasterxml.jackson.databind.JsonNode choice = rootNode.get("choices").get(0);
                if (choice.has("delta")) {
                    com.fasterxml.jackson.databind.JsonNode delta = choice.get("delta");
                    String content = "";

                    // 1. Handle Reasoning Content
                    if (delta.has("reasoning_content") && !delta.get("reasoning_content").isNull()) {
                        String reasoning = delta.get("reasoning_content").asText();
                        if (!reasoning.isEmpty()) {
                            // Special handling: We need to pass this up.
                            // Spring AI ChatResponse doesn't have a dedicated field for this.
                            // We will use metadata or a special prefix/wrapper?
                            // Better approach: Since we are controlling the ChatService consumer,
                            // Better approach: Use AssistantMessage metadata
                            Map<String, Object> metadata = new HashMap<>();
                            metadata.put("reasoning_content", reasoning);
                            AssistantMessage msg = new AssistantMessage("", metadata);
                            Generation gen = new Generation(msg);
                            return new ChatResponse(Collections.singletonList(gen));
                        }
                    }

                    // 2. Handle Standard Content
                    if (delta.has("content") && !delta.get("content").isNull()) {
                        content = delta.get("content").asText();
                    }

                    if (content != null && !content.isEmpty()) {
                        AssistantMessage assistantMessage = new AssistantMessage(content);
                        Generation generation = new Generation(assistantMessage);
                        return new ChatResponse(Collections.singletonList(generation));
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("Error parsing stream chunk: {}", json, e);
            return null;
        }
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return null;
    }
}
