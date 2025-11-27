package com.n1etzsch3.novi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class ChatResponse {

    /**
     * 来自 Novi (AI) 的响应内容。
     */
    private String response;

    /**
     * 聊天会话的 ID。
     */
    private String sessionId;

    /**
     * 会话标题。
     * <p>
     * 用于更新前端侧边栏。
     * 对于新会话返回生成的标题，对于现有会话返回 null 或原始标题。
     * </p>
     */
    private String title;
}