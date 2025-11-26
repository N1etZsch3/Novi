package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.repository.NoviDatabaseChatMemory;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ChatConfig {

    private final NoviDatabaseChatMemory noviDatabaseChatMemory;

    /**
     * 创建 ChatClient bean, 现已配置持久化内存
     * @param openAiChatModel 自动配置的 AI 模型
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {

        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(noviDatabaseChatMemory)
                .conversationId(ChatMemory.CONVERSATION_ID)
                .build();


        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        memoryAdvisor
                )
                .build();
    }

}