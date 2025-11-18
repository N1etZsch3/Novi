package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    List<ChatSession> findByUserId(Long userId);

    void createSession(ChatSession session);

    int updateLastActiveTime(String finalSessionId);

}
