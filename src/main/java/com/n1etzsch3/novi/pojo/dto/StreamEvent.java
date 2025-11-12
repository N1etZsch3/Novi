package com.n1etzsch3.novi.pojo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 关键：如果某字段为null，不序列化
public class StreamEvent {
    
    /**
     * 事件类型：
     * "METADATA" - 元数据事件，包含 sessionId 等
     * "CONTENT" - 内容块
     * "ERROR"   - 错误事件
     */
    private String eventType;

    /**
     * 会话ID (只在 METADATA 事件中发送)
     */
    private String sessionId;

    /**
     * AI 回复的内容块 (只在 CONTENT 事件中发送)
     */
    private String content;

    /**
     * 错误信息 (只在 ERROR 事件中发送)
     */
    private String errorMessage;

    // --- 静态工厂方法，使创建更清晰 ---

    public static StreamEvent metadata(String sessionId) {
        return new StreamEvent("METADATA", sessionId, null, null);
    }

    public static StreamEvent content(String content) {
        return new StreamEvent("CONTENT", null, content, null);
    }

    public static StreamEvent error(String errorMessage) {
        return new StreamEvent("ERROR", null, null, errorMessage);
    }
}