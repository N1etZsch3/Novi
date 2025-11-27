package com.n1etzsch3.novi.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.mapper.ChatMemoryMapper;
import com.n1etzsch3.novi.pojo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 修复方案 2b：
 * 自定义的 ChatMemory 实现，替换 MessageWindowChatMemory。
 * 此实现解决了消息重复存储的问题，它只在 add() 方法中保存新消息。
 *
 * 【重构】：
 * 移除了对 ThreadLocal (LoginUserContext) 的依赖。
 * 现在所有方法都从传入的 conversationId (复合键 "userId:sessionId") 中解析 userId。
 */
@Slf4j
@Component // 注册为 Spring Bean
@Primary // 优先使用这个 ChatMemory 实现
public class NoviDatabaseChatMemory implements ChatMemory {

    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 【新增】用于解析复合键的内部类或记录 (Record)
     */
    private record ParsedKey(Long userId, String sessionId) {
    }

    /**
     * 【新增】辅助方法，用于解析复合键
     *
     * @param compositeKey 格式为 "userId:sessionId"
     * @return ParsedKey 对象，如果格式无效则返回 null
     */
    private ParsedKey parseCompositeKey(String compositeKey) {
        if (!StringUtils.hasText(compositeKey) || !compositeKey.contains(":")) {
            log.error("NoviDatabaseChatMemory: 无效的复合键格式: {}", compositeKey);
            return null;
        }
        try {
            String[] parts = compositeKey.split(":", 2);
            Long userId = Long.parseLong(parts[0]);
            String sessionId = parts[1];
            return new ParsedKey(userId, sessionId);
        } catch (Exception e) {
            log.error("NoviDatabaseChatMemory: 解析复合键失败: {}", compositeKey, e);
            return null;
        }
    }

    @Autowired
    public NoviDatabaseChatMemory(ChatMemoryMapper chatMemoryMapper) {
        this.chatMemoryMapper = chatMemoryMapper;
    }

    /**
     * 加载历史记录 (已重构)
     */
    @Override
    public List<Message> get(String conversationId) {
        // 【关键修改】
        ParsedKey key = parseCompositeKey(conversationId);
        if (key == null || key.userId == null) {
            log.warn("NoviDatabaseChatMemory.get: 未找到用户ID，返回空列表 (Key: {})", conversationId);
            return Collections.emptyList();
        }

        List<ChatMessage> chatMessages = chatMemoryMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, key.userId)
                        .eq(ChatMessage::getSessionId, key.sessionId)
                        .orderByAsc(ChatMessage::getId));

        return chatMessages.stream()
                .map(this::toSpringAiMessage)
                .collect(Collectors.toList());
    }

    /**
     * 保存新消息 (已重构)
     * MessageChatMemoryAdvisor 会在 adviseResponse 后调用此方法，
     * 传入的 'messages' 列表仅包含 (User, Assistant) 的新消息对。
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        // 【关键修改】
        ParsedKey key = parseCompositeKey(conversationId);
        if (key == null || key.userId == null) {
            log.error("NoviDatabaseChatMemory.add: 无法保存消息，未找到用户ID (Key: {})", conversationId);
            return;
        }

        for (Message message : messages) {
            // 我们只持久化 USER 和 ASSISTANT 的消息
            if (message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.ASSISTANT) {
                // 【关键修改】
                ChatMessage chatMessage = toChatMessageEntity(message, key.userId, key.sessionId);
                chatMemoryMapper.insert(chatMessage);
            }
        }
    }

    /**
     * 清理会话 (已重构)
     */
    @Override
    public void clear(String conversationId) {
        // 【关键修改】
        ParsedKey key = parseCompositeKey(conversationId);
        if (key == null || key.userId == null) {
            log.warn("NoviDatabaseChatMemory.clear: 未找到用户ID，操作中止 (Key: {})", conversationId);
            return;
        }
        chatMemoryMapper.delete(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, key.userId)
                        .eq(ChatMessage::getSessionId, key.sessionId));
    }

    // --- 辅助转换方法 ---

    /**
     * 转换 (Novi 实体) ChatMessage -> (Spring AI) Message
     */
    private Message toSpringAiMessage(ChatMessage chatMessage) {
        // (此方法无需更改)
        if (chatMessage.getRole() == ChatMessage.MessageRole.USER) {
            return new UserMessage(chatMessage.getContent());
        } else if (chatMessage.getRole() == ChatMessage.MessageRole.ASSISTANT) {
            return new AssistantMessage(chatMessage.getContent());
        }
        // 理论上不应发生，但作为保障
        log.warn("未知的消息角色: {}", chatMessage.getRole());
        return new UserMessage(chatMessage.getContent()); // 默认
    }

    /**
     * 转换 (Spring AI) Message -> (Novi 实体) ChatMessage
     * (已修正)
     */
    private ChatMessage toChatMessageEntity(Message message, Long userId, String sessionId) {
        ChatMessage.MessageRole role = (message.getMessageType() == MessageType.USER)
                ? ChatMessage.MessageRole.USER
                : ChatMessage.MessageRole.ASSISTANT;

        // --- 修正 ---
        // 根据文档，UserMessage 和 AssistantMessage 都使用 .getText()来获取纯文本。
        String textContent;
        if (message instanceof UserMessage) {
            textContent = ((UserMessage) message).getText();
        } else if (message instanceof AssistantMessage) {
            textContent = ((AssistantMessage) message).getText();
        } else {
            // 此分支理论上不可达，因为 saveAll 中有类型检查
            log.warn("未知的 Message 实现类型: {}", message.getClass().getName());
            textContent = ""; // 安全回退
        }

        return ChatMessage.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role(role)
                .content(textContent)
                .timestamp(LocalDateTime.now()) // 由 MyBatis 插入
                .build();
    }

}