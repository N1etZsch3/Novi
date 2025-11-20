package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.mapper.ChatSessionMapper;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.StreamEvent;
import com.n1etzsch3.novi.pojo.entity.ChatSession;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.service.ChatService;
import com.n1etzsch3.novi.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserPreferenceService userPreferenceService;

    @Value("classpath:/prompts/chat-system.st")
    private Resource systemPromptResource;

    /**
     * 辅助方法：创建复合键
     */
    private String createCompositeKey(Long userId, String sessionId) {
        return userId + ":" + sessionId;
    }

    private record SessionInfo(String sessionId, String title) {}

    /**
     * 辅助方法：获取或创建会话
     */
    private SessionInfo getOrCreateSession(Long userId, String requestedSessionId, String messageContent) {
        boolean isNewSession = !StringUtils.hasText(requestedSessionId);
        String finalSessionId = isNewSession ? UUID.randomUUID().toString() : requestedSessionId;
        String finalTitle = null;

        if (isNewSession) {
            ChatSession session = new ChatSession();
            session.setId(finalSessionId);
            session.setUserId(userId);
            finalTitle = messageContent != null && messageContent.length() > 20 ?
                    messageContent.substring(0, 20) + "..." : messageContent;
            session.setTitle(finalTitle);
            chatSessionMapper.createSession(session);
            log.info("创建新会话 (DB): {}, 标题: {}", finalSessionId, finalTitle);
        } else {
            int rows = chatSessionMapper.updateLastActiveTime(finalSessionId);
            if (rows == 0) {
                log.warn("会话 {} 在数据库不存在，重新创建", finalSessionId);
                finalTitle = "恢复的会话";
                ChatSession session = new ChatSession();
                session.setId(finalSessionId);
                session.setUserId(userId);
                session.setTitle(finalTitle);
                chatSessionMapper.createSession(session);
            } else {
                finalTitle = null;
            }
        }
        return new SessionInfo(finalSessionId, finalTitle);
    }

    // --- 1. 阻塞式调用 ---
    @Override
    public ChatResponse handleCallMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // 1. 准备 System Message
        Message systemMessage = buildSystemMessage(userId, userMessage);

        // 2. 获取 Session
        SessionInfo sessionInfo = getOrCreateSession(userId, request.getSessionId(), userMessage);
        String compositeKey = createCompositeKey(userId, sessionInfo.sessionId());

        // 3. 调用 AI
        String AIResponse = chatClient.prompt()
                .messages(systemMessage) // 注入动态 System Prompt
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, compositeKey))
                .call()
                .content();

        return new ChatResponse(AIResponse, sessionInfo.sessionId(), sessionInfo.title());
    }

    // --- 2. 流式调用 ---
    @Override
    public Flux<String> handleStreamMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // 1. 【新增】准备 System Message (逻辑与阻塞式一致)
        Message systemMessage = buildSystemMessage(userId, userMessage);

        // 2. 获取 Session
        SessionInfo sessionInfo = getOrCreateSession(userId, request.getSessionId(), userMessage);
        String sessionIdToUse = sessionInfo.sessionId();
        String compositeKey = createCompositeKey(userId, sessionIdToUse);

        // 3. 创建元数据事件
        StreamEvent metadataEvent = StreamEvent.metadata(sessionIdToUse, sessionInfo.title());
        Flux<StreamEvent> metadataStream = Flux.just(metadataEvent);

        // 4. 【修改】AI 内容流 - 注入 systemMessage
        Flux<StreamEvent> contentStream = chatClient.prompt()
                .messages(systemMessage) // <--- 关键：这里也要注入 System Prompt
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, compositeKey))
                .stream()
                .content()
                .map(StreamEvent::content);

        // 5. 拼接并序列化
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

    /**
     * 【新增】抽取公共方法，构建动态的 System Message
     * 避免代码重复，保证流式和阻塞式的人设一致
     */
    private Message buildSystemMessage(Long userId, String userMessage) {
        // 1. 获取用户信息
        UserAccount user = userAccountMapper.findById(userId);
        String nickname = (user.getNickname() != null) ? user.getNickname() : "老铁";

        // 2. 获取用户偏好 (后续可改为从 DB 获取)

        // String personality = "随性、说话直爽、有点小幽默";
        String personality = String.valueOf(userPreferenceService.getPersonaSettings(userId));
        log.info("用户 {} 的人设: {}", userId, personality);

        // 3. 获取记忆
        // TODO : 实现记忆检索逻辑
        String memories = "（暂无特殊记忆，就像平时一样闲聊）";

        // 4. 构建并返回 System Message
        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptResource);
        Map<String, Object> promptVars = Map.of(
                "nickname", nickname,
                "personality", personality,
                "current_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                "memories", memories,
                "user_message", userMessage
        );
        return systemTemplate.createMessage(promptVars);
    }
}