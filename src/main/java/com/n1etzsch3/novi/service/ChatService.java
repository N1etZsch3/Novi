package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;

public interface ChatService {

    /**
     * 处理用户的聊天消息
     *
     * @param userId      从 Token 中解析出的用户ID
     * @param request 包含消息内容和 sessionId 的请求
     * @return 包含AI回复和 sessionId 的响应
     */
    ChatResponse handleMessage(Long userId, ChatRequest request);

}