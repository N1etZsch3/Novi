package com.n1etzsch3.novi.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.common.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

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
}