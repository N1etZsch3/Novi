package com.n1etzsch3.novi.controller;


import com.n1etzsch3.novi.mapper.ChatMemoryMapper;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.pojo.entity.ChatMessage;
import com.n1etzsch3.novi.pojo.entity.ChatSession;
import com.n1etzsch3.novi.service.ChatSessionService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@AllArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 获取侧边栏列表
     */
    @GetMapping
    public Result getSessionList() {
        Long userId = LoginUserContext.getUserId();
        List<ChatSession> sessions = chatSessionService.getUserSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 点击某个会话，获取该会话的所有消息详情
     */
    @GetMapping("/{sessionId}/messages")
    public Result getSessionMessages(@PathVariable String sessionId) {
        Long userId = LoginUserContext.getUserId();

        // 安全检查：确保这个 sessionId 属于当前用户
        chatSessionService.validateSessionOwner(sessionId, userId);

        // 复用已有的 ChatMemoryMapper 方法
        List<ChatMessage> messages = chatMemoryMapper.findByUserIdAndSessionId(userId, sessionId);
        return Result.success(messages);
    }

    /**
     * 删除某个会话及其所有消息
     */
    @DeleteMapping("/{sessionId}")
    public Result deleteSession(@PathVariable String sessionId) {
        Long userId = LoginUserContext.getUserId();
        chatSessionService.deleteSession(sessionId, userId);
        return Result.success();
    }

}
