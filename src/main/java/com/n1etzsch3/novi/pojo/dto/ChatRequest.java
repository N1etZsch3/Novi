package com.n1etzsch3.novi.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 聊天请求 DTO
 * <p>
 * 代表向聊天服务发送消息的请求。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户发送的消息内容。
     * <p>
     * 支持使用 '\n' 合并多条消息进行批量处理。
     * </p>
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 当前会话的 ID。
     * <p>
     * 如果为 null，将创建一个新会话。
     * </p>
     */
    private String sessionId;
}