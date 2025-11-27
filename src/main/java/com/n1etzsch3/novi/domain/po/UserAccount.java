package com.n1etzsch3.novi.domain.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Set;

/**
 * 用户账户实体类
 * <p>
 * 代表已注册的用户账户。
 * </p>
 * <p>
 * 对应数据库表 `user_account`。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_account")
public class UserAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名 (唯一)。
     */
    private String username;

    /**
     * 哈希密码。
     * <p>
     * 仅写访问，防止在 API 响应中暴露。
     * </p>
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String hashedPassword;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 电子邮件地址 (唯一)。
     */
    private String email;

    /**
     * 用户偏好设置 (JSON)。
     */
    private String preferences;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;

    // --- 关系 ---

    /**
     * 与用户关联的聊天消息。
     */
    @TableField(exist = false)
    @JsonIgnore
    @Builder.Default
    private Set<ChatMessage> chatMessages = new HashSet<>();

    /**
     * 与用户关联的用户记忆。
     */
    @TableField(exist = false)
    @JsonIgnore
    @Builder.Default
    private Set<UserMemory> userMemories = new HashSet<>();
}