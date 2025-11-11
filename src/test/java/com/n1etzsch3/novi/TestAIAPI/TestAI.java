package com.n1etzsch3.novi.TestAIAPI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@DisplayName("Qwen (DashScope) Spring AI 测试版 - 自动配置")
public class TestAI {

    private static final Logger log = LoggerFactory.getLogger(TestAI.class);

    @Autowired
    private ChatClient chatClient;

    @Test
    @DisplayName("测试 Qwen 聊天接口（使用自动配置）")
    void testQwenChat() {
        try {
            log.info("🚀 启动 Qwen API 测试 (自动配置)...");

            String result = chatClient.prompt()
                    .system("你是一个友好的中文AI助手。")
                    .user("请用一句话介绍一下你自己。")
                    .call()
                    .content();

            log.info("✅ AI 响应内容:\n{}", result);

        } catch (Exception e) {
            log.error("❌ 调用失败: {}", e.getMessage(), e);
        }
    }
}