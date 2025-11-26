package com.n1etzsch3.novi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 查找用户的所有会话（按更新时间倒序）。
     *
     * @param userId 用户 ID。
     * @return 会话列表。
     */
    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY updated_at DESC")
    List<ChatSession> findByUserId(Long userId);

    /**
     * 更新会话的最后活跃时间。
     *
     * @param sessionId 会话 ID。
     * @return 影响的行数。
     */
    @Update("UPDATE chat_session SET updated_at = NOW() WHERE id = #{sessionId}")
    int updateLastActiveTime(String sessionId);

    /**
     * 统计指定用户和会话 ID 的数量（用于检查所有权）。
     *
     * @param sessionId 会话 ID。
     * @param userId    用户 ID。
     * @return 数量。
     */
    @Select("SELECT COUNT(*) FROM chat_session WHERE id = #{sessionId} AND user_id = #{userId}")
    int countByUserIdAndSessionId(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    /**
     * 软删除会话。
     *
     * @param sessionId 会话 ID。
     * @param userId    用户 ID。
     */
    @Update("UPDATE chat_session SET is_deleted = 1 WHERE id = #{sessionId} AND user_id = #{userId}")
    void softDeleteSession(@Param("sessionId") String sessionId, @Param("userId") Long userId);
}
