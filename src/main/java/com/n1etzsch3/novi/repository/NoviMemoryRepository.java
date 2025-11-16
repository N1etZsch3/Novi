package com.n1etzsch3.novi.repository;

import com.n1etzsch3.novi.mapper.ChatMemoryMapper;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class NoviMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryMapper chatMemoryMapper;

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return List.of();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {

    }

    @Override
    public void deleteByConversationId(String conversationId) {

    }
}
