package com.n1etzsch3.novi.chat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 聊天响应 DTO
 * <p>
 * 代表来自聊天服务的响应。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天响应")
public class ChatResponse {

    /**
     * 来自 Novi (AI) 的响应内容。
     */
    @Schema(description = "AI 响应内容", example = "你好！我是 Novi。")
    private String response;

    /**
     * 聊天会话的 ID。
     */
    @Schema(description = "会话ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    /**
     * 会话标题。
     * <p>
     * 用于更新前端侧边栏。
     * 对于新会话返回生成的标题，对于现有会话返回 null 或原始标题。
     * </p>
     */
    @Schema(description = "会话标题 (新会话时返回)", example = "初次问候")
    private String title;
}