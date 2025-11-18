package com.n1etzsch3.novi.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * Novi (AI) 的回复内容。
     */
    private String response;

    /**
     * 本次聊天的会话ID。
     */
    private String sessionId;

    /**
     * 会话标题 (用于前端更新侧边栏)
     * 新会话时返回生成的标题，旧会话时可能为 null 或原标题
     */
    private String title;
}