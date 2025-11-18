package com.n1etzsch3.novi.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatSession {
    private String id;          // session_id
    private Long userId;
    private String title;       // 会话标题 (默认为 "新会话" 或第一条消息的前20个字)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}