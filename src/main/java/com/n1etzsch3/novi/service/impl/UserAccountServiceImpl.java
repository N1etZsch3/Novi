package com.n1etzsch3.novi.service.impl;

import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.registrationRequest;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.service.UserAccountService;
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
}
