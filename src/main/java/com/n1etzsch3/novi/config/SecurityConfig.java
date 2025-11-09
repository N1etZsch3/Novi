package com.n1etzsch3.novi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * 将 PasswordEncoder 注册为 Spring Bean
     * * @Bean 告诉 Spring, 这个方法的返回值是一个需要被 Spring 容器管理的 Bean
     * * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 返回 BCryptPasswordEncoder 实例
        // 这是一个基于 BCrypt 算法的 PasswordEncoder 实现
        return new BCryptPasswordEncoder();
    }
}