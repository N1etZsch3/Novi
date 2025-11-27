package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.domain.dto.ChatRequest;
import com.n1etzsch3.novi.domain.dto.ChatResponse;
import com.n1etzsch3.novi.domain.dto.Result;
import com.n1etzsch3.novi.service.ChatService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 聊天控制器
 * <p>
 * 处理聊天相关的请求，包括阻塞式调用和流式响应。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送消息 (阻塞式)
     * <p>
     * 此端点会等待 AI 生成完整的回复后再返回。
     * </p>
     *
     * @param request 包含消息内容和会话 ID 的聊天请求对象。
     * @return 包含 AI 回复的结果对象。
     */
    @PostMapping("/send/call")
    public Result sendMessage(@Validated @RequestBody ChatRequest request) {
        // 鉴权: UserId 从 ThreadLocal 中获取 (由拦截器设置)
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            return Result.error("INVALID_TOKEN");
        }

        log.info("Received blocking message from user {} in session {}: {}", userId, request.getSessionId(),
                request.getMessage());

        // 将业务逻辑委托给 ChatService
        ChatResponse response = chatService.handleCallMessage(userId, request);

        return Result.success(response);
    }

    /**
     * 发送消息 (流式)
     * <p>
     * 此端点返回一个事件流 (SSE)，随着 AI 生成回复实时推送。
     * </p>
     *
     * @param request 包含消息内容和会话 ID 的聊天请求对象。
     * @return 代表事件流的 Flux<String> 对象。
     */
    @PostMapping(value = "/send/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@Validated @RequestBody ChatRequest request) {

        // 鉴权: UserId 从 ThreadLocal 中获取
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            log.warn("Stream call failed: INVALID_TOKEN, session: {}", request.getSessionId());
        }

        // 将业务逻辑委托给 ChatService
        Flux<String> stream = chatService.handleStreamMessage(userId, request);

        // 注意: userId 上下文由响应式框架捕获/传播，或者在需要时手动处理。
        // 在此实现中，服务层在初始提取后独立于线程上下文处理逻辑。
        return stream;
    }

}
