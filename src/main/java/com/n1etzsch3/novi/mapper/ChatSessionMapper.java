package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 聊天会话 Mapper
 * <p>
 * 聊天会话的数据访问对象。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Mapper
public interface ChatSessionMapper {

    /**
     * 查找用户的所有聊天会话。
     *
     * @param userId 用户 ID。
     * @return 聊天会话列表。
     */
    List<ChatSession> findByUserId(Long userId);

    /**
     * 创建新的聊天会话。
     *
     * @param session 聊天会话实体。
     */
    void createSession(ChatSession session);

    /**
     * 更新会话的最后活跃时间。
     *
     * @param finalSessionId 会话 ID。
     * @return 受影响的行数。
     */
    int updateLastActiveTime(String finalSessionId);

    /**
     * 统计用户 ID 和会话 ID 的会话数量。
     *
     * @param userId    用户 ID。
     * @param sessionId 会话 ID。
     * @return 匹配的会话数量。
     */
    int countByUserIdAndSessionId(Long userId, String sessionId);

    /**
     * 软删除会话（标记为已删除）。
     *
     * @param sessionId 会话 ID。
     * @param userId    用户 ID（用于所有权验证）。
     */
    void softDeleteSession(String sessionId, Long userId);
}
