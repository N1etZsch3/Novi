package com.n1etzsch3.novi.repository;

import com.n1etzsch3.novi.mapper.ChatMemoryMapper;
import com.n1etzsch3.novi.pojo.entity.ChatMessage;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component; // 修改为 @Component

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 修复方案 2b：
 * 自定义的 ChatMemory 实现，替换 MessageWindowChatMemory。
 * 此实现解决了消息重复存储的问题，它只在 add() 方法中保存新消息。
 */
@Slf4j
@Component // 注册为 Spring Bean
@Primary   // 优先使用这个 ChatMemory 实现
public class NoviDatabaseChatMemory implements ChatMemory {

    private final ChatMemoryMapper chatMemoryMapper;

    @Autowired
    public NoviDatabaseChatMemory(ChatMemoryMapper chatMemoryMapper) {
        this.chatMemoryMapper = chatMemoryMapper;
    }

    /**
     * 加载历史记录 (依赖 ThreadLocal)
     */
    @Override
    public List<Message> get(String conversationId) {
        Long userId = LoginUserContext.getUserId(); // 依赖问题一的修复 (context-propagation)
        if (userId == null) {
            log.warn("NoviDatabaseChatMemory.find: 未找到用户ID，返回空列表 (Session: {})", conversationId);
            return Collections.emptyList();
        }

        List<ChatMessage> chatMessages = chatMemoryMapper.findByUserIdAndSessionId(userId, conversationId);
        
        return chatMessages.stream()
                .map(this::toSpringAiMessage)
                .collect(Collectors.toList());
    }

    /**
     * 保存新消息 (依赖 ThreadLocal)
     * MessageChatMemoryAdvisor 会在 adviseResponse 后调用此方法，
     * 传入的 'messages' 列表仅包含 (User, Assistant) 的新消息对。
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        Long userId = LoginUserContext.getUserId(); // 依赖问题一的修复 (context-propagation)
        if (userId == null) {
            log.error("NoviDatabaseChatMemory.add: 无法保存消息，未找到用户ID (Session: {})", conversationId);
            return;
        }

        UserAccount currentUser = new UserAccount();
        currentUser.setId(userId);

        for (Message message : messages) {
            // 我们只持久化 USER 和 ASSISTANT 的消息
            if (message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.ASSISTANT) {
                ChatMessage chatMessage = toChatMessageEntity(message, currentUser, conversationId);
                chatMemoryMapper.saveMessage(chatMessage);
            }
        }
    }

    /**
     * 清理会话 (依赖 ThreadLocal)
     */
    @Override
    public void clear(String conversationId) {
        Long userId = LoginUserContext.getUserId(); // 依赖问题一的修复 (context-propagation)
        if (userId == null) {
            log.warn("NoviDatabaseChatMemory.clear: 未找到用户ID，操作中止 (Session: {})", conversationId);
            return;
        }
        chatMemoryMapper.deleteByUserIdAndSessionId(userId, conversationId);
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
    private ChatMessage toChatMessageEntity(Message message, UserAccount user, String sessionId) {
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
                .user(user)
                .sessionId(sessionId)
                .role(role)
                .content(textContent)
                .timestamp(LocalDateTime.now()) // 由 MyBatis 插入
                .build();
    }

}