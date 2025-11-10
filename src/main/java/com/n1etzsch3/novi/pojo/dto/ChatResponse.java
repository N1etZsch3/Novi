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
     * 如果请求时 sessionId 为 null，这里将返回一个新生成的ID。
     */
    private String sessionId;
}