package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.repository.NoviDatabaseChatMemory;
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
        private final DynamicChatModelFactory dynamicChatModelFactory;

        /**
         * 创建 ChatClient bean，支持动态模型切换
         * <p>
         * 通过 DynamicChatModelFactory 从数据库读取当前激活的模型配置。
         * ChatClient会在每次调用时通过工厂获取最新的模型配置。
         * </p>
         */
        @Bean
        public ChatClient chatClient() {
                log.info("Initializing ChatClient with dynamic model configuration");

                MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(noviDatabaseChatMemory)
                                .conversationId(ChatMemory.CONVERSATION_ID)
                                .build();

                log.info("ChatClient initialized successfully with dynamic model");
                return ChatClient.builder(dynamicChatModelFactory.createChatModel())
                                .defaultAdvisors(
                                                new SimpleLoggerAdvisor(),
                                                memoryAdvisor)
                                .build();
        }
}