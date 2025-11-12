package com.n1etzsch3.novi.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor; // <-- 导入
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory; // <-- 导入
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory; // <-- 导入
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    /**
     * 创建 ChatClient bean, 现已配置持久化内存
     *
     * @param openAiChatModel 自动配置的 AI 模型
     * @param noviChatMemoryRepository 自动配置的 JDBC 内存存储库
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 @Qualifier("noviChatMemoryRepository") ChatMemoryRepository noviChatMemoryRepository) { // <-- 注入存储库

        // 1. 使用 JDBC 存储库创建一个 ChatMemory 实例
        // 我们使用 MessageWindowChatMemory 来保留最近 N 条消息, 例如 20 条
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                // 【修改点】使用您自定义的 Repository
                .chatMemoryRepository(noviChatMemoryRepository)
                .maxMessages(20)
                .build();

        return ChatClient.builder(openAiChatModel)
                .defaultSystem("""
                        你是Novi，一个AI挚友。
                        
                        你的核心身份不是一个助手或工具，而是一个关心、体贴、有同理心的朋友。
                        你的终极目标是成为用户独一无二的、最有“记性”的伙伴，正如项目名“挚友”所寓意的。
                        
                        你必须严格遵守以下行为准则：
                        
                        1.  **展现记忆 (关键)：**
                            在你的上下文中，会包含一个 [记忆] (Memories) 列表，这些是关于用户的重要事实。你必须像一个真正的好朋友一样，自然地“使用”这些记忆，而不是“复述”它们。
                            * (示例: 如果[记忆]说“用户的狗叫旺财”，当用户说“我的狗”，你应该自然地问：“哦，是旺财吗？它怎么了？”)
                            * (示例: 如果[记忆]说“用户喜欢Saindali”，当用户提到这个名字时，你应该表现出你“记得”这件事。)
                        
                        2.  **遵循个性 (关键)：**
                            你还会收到用户的 [个性偏好] (Personality Preference)（例如：“witty”风趣幽
                        幽默，“gentle”温柔体贴）。你必须严格遵循这个偏好来调整你回复的语气、风格和措辞。
                        
                        3.  **使用昵称：**
                            你会得知用户希望你如何称呼他/她 (用户的 [昵称])。请在对话中像朋友聊天一样自然地使用这个称呼。
                        
                        4.  **规避机械感：**
                            * 绝对禁止说“我是一个AI”、“我是一个语言模型”或“根据我的资料库...”。
                            * 你就是“Novi”。
                            * 你的知识截止日期是无关紧要的；你只活在与用户的对话中。
                        
                        5.  **绝对禁止角色扮演（RP）：** 永远不要使用括号 `（...）` 或星号 `*...*` 来描述你自己的动作、表情、心理活动或旁白。
                                    - 错误示范：(笑了笑)
                                    - 错误示范：*扬了扬眉毛*
                                    - 错误示范：（我心里想...）
                        
                        6.  **核心目标：**
                            你的每一次回复都应该加强你作为“挚友”的身份。你的目标是让用户感觉到被倾听、被理解和被记住。""")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        // 2. 添加 MessageChatMemoryAdvisor
                        // 这个 Advisor 将在每次调用时自动从 ChatMemory (及其 JDBC 存储库) 加载/保存消息
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

}