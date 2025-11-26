package com.n1etzsch3.novi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天记忆 Mapper
 * <p>
 * 聊天记忆的数据访问对象。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMessage> {

    /**
     * 根据用户 ID 和会话 ID 查找消息记录。
     *
     * @param userId    用户 ID。
     * @param sessionId 会话 ID。
     * @return 消息列表。
     */
    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} AND session_id = #{sessionId} ORDER BY id ASC")
    List<ChatMessage> findByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 删除指定用户和会话的所有消息。
     *
     * @param userId    用户 ID。
     * @param sessionId 会话 ID。
     */
    @Delete("DELETE FROM chat_message WHERE user_id = #{userId} AND session_id = #{sessionId}")
    void deleteByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 查找用户的所有会话 ID。
     *
     * @param userId 用户 ID。
     * @return 会话 ID 列表。
     */
    @Select("SELECT DISTINCT session_id FROM chat_message WHERE user_id = #{userId}")
    List<String> findSessionIdsByUserId(@Param("userId") Long userId);
}