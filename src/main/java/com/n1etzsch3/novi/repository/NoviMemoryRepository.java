package com.n1etzsch3.novi.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.mapper.ChatMemoryMapper;
import com.n1etzsch3.novi.pojo.entity.ChatMessage;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@AllArgsConstructor
@Primary
/**
 * 开发测试版 MessageWindowChatMemory 实现
 * 基于 Novi 的数据库和 MyBatis 映射器
 * 用于MessageWindowMemory 持久化聊天记录
 * 
 * @Note: 此实现与 LoginUserContext 绑定
 */
public class NoviMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 获取当前登录用户的所有会话ID
     *
     * @Note: 此实现与 LoginUserContext 绑定
     */
    @Override
    public List<String> findConversationIds() {
        Long userId = LoginUserContext.getUserId();
        log.info("NoviMemoryRepository.findConversationIds: User ID from ThreadLocal: {}", userId);
        if (userId == null) {
            log.warn("NoviMemoryRepository.findConversationIds: 未找到用户ID，返回空列表");
            return Collections.emptyList();
        }
        List<Object> sessionIds = chatMemoryMapper.selectObjs(
                new LambdaQueryWrapper<ChatMessage>()
                        .select(ChatMessage::getSessionId)
                        .eq(ChatMessage::getUserId, userId)
                        .groupBy(ChatMessage::getSessionId));
        return sessionIds.stream().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * 根据会话ID获取聊天记录
     *
     * @Note: 此实现与 LoginUserContext 绑定
     */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            log.warn("NoviMemoryRepository.findByConversationId: 未找到用户ID，返回空列表");
            return Collections.emptyList();
        }

        // 1. 从数据库中获取持久化的 ChatMessage 实体
        List<ChatMessage> chatMessages = chatMemoryMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getSessionId, conversationId)
                        .orderByAsc(ChatMessage::getId));

        // 2. 将 ChatMessage 实体 转换为 Spring AI 的 Message 列表
        return chatMessages.stream()
                .map(this::toSpringAiMessage)
                .collect(Collectors.toList());
    }

    /**
     * 保存聊天记录
     *
     * @Note: 此实现与 LoginUserContext 绑定
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Long userId = LoginUserContext.getUserId();
        log.info("NoviMemoryRepository.saveAll: User ID from ThreadLocal: {}", userId);
        if (userId == null) {
            log.error("NoviMemoryRepository.saveAll: 无法保存消息，未找到用户ID");
            // 在这种情况下，我们不能继续，因为 user_id 是 chat_message 表的 NOT NULL 字段
            return;
        }

        for (Message message : messages) {
            // 我们只持久化 USER 和 ASSISTANT 的消息
            // SYSTEM 消息通常是固定的，不需要存储在会话历史中
            if (message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.ASSISTANT) {

                // 1. 将 Spring AI 的 Message 转换为 ChatMessage 实体
                ChatMessage chatMessage = toChatMessageEntity(message, userId, conversationId);

                // 2. 保存到数据库
                chatMemoryMapper.insert(chatMessage);
            }
        }
    }

    /**
     * 根据会话ID删除聊天记录
     *
     * @Note: 此实现与 LoginUserContext 绑定
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            log.warn("NoviMemoryRepository.deleteByConversationId: 未找到用户ID，操作中止");
            return;
        }
        chatMemoryMapper.delete(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getSessionId, conversationId));
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
        // 根据教程，UserMessage 和 AssistantMessage 都使用 .getText()
        // 而不是 .getContent() 来获取纯文本。
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