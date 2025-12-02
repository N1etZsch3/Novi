package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.chat.repository.NoviDatabaseChatMemory;
import com.n1etzsch3.novi.aiconfig.model.DynamicChatModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天配置类
 * <p>
 * 配置ChatClient，支持持久化内存和动态模型切换。
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
        private final DynamicChatModel dynamicChatModel;

        /**
         * 创建 ChatClient bean，支持动态模型切换
         * <p>
         * 使用 DynamicChatModel 代理，确保每次调用都使用最新的模型配置。
         * </p>
         */
        @Bean
        public ChatClient chatClient() {
                log.info("Initializing ChatClient with DynamicChatModel");

                MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(noviDatabaseChatMemory)
                                .conversationId(ChatMemory.CONVERSATION_ID)
                                .build();

                log.info("ChatClient initialized successfully with dynamic model proxy");
                return ChatClient.builder(dynamicChatModel)
                                .defaultAdvisors(
                                                new SimpleLoggerAdvisor(),
                                                memoryAdvisor)
                                .build();
        }
}