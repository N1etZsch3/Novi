package com.n1etzsch3.novi.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 * <p>
 * 代表包含多条消息的聊天会话。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatSession {
    /**
     * 会话 ID (UUID)。
     */
    private String id;

    /**
     * 与会话关联的用户 ID。
     */
    private Long userId;

    /**
     * 会话标题。
     */
    private String title;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}