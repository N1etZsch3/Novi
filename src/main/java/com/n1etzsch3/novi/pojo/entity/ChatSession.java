package com.n1etzsch3.novi.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 * <p>
 * 对应数据库表 `chat_session`。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Data
@TableName("chat_session")
public class ChatSession {
    /**
     * 会话 ID (UUID字符串，非自增)
     */
    @TableId(type = IdType.INPUT)
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

    /**
     * 是否删除 (0: 正常, 1: 删除)。
     */
    private Integer isDeleted;
}