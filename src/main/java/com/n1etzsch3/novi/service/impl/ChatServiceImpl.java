package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {


    @Override
    public ChatResponse handleMessage(Long userId, ChatRequest request) {
        return null;
    }
}
