package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 聊天服务接口
 * <p>
 * 定义核心聊天操作，支持阻塞式和流式响应。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
public interface ChatService {

    /**
     * 处理阻塞式聊天消息。
     *
     * @param userId  用户 ID。
     * @param request 聊天请求对象。
     * @return 聊天响应对象。
     */
    ChatResponse handleCallMessage(Long userId, ChatRequest request);

    /**
     * 处理流式聊天消息。
     *
     * @param userId  用户 ID。
     * @param request 聊天请求对象。
     * @return 字符串流 (SSE)。
     */
    Flux<String> handleStreamMessage(Long userId, ChatRequest request);
}
