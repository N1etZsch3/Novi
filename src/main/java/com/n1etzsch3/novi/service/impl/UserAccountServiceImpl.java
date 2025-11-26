package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.*;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.time.LocalDateTime;

/**
 * 用户账户服务实现类
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final com.n1etzsch3.novi.utils.JwtUtils jwtUtils;

    @Override
    public void registerUser(RegistrationRequest request) {
        // 1. 检查用户名是否已存在
        UserAccount existingUser = userAccountMapper
                .selectOne(new QueryWrapper<UserAccount>().eq("username", request.getUsername()));
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建新用户
        UserAccount newUser = UserAccount.builder()
                .username(request.getUsername())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 3. 保存用户
        userAccountMapper.insert(newUser);
        log.info("用户注册成功: {}", request.getUsername());
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Override
    public LoginRespond login(LoginRequest loginRequest) {
        // 1.通过用户名查找用户
        UserAccount userAccount = userAccountMapper
                .selectOne(new QueryWrapper<UserAccount>().eq("username", loginRequest.getUsername()));
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
     * 
     * @param userId 用户ID
     * @return 用户资料DTO
     */
    @Override
    public UserProfileDto getUserDetailsById(Long userId) {
        // 使用 selectById
        UserAccount userAccount = userAccountMapper.selectById(userId);
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
     * 动态更新用户资料 (支持部分更新 + 密码修改)
     */
    @Override
    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest req) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null)
            throw new BusinessException("用户不存在");

        // 1. 处理邮箱修改
        if (StringUtils.hasText(req.getEmail()) && !req.getEmail().equals(user.getEmail())) {
            UserAccount conflict = userAccountMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAccount>()
                            .eq(UserAccount::getEmail, req.getEmail())
                            .ne(UserAccount::getId, userId));
            if (conflict != null)
                throw new BusinessException("该邮箱已被其他用户注册");
            user.setEmail(req.getEmail());
        }

        // 2. 处理昵称修改
        if (StringUtils.hasText(req.getNickname())) {
            user.setNickname(req.getNickname());
        }

        // 3. 处理密码修改
        if (StringUtils.hasText(req.getNewPassword())) {
            // 必须提供旧密码进行验证
            if (!StringUtils.hasText(req.getCurrentPassword())) {
                throw new BusinessException("修改密码需要提供当前密码");
            }
            if (!passwordEncoder.matches(req.getCurrentPassword(), user.getHashedPassword())) {
                throw new BusinessException("当前密码错误");
            }
            // 加密新密码
            user.setHashedPassword(passwordEncoder.encode(req.getNewPassword()));
            log.info("用户 {} 修改了密码", userId);
        }

        // 4. 执行更新
        userAccountMapper.updateById(user);
    }

    private final ObjectMapper objectMapper;

    /**
     * 获取用户偏好
     */
    @Override
    public java.util.Map<String, Object> getUserPreferences(Long userId) {
        // 1. 使用 findById 获取完整的用户对象
        UserAccount userAccount = userAccountMapper.selectById(userId);

        // 2. 检查用户是否存在
        if (userAccount == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 从实体中获取 preferences (现在是 JSON 字符串)
        String preferencesString = userAccount.getPreferences();

        // 4. 如果字符串为空或null，返回空Map
        if (!StringUtils.hasText(preferencesString)) {
            log.info("用户 {} 尚无偏好设置，返回空Map", userId);
            return java.util.Collections.emptyMap();
        }

        // 5. 【新增】手动反序列化 JSON 字符串为 Map
        try {
            return objectMapper.readValue(
                    preferencesString,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {
                    });

        } catch (JsonProcessingException e) {
            log.error("JSON 反序列化用户偏好设置失败: {}", e.getMessage());
            // 即使反序列化失败，也应返回空Map而不是抛出异常
            return java.util.Collections.emptyMap();
        }
    }

    /**
     * 更新用户偏好
     */
    @Override
    public java.util.Map<String, Object> updateUserPreferences(Long userId, java.util.Map<String, Object> preferences) {
        if (preferences == null) {
            throw new BusinessException("偏好设置不能为空");
        }

        try {
            // 4. 在 Service 层手动序列化为 JSON 字符串
            String preferencesString = objectMapper.writeValueAsString(preferences);

            // 5. 将序列化后的字符串传递给 Mapper
            userAccountMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserAccount>()
                            .eq(UserAccount::getId, userId)
                            .set(UserAccount::getPreferences, preferencesString)
                            .set(UserAccount::getUpdatedAt, LocalDateTime.now()));

            log.info("成功更新用户 {} 的偏好设置", userId);
            return preferences;

        } catch (JsonProcessingException e) {
            // 6. 如果序列化失败，抛出业务异常
            log.error("JSON 序列化偏好设置失败: {}", e.getMessage());
            throw new BusinessException("偏好设置格式无效");
        }
    }

}
