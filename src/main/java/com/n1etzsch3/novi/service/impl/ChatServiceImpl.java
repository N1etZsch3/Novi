package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.mapper.ChatSessionMapper;
import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.StreamEvent;
import com.n1etzsch3.novi.pojo.entity.ChatSession;
import com.n1etzsch3.novi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.util.StringUtils; // 建议引入这个工具类

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ChatSessionMapper chatSessionMapper;

    /**
     * 辅助方法：创建复合键
     * 格式: "userId:sessionId"
     * 作用: 解决 Stream 流式异步调用中 ThreadLocal 无法传递 userId 的问题
     */
    private String createCompositeKey(Long userId, String sessionId) {
        return userId + ":" + sessionId;
    }

    /**
     * 核心方法：获取或创建会话 (并同步到数据库)
     */
    private String getOrCreateSession(Long userId, String requestedSessionId, String messageContent) {
        // 1. 判断是否是新会话 (使用 StringUtils 以防空字符串)
        boolean isNewSession = !StringUtils.hasText(requestedSessionId);

        String finalSessionId = isNewSession ? UUID.randomUUID().toString() : requestedSessionId;

        if (isNewSession) {
            // --- 创建新会话 ---
            ChatSession session = new ChatSession();
            session.setId(finalSessionId);
            session.setUserId(userId);

            // 生成标题：取前 20 个字
            // @TODO：调用AI，总结性生成一个标题
            String title = messageContent != null && messageContent.length() > 20 ?
                    messageContent.substring(0, 20) + "..." : messageContent;
            session.setTitle(title);

            chatSessionMapper.createSession(session);
            log.info("创建新会话 (DB): {}, 标题: {}", finalSessionId, title);
        } else {
            // --- 更新旧会话时间 ---
            // 这里判断 update 的影响行数，如果为0说明前端传的 ID 数据库里没有
            int rows = chatSessionMapper.updateLastActiveTime(finalSessionId);
            if (rows == 0) {
                // 容错处理：如果数据库没这个 ID，就当作新会话创建
                log.warn("会话 {} 在数据库不存在，重新创建", finalSessionId);
                ChatSession session = new ChatSession();
                session.setId(finalSessionId);
                session.setUserId(userId);
                session.setTitle("恢复的会话");
                chatSessionMapper.createSession(session);
            }
        }

        return finalSessionId;
    }

    @Override
    public ChatResponse handleCallMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // 1. 获取或创建 sessionId (同步数据库)
        String sessionIdToUse = getOrCreateSession(userId, request.getSessionId(), userMessage);

        // 2. 构建复合键 (userId:sessionId) 用于 AI 上下文
        final String compositeKey = createCompositeKey(userId, sessionIdToUse);

        log.info("阻塞式调用 - 用户: {}, 会话: {}", userId, sessionIdToUse);

        String AIResponse = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        // 关键：传递复合键，确保 ChatMemory 能解析出 userId
                        .param(ChatMemory.CONVERSATION_ID, compositeKey))
                .call()
                .content();

        return new ChatResponse(AIResponse, sessionIdToUse);
    }

    @Override
    public Flux<String> handleStreamMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // --- 修正点 1：调用 getOrCreateSession ---
        // 之前这里直接生成了 UUID，导致没有入库。现在统一逻辑。
        final String sessionIdToUse = getOrCreateSession(userId, request.getSessionId(), userMessage);

        // --- 修正点 2：构建复合键 ---
        final String compositeKey = createCompositeKey(userId, sessionIdToUse);

        log.info("流式调用 - 用户: {}, 会话: {}, 复合键: {}", userId, sessionIdToUse, compositeKey);

        // 1. 创建元数据事件 (告诉前端真实的 sessionId)
        StreamEvent metadataEvent = StreamEvent.metadata(sessionIdToUse);
        Flux<StreamEvent> metadataStream = Flux.just(metadataEvent);

        // 2. 创建 AI 内容流
        Flux<StreamEvent> contentStream = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        // 关键：传递复合键给 ChatMemoryAdvisor
                        .param(ChatMemory.CONVERSATION_ID, compositeKey))
                .stream()
                .content()
                .map(StreamEvent::content);

        // 3. 拼接并序列化
        return Flux.concat(metadataStream, contentStream)
                .<String>handle((event, sink) -> {
                    try {
                        sink.next(objectMapper.writeValueAsString(event));
                    } catch (JsonProcessingException e) {
                        log.error("序列化失败", e);
                        sink.error(new RuntimeException("Stream serialization error", e));
                    }
                })
                .doOnError(e -> log.error("流式处理错误, 用户: {}", userId, e))
                .doOnComplete(() -> log.info("流式响应完成, 会话: {}", sessionIdToUse));
    }
}