package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.UserAccount;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

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

    /**
     * 通过用户ID查询用户
     */
    UserAccount findById(Long id);

    /**
     * 更新用户信息
     */
    void updateUser(UserAccount userAccount);

    /**
     * 根据用户ID查询偏好设置
     */
    Map<String, Object> findPreferencesById(Long userId);

    /**
     * 更新用户偏好设置
     */
    void updatePreferences(@Param("userId") Long userId, @Param("preferences") Map<String, Object> preferences);

}
