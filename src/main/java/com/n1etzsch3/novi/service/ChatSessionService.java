package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.pojo.entity.ChatSession;

import java.util.List;

public interface ChatSessionService {
    List<ChatSession> getUserSessions(Long userId);

    void validateSessionOwner(String sessionId, Long userId);

    void deleteSession(String sessionId, Long userId);
}
