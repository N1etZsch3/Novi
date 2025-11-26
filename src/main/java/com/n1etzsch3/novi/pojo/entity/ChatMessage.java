package com.n1etzsch3.novi.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * <p>
 * 代表聊天会话中的单条消息。
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
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_user_session", columnList = "user_id, session_id, timestamp")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话 ID。
     */
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    /**
     * 消息角色 (USER 或 ASSISTANT)。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    /**
     * 消息内容。
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 创建时间戳。
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // --- 关系 ---

    /**
     * 拥有此消息的用户。
     * <p>
     * FetchType.LAZY: 仅在访问时加载。
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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
