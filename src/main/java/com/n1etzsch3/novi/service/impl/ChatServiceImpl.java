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
    private final ObjectMapper objectMapper;

    @Override
    public ChatResponse handleCallMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        final String sessionIdToUse = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        // 阻塞式调用不需要修改，因为 JwtAuthInterceptor 已经设置了 ThreadLocal，
        // 并且这个调用链在同一个 T1 线程中执行。
        String AIResponse = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, sessionIdToUse))
                .call()
                .content();

        log.info("AI 回复用户 {} 在会话 {} 的消息: {}", userId, request.getSessionId(), AIResponse);

        return new ChatResponse(AIResponse, request.getSessionId());
    }

    /**
     * 处理流式消息 (已重构)
     * 1. 移除了手动的 ThreadLocal set/clear (方案B会自动处理)
     * 2. 增加了 onErrorResume (健壮性修复)
     */
    @Override
    public Flux<String> handleStreamMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();
        log.info("收到用户 {} 在会话 {} 的消息: {}，流式调用", userId, request.getSessionId(), userMessage);

        final String sessionIdToUse = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        StreamEvent metadataEvent = StreamEvent.metadata(sessionIdToUse);
        Flux<StreamEvent> metadataStream = Flux.just(metadataEvent);


        // --- 3. 【修改】创建 AI 内容流 (使用 Flux.defer) ---
        Flux<StreamEvent> contentStream = Flux.defer(() -> {

            // *** 关键修复 (A) 和 (B) 已移除 ***
            // LoginUserContext.setUserId(userId); // <-- 已移除，由方案B自动处理
            // log.info(...) // <-- 已移除

            // chatClient 的调用现在将由 context-propagation "自动装配" ThreadLocal
            return chatClient.prompt()
                    .user(userMessage)
                    .advisors(advisorSpec -> advisorSpec
                            .param(ChatMemory.CONVERSATION_ID, sessionIdToUse))
                    .stream()
                    .content()
                    .map(StreamEvent::content); // 将每个 String 块 转换为 StreamEvent.content(chunk)

        });
        // .doFinally(...) // <-- 已移除，由 JwtAuthInterceptor 和 Accessor 自动清理

        // --- 4. 拼接流，序列化，并添加健壮性修复 (onErrorResume) ---
        return Flux.concat(metadataStream, contentStream)
                .<String>handle((event, sink) -> {
                    try {
                        sink.next(objectMapper.writeValueAsString(event));
                    } catch (JsonProcessingException e) {
                        log.error("序列化 StreamEvent 失败", e);
                        sink.error(new RuntimeException("Stream serialization error", e));
                    }
                })
                // --- 【新增】研究报告第四部分：健壮性修复 ---
                .onErrorResume(e -> {
                    // 捕获所有上游异常 (包括 NoviMemoryRepository 的 NPE 或 AI 模型的错误)
                    log.error("流式处理中发生严重错误，用户: {}", userId, e);

                    // 将异常转换为一个 StreamEvent.error 事件
                    // 这样客户端就不会卡住，而是能收到一个明确的错误提示
                    StreamEvent errorEvent = StreamEvent.error("服务器在处理您的请求时遇到问题，请稍后再试。");
                    try {
                        // 返回包含错误事件的单个Flux，然后正常结束
                        return Flux.just(objectMapper.writeValueAsString(errorEvent));
                    } catch (JsonProcessingException jsonEx) {
                        // 如果连序列化错误事件都失败了，只能返回一个空流
                        return Flux.empty();
                    }
                })
                .doOnComplete(() -> log.info("用户 {} 的流式响应序列化完成，会话: {}", userId, sessionIdToUse));
    }
}