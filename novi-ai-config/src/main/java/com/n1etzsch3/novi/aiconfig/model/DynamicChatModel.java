package com.n1etzsch3.novi.aiconfig.model;

import com.n1etzsch3.novi.aiconfig.factory.DynamicChatModelFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 动态 ChatModel 代理
 * <p>
 * 代理所有调用到当前激活的 ChatModel 实例。
 * 解决 ChatClient 持有旧 ChatModel 实例导致无法切换模型的问题。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicChatModel implements ChatModel {

    private final DynamicChatModelFactory dynamicChatModelFactory;

    @Override
    public ChatResponse call(Prompt prompt) {
        return dynamicChatModelFactory.createChatModel().call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return dynamicChatModelFactory.createChatModel().stream(prompt);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return dynamicChatModelFactory.createChatModel().getDefaultOptions();
    }
}
