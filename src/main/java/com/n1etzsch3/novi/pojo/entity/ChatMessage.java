package com.n1etzsch3.novi.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING) // 将枚举存储为字符串 ("USER", "ASSISTANT")
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // --- 关系 ---

    /**
     * 多条消息属于一个用户
     * FetchType.LAZY: 懒加载，只有在访问 user 属性时才去数据库查询
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // 关键：防止和 UserAccount 产生序列化循环
    private UserAccount user;

    /**
     * 角色枚举
     * (可以放在单独的
     * com.novi.zhiyou.model.enums.MessageRole.java
     * 文件中)
     */
    public enum MessageRole {
        USER,
        ASSISTANT
    }
}
