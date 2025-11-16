package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.StreamEvent;
import com.n1etzsch3.novi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper; // <-- 注入 ObjectMapper

    @Override
    public ChatResponse handleCallMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // --- 修复 2a：添加 sessionId 管理 ---
        final String sessionIdToUse = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        log.info("AI 回复用户 {} 在会话 {} 的消息 (阻塞式): {}", userId, sessionIdToUse, userMessage);

        String AIResponse = chatClient.prompt()
                .user(userMessage)
                // --- 修复 2a：将会话 ID 传递给 Advisor ---
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, sessionIdToUse))
                .call()
                .content();

        // --- 修复 2a：返回正确的（可能是新生成的）sessionId ---
        return new ChatResponse(AIResponse, sessionIdToUse);
    }

    /**
     * 处理流式消息 (修改后)
     * * @return 一个 Flux<String>，其中每个 String 都是一个序列化后的 StreamEvent JSON
     */
    @Override
    public Flux<String> handleStreamMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();
        log.info("收到用户 {} 在会话 {} 的消息: {}，流式调用", userId, request.getSessionId(), userMessage);

        // --- 1. 会话 ID 管理 ---
        // 确保有一个 sessionId。如果请求中没有，就创建一个。
        final String sessionIdToUse = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        // --- 2. 创建元数据事件 ---
        // 这是将发送的第一个事件，告诉客户端 sessionId。
        StreamEvent metadataEvent = StreamEvent.metadata(sessionIdToUse);
        Flux<StreamEvent> metadataStream = Flux.just(metadataEvent);


        // --- 3. 创建 AI 内容流 ---
        Flux<StreamEvent> contentStream = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, sessionIdToUse))
                .stream()
                .content() // 这返回 Flux<String> (AI 的内容块)
                .map(StreamEvent::content); // 将每个 String 块 转换为 StreamEvent.content(chunk)

        log.info(contentStream.toString());

        // --- 4. 拼接流，并序列化为 JSON 字符串 ---
        // Flux.concat 确保 metadataStream 首先被发送，然后才是 contentStream
        return Flux.concat(metadataStream, contentStream)
                .<String>handle((event, sink) -> {
                    try {
                        // 将 StreamEvent 对象 序列化为 JSON 字符串
                        sink.next(objectMapper.writeValueAsString(event));
                    } catch (JsonProcessingException e) {
                        // 如果序列化失败，抛出一个运行时异常，这将终止流
                        log.error("序列化 StreamEvent 失败", e);
                        sink.error(new RuntimeException("Stream serialization error", e));
                    }
                })
                .doOnError(e -> log.error("流式处理中发生错误，用户: {}", userId, e))
                .doOnComplete(() -> log.info("用户 {} 的流式响应完成，会话: {}", userId, sessionIdToUse));
    }
}
