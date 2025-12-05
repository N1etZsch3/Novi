package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.aiconfig.model.DynamicChatModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AllArgsConstructor
public class ChatConfig {

        private final DynamicChatModel dynamicChatModel;

        /**
         * 创建 ChatClient bean，支持动态模型切换
         * <p>
         * 使用 DynamicChatModel 代理，确保每次调用都使用最新的模型配置。
         * conversationId() 配置告诉 advisor 从 context 的哪个参数读取 conversationId 值。
         * ChatServiceImpl 通过 .param(ChatMemory.CONVERSATION_ID, compositeKey) 传入实际的复合键。
         * </p>
         */
        @Bean
        public ChatClient chatClient() {
                log.info("Initializing ChatClient with DynamicChatModel");

                log.info("ChatClient initialized successfully with dynamic model proxy");
                return ChatClient.builder(dynamicChatModel)
                                .defaultAdvisors(
                                                new SimpleLoggerAdvisor())
                                .build();
        }
}