package com.n1etzsch3.novi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.domain.po.UserAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户账户 Mapper
 * <p>
 * 用户账户的数据访问对象。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {
}
