package com.n1etzsch3.novi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.mapper.ChatMemoryMapper;
import com.n1etzsch3.novi.domain.dto.Result;
import com.n1etzsch3.novi.domain.po.ChatMessage;
import com.n1etzsch3.novi.domain.po.ChatSession;
import com.n1etzsch3.novi.service.ChatSessionService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天会话控制器
 * <p>
 * 管理聊天会话，包括获取列表、消息和删除会话。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@RestController
@RequestMapping("/api/v1/sessions")
@AllArgsConstructor
@Slf4j
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 获取当前用户的聊天会话列表。
     *
     * @return 包含聊天会话列表的结果。
     */
    @GetMapping
    public Result getSessionList() {
        Long userId = LoginUserContext.getUserId();
        List<ChatSession> sessions = chatSessionService.getUserSessions(userId);
        log.info("Retrieved session list for user: {}", userId);
        return Result.success(sessions);
    }

    /**
     * 获取特定聊天会话的消息。
     *
     * @param sessionId 会话 ID。
     * @return 包含聊天消息列表的结果。
     */
    @GetMapping("/{sessionId}/messages")
    public Result getSessionMessages(@PathVariable String sessionId) {
        Long userId = LoginUserContext.getUserId();

        // 安全检查: 确保会话属于当前用户
        chatSessionService.validateSessionOwner(sessionId, userId);

        // 复用现有的 ChatMemoryMapper 方法
        List<ChatMessage> messages = chatMemoryMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getId));
        log.info("Retrieved messages for session: {}", sessionId);
        return Result.success(messages);
    }

    /**
     * 删除聊天会话及其消息。
     *
     * @param sessionId 要删除的会话 ID。
     * @return 成功结果。
     */
    @DeleteMapping("/{sessionId}")
    public Result deleteSession(@PathVariable String sessionId) {
        Long userId = LoginUserContext.getUserId();
        chatSessionService.deleteSession(sessionId, userId);
        log.info("Deleted session: {}", sessionId);
        return Result.success();
    }

}
