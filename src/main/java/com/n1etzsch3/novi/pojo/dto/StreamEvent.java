package com.n1etzsch3.novi.pojo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamEvent {

    private String eventType;
    private String sessionId;
    private String content;
    private String errorMessage;

    // 新增标题字段
    private String title;

    // --- 静态工厂方法 ---

    /**
     * 元数据事件：现在包含 sessionId 和 title
     */
    public static StreamEvent metadata(String sessionId, String title) {
        // 参数顺序必须与字段声明顺序一致：eventType, sessionId, content, errorMessage, title
        return new StreamEvent("METADATA", sessionId, null, null, title);
    }

    public static StreamEvent content(String content) {
        return new StreamEvent("CONTENT", null, content, null, null);
    }

    public static StreamEvent error(String errorMessage) {
        return new StreamEvent("ERROR", null, null, errorMessage, null);
    }
}