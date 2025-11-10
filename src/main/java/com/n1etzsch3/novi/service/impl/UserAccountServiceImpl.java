package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.*;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.service.UserAccountService;
import com.n1etzsch3.novi.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    UserAccountMapper userAccountMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * 用户注册
     * @param registrationRequest 用户注册请求
     */
    @Override
    public void registerUser(registrationRequest registrationRequest) {

        // 1.检查用户名是否存在
        UserAccount existingUser = userAccountMapper.findByUsername(registrationRequest.getUsername());
        if (existingUser != null) {
            // 抛出一个运行时异常，而不是返回SQL错误
            throw new BusinessException("用户名已存在");
        }

        // 2.检查邮箱是否存在
        UserAccount existingEmailUser = userAccountMapper.findByEmail(registrationRequest.getEmail());
        if (existingEmailUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 3.创建新用户
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(registrationRequest.getUsername());
        userAccount.setHashedPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        userAccount.setNickname(registrationRequest.getNickname());
        userAccount.setEmail(registrationRequest.getEmail());
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setUpdatedAt(LocalDateTime.now());

        userAccountMapper.addUser(userAccount);

        log.info("新用户注册成功: {}", userAccount.getUsername());
    }

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Override
    public LoginRespond login(LoginRequest loginRequest) {
        // 1.通过用户名查找用户
        UserAccount userAccount = userAccountMapper.findByUsername(loginRequest.getUsername());
        if (userAccount == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2.验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), userAccount.getHashedPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3.生成JWT令牌
        String token = jwtUtils.generateToken(userAccount.getId(), userAccount.getUsername());

        // 4.构建登录响应
        LoginRespond loginRespond = new LoginRespond();
        loginRespond.setUserId(Math.toIntExact(userAccount.getId()));
        loginRespond.setToken(token);

        log.info("用户登录成功: {}", userAccount.getUsername());
        return loginRespond;
    }

    /**
     * 根据用户ID获取用户详情
     * @param userId 用户ID
     * @return 用户资料DTO
     */
    @Override
    public UserProfileDto getUserDetailsById(Long userId) {
        // 使用 findById
        UserAccount userAccount = userAccountMapper.findById(userId);
        if (userAccount == null) {
            // 这种情况理论上不应发生，因为Token是有效的
            throw new BusinessException("用户不存在");
        }

        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setId(Math.toIntExact(userAccount.getId()));
        userProfileDto.setUsername(userAccount.getUsername());
        userProfileDto.setNickname(userAccount.getNickname());
        userProfileDto.setEmail(userAccount.getEmail());
        userProfileDto.setCreatedAt(userAccount.getCreatedAt());
        userProfileDto.setUpdatedAt(userAccount.getUpdatedAt());

        log.info("获取用户详情: {}", userAccount.getUsername());
        return userProfileDto;
    }

    /**
     * 更新用户资料
     * @param userId 用户ID
     * @param updateRequest 更新请求
     */
    @Override
    public void updateUserProfile(Long userId, UserProfileUpdateRequest updateRequest) {
        // 1.通过id查找用户
        UserAccount userAccount = userAccountMapper.findById(userId);
        if (updateRequest.getNickname() == null) {
            throw new BusinessException("用户不存在");
        }

        // 2.更新用户资料
        userAccount.setNickname(updateRequest.getNickname());
        userAccount.setEmail(updateRequest.getEmail());
        userAccount.setUpdatedAt(LocalDateTime.now());

        // 3.保存更新
        userAccountMapper.updateUser(userAccount);

    }

    /**
     * 获取用户偏好
     */
    @Override
    public Map<String, Object> getUserPreferences(Long userId) {
        Map<String, Object> preferences = userAccountMapper.findPreferencesById(userId);

        // 如果用户从未设置过偏好，数据库可能返回 null
        if (preferences == null) {
            log.info("用户 {} 尚无偏好设置，返回空Map", userId);
            return Collections.emptyMap(); // 返回一个空Map，而不是 null
        }

        log.info("成功获取用户 {} 的偏好设置", userId);
        return preferences;
    }

    /**
     * 更新用户偏好
     */
    @Override
    public Map<String, Object> updateUserPreferences(Long userId, Map<String, Object> preferences) {
        if (preferences == null) {
            // 防止客户端发送 null，导致 JSON 字段被清空
            throw new BusinessException("偏好设置不能为空");
        }

        userAccountMapper.updatePreferences(userId, preferences);
        log.info("成功更新用户 {} 的偏好设置", userId);

        // 按照API文档，返回更新后的偏好
        return preferences;
    }
}
