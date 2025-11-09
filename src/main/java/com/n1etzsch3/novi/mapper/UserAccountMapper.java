package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountMapper {

    /**
     * 添加用户
     * @param userAccount 用户实体类
     */
    void addUser(UserAccount userAccount);

    /**
     * 通过用户名查询用户
     */
    UserAccount findByUsername(String username);

    /**
     * 通过邮箱查询用户
     */
    UserAccount findByEmail(String email);
}
