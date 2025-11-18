package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.mapper.ChatSessionMapper;
import com.n1etzsch3.novi.pojo.entity.ChatSession;
import com.n1etzsch3.novi.service.ChatSessionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@AllArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;

    /**
     * 根据用户Id获取该用户的所有会话
     */
    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return chatSessionMapper.findByUserId(userId);
    }


    @Override
    public void validateSessionOwner(String sessionId, Long userId) {
        int count = chatSessionMapper.countByUserIdAndSessionId(userId, sessionId);
        if (count == 0) {
            // 可能是会话不存在，也可能是属于别人
            throw new BusinessException("会话不存在或无权访问");
        }
    }

    @Override
    public void deleteSession(String sessionId, Long userId) {
        chatSessionMapper.softDeleteSession(sessionId, userId);
    }
}
