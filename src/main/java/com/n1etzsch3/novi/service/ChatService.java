package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 处理用户的聊天消息
     * 阻塞式调用，等待 AI 回复后返回
     *
     * @param userId      从 Token 中解析出的用户ID
     * @param request 包含消息内容和 sessionId 的请求
     * @return 包含AI回复和 sessionId 的响应
     */
    ChatResponse handleCallMessage(Long userId, ChatRequest request);


    /**
     * 流式调用
     */
    Flux<String> handleStreamMessage(Long userId, ChatRequest request);
}