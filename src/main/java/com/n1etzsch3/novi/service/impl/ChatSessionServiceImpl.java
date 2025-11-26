package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.n1etzsch3.novi.mapper.ChatSessionMapper;
import com.n1etzsch3.novi.pojo.entity.ChatSession;
import com.n1etzsch3.novi.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天会话服务实现类
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return chatSessionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getIsDeleted, 0)
                        .orderByDesc(ChatSession::getUpdatedAt));
    }

    @Override
    public void validateSessionOwner(String sessionId, Long userId) {
        Long count = chatSessionMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getUserId, userId));
        if (count == 0) {
            throw new BusinessException("Session not found or access denied");
        }
    }

    @Override
    public void deleteSession(String sessionId, Long userId) {
        // 验证所有权
        validateSessionOwner(sessionId, userId);
        chatSessionMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getUserId, userId)
                        .set(ChatSession::getIsDeleted, 1));
    }

    @Override
    public ChatSession getSession(Long userId, String sessionId) {
        // 验证所有权并获取
        ChatSession session = chatSessionMapper.selectOne(
                new QueryWrapper<ChatSession>()
                        .eq("id", sessionId)
                        .eq("user_id", userId));
        if (session == null) {
            throw new RuntimeException("Session not found or access denied");
        }
        return session;
    }
}
