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
@Table(name = "user_memory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_fact", columnNames = {"user_id", "fact_key"})
})
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fact_key", nullable = false, length = 100)
    private String factKey;

    @Column(name = "fact_value", nullable = false, columnDefinition = "TEXT")
    private String factValue;

    @Column(name = "fact_type", length = 50)
    private String factType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    // --- 关系 ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // 关键：防止和 UserAccount 产生序列化循环
    private UserAccount user;

    /**
     * 记忆的来源消息（可以为 null）
     * 对应数据库的 ON DELETE SET NULL 行为（如果 ChatMessage 被删除，这里会变为 null）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_message_id", foreignKey = @ForeignKey(
            name = "user_memory_ibfk_2",
            foreignKeyDefinition = "FOREIGN KEY (source_message_id) REFERENCES chat_message(id) ON DELETE SET NULL"
    ))
    private ChatMessage sourceMessage;
}
