package com.n1etzsch3.novi.mapper;

import com.n1etzsch3.novi.pojo.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

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
public interface UserAccountMapper {

    /**
     * 添加新用户。
     *
     * @param userAccount 用户账户实体。
     */
    void addUser(UserAccount userAccount);

    /**
     * 根据用户名查找用户。
     *
     * @param username 用户名。
     * @return 用户账户实体，如果未找到则返回 null。
     */
    UserAccount findByUsername(String username);

    /**
     * 根据电子邮件查找用户。
     *
     * @param email 电子邮件地址。
     * @return 用户账户实体，如果未找到则返回 null。
     */
    UserAccount findByEmail(String email);

    /**
     * 根据 ID 查找用户。
     *
     * @param id 用户 ID。
     * @return 用户账户实体，如果未找到则返回 null。
     */
    UserAccount findById(Long id);

    /**
     * 根据电子邮件查找用户，排除特定 ID。
     * <p>
     * 用于在更新个人资料时检查电子邮件的唯一性。
     * </p>
     *
     * @param email 电子邮件地址。
     * @param id    要排除的用户 ID。
     * @return 用户账户实体，如果未找到则返回 null。
     */
    UserAccount findByEmailAndNotId(@Param("email") String email, @Param("id") Long id);

    /**
     * 更新用户信息。
     *
     * @param userAccount 包含更新值的用户账户实体。
     */
    void updateUser(UserAccount userAccount);

    /**
     * 查找用户偏好设置（JSON 字符串）。
     *
     * @param userId 用户 ID。
     * @return 偏好设置 JSON 字符串。
     */
    String findPreferencesJsonById(Long userId);

    /**
     * 更新用户偏好设置。
     *
     * @param userId      用户 ID。
     * @param preferences 新的偏好设置 JSON 字符串。
     */
    void updatePreferences(@Param("userId") Long userId, @Param("preferences") String preferences);
}
