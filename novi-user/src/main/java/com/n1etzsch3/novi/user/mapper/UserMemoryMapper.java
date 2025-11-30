package com.n1etzsch3.novi.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.common.pojo.entity.UserMemory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户记忆 Mapper
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Mapper
public interface UserMemoryMapper extends BaseMapper<UserMemory> {
}
