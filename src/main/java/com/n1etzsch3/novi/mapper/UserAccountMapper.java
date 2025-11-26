package com.n1etzsch3.novi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("SELECT * FROM user_account WHERE email = #{email} AND id != #{id}")
    UserAccount findByEmailAndNotId(@Param("email") String email, @Param("id") Long id);

    /**
     * 查找用户偏好设置（JSON 字符串）。
     *
     * @param userId 用户 ID。
     * @return 偏好设置 JSON 字符串。
     */
    @Select("SELECT preferences FROM user_account WHERE id = #{userId}")
    String findPreferencesJsonById(Long userId);

    /**
     * 更新用户偏好设置。
     *
     * @param userId      用户 ID。
     * @param preferences 新的偏好设置 JSON 字符串。
     */
    @Update("UPDATE user_account SET preferences = #{preferences}, updated_at = NOW() WHERE id = #{userId}")
    void updatePreferences(@Param("userId") Long userId, @Param("preferences") String preferences);
}
