package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.LoginRequest;
import com.n1etzsch3.novi.pojo.dto.LoginRespond;
import com.n1etzsch3.novi.pojo.dto.UserProfileDto;
import com.n1etzsch3.novi.pojo.dto.registrationRequest;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.service.UserAccountService;
import com.n1etzsch3.novi.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
     * 根据用户名获取用户详情
     * @param username 用户名
     * @return 用户资料DTO
     */
    @Override
    public UserProfileDto getUserDetailsByUsername(String username) {
        UserAccount userAccount = userAccountMapper.findByUsername(username);
        if (userAccount == null) {
            throw new BusinessException("用户不存在");
        }

        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setId(Math.toIntExact(userAccount.getId()));
        userProfileDto.setUsername(userAccount.getUsername());
        userProfileDto.setNickname(userAccount.getNickname());
        userProfileDto.setEmail(userAccount.getEmail());
        userProfileDto.setCreatedAt(userAccount.getCreatedAt());
        userProfileDto.setUpdatedAt(userAccount.getUpdatedAt());

        log.info("获取用户详情: {}", username);
        return userProfileDto;
    }
}
