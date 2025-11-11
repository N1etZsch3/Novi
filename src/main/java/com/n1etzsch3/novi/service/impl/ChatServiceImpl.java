package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;


    @Override
    public ChatResponse handleMessage(Long userId, ChatRequest request) {

        String userMessage = request.getMessage();

        String AIResponse = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        return new ChatResponse(AIResponse, request.getSessionId());
    }
}
