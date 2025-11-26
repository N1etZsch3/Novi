package com.n1etzsch3.novi.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用户账户实体类
 * <p>
 * 代表已注册的用户账户。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名 (唯一)。
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 哈希密码。
     * <p>
     * 仅写访问，防止在 API 响应中暴露。
     * </p>
     */
    @Column(name = "hashed_password", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String hashedPassword;

    /**
     * 用户昵称。
     */
    @Column(length = 50)
    private String nickname;

    /**
     * 电子邮件地址 (唯一)。
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * 用户偏好设置 (JSON)。
     */
    @Column(columnDefinition = "json")
    private String preferences;

    /**
     * 创建时间。
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后更新时间。
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- 关系 ---

    /**
     * 与用户关联的聊天消息。
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ChatMessage> chatMessages = new HashSet<>();

    /**
     * 与用户关联的用户记忆。
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UserMemory> userMemories = new HashSet<>();
}