package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMemoryMapper {

    /**
     * 插入一条新的聊天消息
     *
     * @param chatMessage 聊天消息实体
     * @Note 在对应的 XML 文件中，您需要通过 #{user.id} 来获取
     * chatMessage 对象中嵌套的 UserAccount 对象的 ID。
     */
    void saveMessage(ChatMessage chatMessage);

    /**
     * 根据用户ID和会话ID查询聊天记录
     * (按照时间戳升序排列)
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 聊天消息列表
     */
    List<ChatMessage> findByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 根据用户ID查询该用户所有的会话ID（去重）
     *
     * @param userId 用户ID
     * @return 该用户的所有会话ID列表
     */
    List<String> findSessionIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和会话ID删除聊天记录
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void deleteByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

}