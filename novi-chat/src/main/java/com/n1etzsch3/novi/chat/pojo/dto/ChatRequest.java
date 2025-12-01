package com.n1etzsch3.novi.chat.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "聊天请求")
public class ChatRequest {

    /**
     * 用户发送的消息内容。
     * <p>
     * 支持使用 '\n' 合并多条消息进行批量处理。
     * </p>
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容 (支持换行)", example = "你好，Novi！")
    private String message;

    /**
     * 当前会话的 ID。
     * <p>
     * 如果为 null，将创建一个新会话。
     * </p>
     */
    @Schema(description = "会话ID (为空则创建新会话)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;
}