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

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "hashed_password", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 关键：只写（反序列化），不读（序列化）
    private String hashedPassword;

    @Column(length = 50)
    private String nickname;

    @Column(unique = true, length = 100)
    private String email;

    @Column(columnDefinition = "json")
    private String preferences;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- 关系 ---

    /**
     * 一个用户可以有多条聊天记录
     * cascade = CascadeType.ALL: 删除用户时，级联删除所有聊天记录
     * orphanRemoval = true: 从集合中移除消息时，也从数据库中删除
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // 关键：防止序列化时无限循环
    private Set<ChatMessage> chatMessages = new HashSet<>();

    /**
     * 一个用户可以有多条记忆
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // 关键：防止序列化时无限循环
    private Set<UserMemory> userMemories = new HashSet<>();
}