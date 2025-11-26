package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 聊天记忆 Mapper
 * <p>
 * 聊天消息（历史记录）的数据访问对象。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Mapper
public interface ChatMemoryMapper {

    /**
     * 保存新的聊天消息。
     *
     * @param chatMessage 聊天消息实体。
     */
    void saveMessage(ChatMessage chatMessage);

    /**
     * 根据用户 ID 和会话 ID 查找聊天消息。
     * <p>
     * 结果按时间戳升序排列。
     * </p>
     *
     * @param userId    用户 ID。
     * @param sessionId 会话 ID。
     * @return 聊天消息列表。
     */
    List<ChatMessage> findByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 查找用户的不同会话 ID。
     *
     * @param userId 用户 ID。
     * @return 会话 ID 列表。
     */
    List<String> findSessionIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 和会话 ID 删除聊天消息。
     *
     * @param userId    用户 ID。
     * @param sessionId 会话 ID。
     */
    void deleteByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

}