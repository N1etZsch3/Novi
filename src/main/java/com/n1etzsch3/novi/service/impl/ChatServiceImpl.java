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
     * 【内部记录类】用于同时传递 SessionId 和 Title
     */
    private record SessionInfo(String sessionId, String title) {}

    /**
     * 辅助方法：获取或创建会话
     * 【优化】：返回值从 String 改为 SessionInfo
     */
    private SessionInfo getOrCreateSession(Long userId, String requestedSessionId, String messageContent) {
        boolean isNewSession = !StringUtils.hasText(requestedSessionId);
        String finalSessionId = isNewSession ? UUID.randomUUID().toString() : requestedSessionId;
        String finalTitle = null; // 用于返回给前端的标题

        if (isNewSession) {
            // --- 创建新会话 ---
            ChatSession session = new ChatSession();
            session.setId(finalSessionId);
            session.setUserId(userId);

            // 生成标题：取前 20 个字
            finalTitle = messageContent != null && messageContent.length() > 20 ?
                    messageContent.substring(0, 20) + "..." : messageContent;
            session.setTitle(finalTitle);

            chatSessionMapper.createSession(session);
            log.info("创建新会话 (DB): {}, 标题: {}", finalSessionId, finalTitle);
        } else {
            // --- 更新旧会话时间 ---
            int rows = chatSessionMapper.updateLastActiveTime(finalSessionId);
            if (rows == 0) {
                // 容错：如果数据库没这个 ID，重建
                log.warn("会话 {} 在数据库不存在，重新创建", finalSessionId);
                finalTitle = "恢复的会话";
                ChatSession session = new ChatSession();
                session.setId(finalSessionId);
                session.setUserId(userId);
                session.setTitle(finalTitle);
                chatSessionMapper.createSession(session);
            } else {
                // 旧会话存在，虽然我们没改标题，但为了前端方便，
                // 这里可以选择去查一下标题返回，或者直接返回 null 让前端保持原样。
                // 这里简单起见，返回 null，表示标题未变更。
                finalTitle = null;
            }
        }

        return new SessionInfo(finalSessionId, finalTitle);
    }

    // --- 阻塞式调用 ---
    @Override
    public ChatResponse handleCallMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // 1. 获取 SessionInfo (包含 ID 和 Title)
        SessionInfo sessionInfo = getOrCreateSession(userId, request.getSessionId(), userMessage);
        String sessionIdToUse = sessionInfo.sessionId();

        // 2. 复合键逻辑
        final String compositeKey = createCompositeKey(userId, sessionIdToUse);

        // 3. AI 调用
        String AIResponse = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, compositeKey))
                .call()
                .content();

        // 4. 【关键】返回包含 title 的响应
        return new ChatResponse(AIResponse, sessionIdToUse, sessionInfo.title());
    }

    // --- 流式调用 ---
    @Override
    public Flux<String> handleStreamMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // 1. 获取 SessionInfo (包含 ID 和 Title)
        SessionInfo sessionInfo = getOrCreateSession(userId, request.getSessionId(), userMessage);
        String sessionIdToUse = sessionInfo.sessionId();

        // 2. 复合键逻辑
        final String compositeKey = createCompositeKey(userId, sessionIdToUse);

        // 3. 【关键】创建元数据事件，带上 title
        StreamEvent metadataEvent = StreamEvent.metadata(sessionIdToUse, sessionInfo.title());
        Flux<StreamEvent> metadataStream = Flux.just(metadataEvent);

        // 4. AI 内容流 (保持不变)
        Flux<StreamEvent> contentStream = chatClient.prompt()
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, compositeKey))
                .stream()
                .content()
                .map(StreamEvent::content);

        // 5. 拼接 (保持不变)
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