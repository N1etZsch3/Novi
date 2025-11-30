package com.n1etzsch3.novi.common.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * <p>
 * 对应数据库表 `chat_message`。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_message")
public class ChatMessage {

    /**
     * 消息 ID (主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话 ID。
     */
    /**
     * 会话 ID。
     */
    private String sessionId;

    /**
     * 消息角色 (USER 或 ASSISTANT)。
     */
    private MessageRole role;

    /**
     * 消息内容。
     */
    private String content;

    /**
     * 创建时间戳。
     */
    private LocalDateTime timestamp;

    /**
     * 用户 ID
     */
    private Long userId;

    // --- 关系 ---

    /**
     * 拥有此消息的用户。
     */
    @TableField(exist = false)
    @JsonIgnore
    private UserAccount user;

    /**
     * 消息角色枚举。
     */
    public enum MessageRole {
        USER,
        ASSISTANT
    }
}
