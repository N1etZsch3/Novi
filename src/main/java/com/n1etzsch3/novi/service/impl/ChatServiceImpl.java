package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.StreamEvent;
import com.n1etzsch3.novi.service.ChatService;
import com.n1etzsch3.novi.utils.LoginUserContext;
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

        String AIResponse = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        log.info("AI 回复用户 {} 在会话 {} 的消息: {}", userId, request.getSessionId(), AIResponse);

        return new ChatResponse(AIResponse, request.getSessionId());
    }

    /**
     * 处理流式消息 (修改后)
     * * @return 一个 Flux<String>，其中每个 String 都是一个序列化后的 StreamEvent JSON
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
        // 使用 Flux.defer() 来包装 chatClient 的调用
        Flux<StreamEvent> contentStream = Flux.defer(() -> {

            // *** 关键修复 (A): 在工作线程上设置 ThreadLocal ***
            // 这将在订阅时（即在 'boundedElastic-2' 线程上）执行
            LoginUserContext.setUserId(userId);
            log.info("已在工作线程 {} 上下文为用户 {} 设置 ThreadLocal", Thread.currentThread().getName(), userId);

            // 现在的 chatClient 调用将在这个已设置好 ThreadLocal 的线程上执行
            return chatClient.prompt()
                    .user(userMessage)
                    // *** 这是您添加的正确代码 ***
                    .advisors(advisorSpec -> advisorSpec
                            .param(ChatMemory.CONVERSATION_ID, sessionIdToUse))
                    .stream()
                    .content() // 这返回 Flux<String> (AI 的内容块)
                    .map(StreamEvent::content); // 将每个 String 块 转换为 StreamEvent.content(chunk)

        }).doFinally(signalType -> {
            // *** 关键修复 (B): 无论成功、失败或取消，都清理工作线程的 ThreadLocal ***
            log.info("流处理完毕 ({}). 清理工作线程 {} 上的 ThreadLocal", signalType, Thread.currentThread().getName());
            LoginUserContext.clear();
        });

        // --- 4. 拼接流，并序列化为 JSON 字符串 ---
        return Flux.concat(metadataStream, contentStream)
                .<String>handle((event, sink) -> {
                    try {
                        sink.next(objectMapper.writeValueAsString(event));
                    } catch (JsonProcessingException e) {
                        log.error("序列化 StreamEvent 失败", e);
                        sink.error(new RuntimeException("Stream serialization error", e));
                    }
                })
                .doOnError(e -> log.error("流式处理中发生错误，用户: {}", userId, e))
                // 注意: 这个 doOnComplete 现在只记录 Service 层的完成
                .doOnComplete(() -> log.info("用户 {} 的流式响应序列化完成，会话: {}", userId, sessionIdToUse));
    }
}
