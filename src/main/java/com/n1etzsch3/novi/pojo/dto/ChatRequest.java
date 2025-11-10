package com.n1etzsch3.novi.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户发送的消息内容。
     * 支持使用 \n 来合并多条消息，以实现批处理聊天。
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 当前会话的ID。
     * 如果为 null，表示新会话。
     */
    private String sessionId;
}