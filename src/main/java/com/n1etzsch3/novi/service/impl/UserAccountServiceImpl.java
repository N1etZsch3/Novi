package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.*;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.service.UserAccountService;
import com.n1etzsch3.novi.utils.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {


    private final UserAccountMapper userAccountMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final ObjectMapper objectMapper;

    /**
     * 用户注册
     * @param registrationRequest 用户注册请求
     */
    @Override
    public void registerUser(RegistrationRequest registrationRequest) {

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
        // 1. 通过id查找用户
        UserAccount userAccount = userAccountMapper.findById(userId);
        if (userAccount == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查Email冲突
        // 只有当Email被修改 且 与原Email不同时，才检查冲突
        if (StringUtils.hasText(updateRequest.getEmail()) && !updateRequest.getEmail().equals(userAccount.getEmail())) {
            UserAccount emailConflict = userAccountMapper.findByEmailAndNotId(updateRequest.getEmail(), userId);
            if (emailConflict != null) {
                throw new BusinessException("该邮箱已被其他用户注册");
            }
            userAccount.setEmail(updateRequest.getEmail());
        }

        // 3. 更新昵称
        userAccount.setNickname(updateRequest.getNickname());

        // 4. 更新时间戳
        userAccount.setUpdatedAt(LocalDateTime.now());

        // 5. 保存更新
        userAccountMapper.updateUser(userAccount);
        log.info("用户资料更新成功: {}", userAccount.getUsername());
    }

    /**
     * 获取用户偏好
     */
    @Override
    public Map<String, Object> getUserPreferences(Long userId) {
        // 1. 使用 findById 获取完整的用户对象
        UserAccount userAccount = userAccountMapper.findById(userId);

        // 2. 检查用户是否存在
        if (userAccount == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 从实体中获取 preferences (现在是 JSON 字符串)
        String preferencesString = userAccount.getPreferences();

        // 4. 如果字符串为空或null，返回空Map
        if (!StringUtils.hasText(preferencesString)) {
            log.info("用户 {} 尚无偏好设置，返回空Map", userId);
            return Collections.emptyMap();
        }

        // 5. 【新增】手动反序列化 JSON 字符串为 Map
        try {
            Map<String, Object> preferencesMap = objectMapper.readValue(
                    preferencesString,
                    new TypeReference<Map<String, Object>>() {}
            );

            log.info("成功获取并反序列化用户 {} 的偏好设置", userId);
            return preferencesMap;

        } catch (JsonProcessingException e) {
            log.error("JSON 反序列化用户偏好设置失败: {}", e.getMessage());
            // 即使反序列化失败，也应返回空Map而不是抛出异常
            return Collections.emptyMap();
        }
    }

    /**
     * 更新用户偏好
     */
    @Override
    public Map<String, Object> updateUserPreferences(Long userId, Map<String, Object> preferences) {
        if (preferences == null) {
            throw new BusinessException("偏好设置不能为空");
        }

        try {
            // 4. 在 Service 层手动序列化为 JSON 字符串
            String preferencesString = objectMapper.writeValueAsString(preferences);

            // 5. 将序列化后的字符串传递给 Mapper (这部分代码无需更改)
            userAccountMapper.updatePreferences(userId, preferencesString);

            log.info("成功更新用户 {} 的偏好设置", userId);
            return preferences;

        } catch (JsonProcessingException e) {
            // 6. 如果序列化失败，抛出业务异常
            log.error("JSON 序列化偏好设置失败: {}", e.getMessage());
            throw new BusinessException("偏好设置格式无效");
        }
    }

}
