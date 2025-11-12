package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.service.ChatService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;



    /**
     * 发送消息 (核心聊天接口)
     * 阻塞式调用，等待 AI 回复后返回
     */
    @PostMapping("/send/call")
    public Result sendMessage(@Validated @RequestBody ChatRequest request) {
        // 鉴权：拦截器已完成，直接从 ThreadLocal 获取 userId
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            // 理论上拦截器会处理，但作为双重检查
            return Result.error("INVALID_TOKEN");
        }

        log.info("收到用户 {} 在会话 {} 的消息: {}，阻塞式调用", userId, request.getSessionId(), request.getMessage());

        // 接收信息 & 委派任务 (包括封装Nickname、调用AI等)
        // 所有复杂逻辑都在 chatService.handleMessage 中完成
        ChatResponse response = chatService.handleCallMessage(userId, request);

        // 返回响应
        return Result.success(response);
    }

    /**
     * 发送消息 (核心聊天接口)
     * 流式调用 (SSE - Server-Sent Events)
     * * @Note
     * 1. 方法修改为 @PostMapping 以接收 ChatRequest (包含 sessionId)
     * 2. produces 修改为 MediaType.TEXT_EVENT_STREAM_VALUE
     * 3. 增加了与 /send/call 一致的 userId 鉴权
     * 4. 业务逻辑完全委派给 chatService.handleStreamMessage
     */
    @PostMapping(value = "/send/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@Validated @RequestBody ChatRequest request) {

        // 鉴权：拦截器已完成，直接从 ThreadLocal 获取 userId
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            // 理论上拦截器会处理，但作为双重检查
            // 对于流式端点，我们返回一个带有错误信号的 Flux
            log.warn("流式调用失败：INVALID_TOKEN，会话: {}", request.getSessionId());
            return Flux.error(new RuntimeException("INVALID_TOKEN"));
        }

        // 逻辑委派给 Service
        return chatService.handleStreamMessage(userId, request);
    }

}

