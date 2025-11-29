package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.repository.NoviDatabaseChatMemory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天配置类
 * <p>
 * 配置ChatClient，支持持久化内存。
 * </p>
 * <p>
 * TODO: 模型热重载功能 - 当前使用 application.yml 配置。
 * AiModelConfigService 已就绪用于管理数据库中的模型配置。
 * 完整的动态模型切换需要实现 ChatClient 刷新机制（如使用 @RefreshScope 或手动刷新）。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class ChatConfig {

        private final NoviDatabaseChatMemory noviDatabaseChatMemory;

        /**
         * 创建 ChatClient bean, 现已配置持久化内存
         * 
         * @param openAiChatModel 自动配置的 AI 模型（来自 application.yml）
         */
        @Bean
        public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
                log.info("Initializing ChatClient with auto-configured OpenAiChatModel");

                MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(noviDatabaseChatMemory)
                                .conversationId(ChatMemory.CONVERSATION_ID)
                                .build();

                return ChatClient.builder(openAiChatModel)
                                .defaultAdvisors(
                                                new SimpleLoggerAdvisor(),
                                                memoryAdvisor)
                                .build();
        }
}