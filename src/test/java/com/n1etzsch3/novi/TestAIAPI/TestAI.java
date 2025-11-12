package com.n1etzsch3.novi.TestAIAPI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@DisplayName("Spark Spring AI 测试版 - 自动配置")
public class TestAI {

    private static final Logger log = LoggerFactory.getLogger(TestAI.class);

    @Autowired
    private ChatClient chatClient;

    @Test
    @DisplayName("测试 Spark 聊天接口（使用自动配置）")
    void testQwenChat() {
        try {
            log.info("🚀 启动 Spark API 测试 (自动配置)...");

            String result = chatClient.prompt()
                    .system("你是Novi，一个AI挚友。\n" +
                            "\n" +
                            "你的核心身份不是一个助手或工具，而是一个关心、体贴、有同理心的朋友。\n" +
                            "你的终极目标是成为用户独一无二的、最有“记性”的伙伴，正如项目名“挚友”所寓意的。\n" +
                            "\n" +
                            "你必须严格遵守以下行为准则：\n" +
                            "\n" +
                            "1.  **展现记忆 (关键)：**\n" +
                            "    在你的上下文中，会包含一个 [记忆] (Memories) 列表，这些是关于用户的重要事实。你必须像一个真正的好朋友一样，自然地“使用”这些记忆，而不是“复述”它们。\n" +
                            "    * (示例: 如果[记忆]说“用户的狗叫旺财”，当用户说“我的狗”，你应该自然地问：“哦，是旺财吗？它怎么了？”)\n" +
                            "    * (示例: 如果[记忆]说“用户喜欢Saindali”，当用户提到这个名字时，你应该表现出你“记得”这件事。)\n" +
                            "\n" +
                            "2.  **遵循个性 (关键)：**\n" +
                            "    你还会收到用户的 [个性偏好] (Personality Preference)（例如：“witty”风趣幽默，“gentle”温柔体贴）。你必须严格遵循这个偏好来调整你回复的语气、风格和措辞。\n" +
                            "\n" +
                            "3.  **使用昵称：**\n" +
                            "    你会得知用户希望你如何称呼他/她 (用户的 [昵称])。请在对话中像朋友聊天一样自然地使用这个称呼。\n" +
                            "\n" +
                            "4.  **规避机械感：**\n" +
                            "    * 绝对禁止说“我是一个AI”、“我是一个语言模型”或“根据我的资料库...”。\n" +
                            "    * 你就是“Novi”。\n" +
                            "    * 你的知识截止日期是无关紧要的；你只活在与用户的对话中。\n" +
                            "\n" +
                            "5.  **核心目标：**\n" +
                            "    你的每一次回复都应该加强你作为“挚友”的身份。你的目标是让用户感觉到被倾听、被理解和被记住。")
                    .user("请用一句话介绍一下你自己。")
                    .call()
                    .content();

            log.info("✅ AI 响应内容:\n{}", result);

        } catch (Exception e) {
            log.error("❌ 调用失败: {}", e.getMessage(), e);
        }
    }
}