package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.service.ChatService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatClient chatClient;

    /**
     * 发送消息 (核心聊天接口)
     */
    @PostMapping("/send")
    public Result sendMessage(@Validated @RequestBody ChatRequest request) {
        // 鉴权：拦截器已完成，直接从 ThreadLocal 获取 userId
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            // 理论上拦截器会处理，但作为双重检查
            return Result.error("INVALID_TOKEN");
        }

        log.info("收到用户 {} 在会话 {} 的消息: {}", userId, request.getSessionId(), request.getMessage());

        // 接收信息 & 委派任务 (包括封装Nickname、调用AI等)
        // 所有复杂逻辑都在 chatService.handleMessage 中完成
        ChatResponse response = chatService.handleMessage(userId, request);

        // 返回响应
        return Result.success(response);
    }

    @GetMapping("/test")
    public Result testEndpoint(@RequestParam String message) {
        return Result.success(chatClient.prompt()
                .user(message)
                .call()
                .content());
    }

}