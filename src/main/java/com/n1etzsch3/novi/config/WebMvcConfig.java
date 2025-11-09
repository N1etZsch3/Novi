package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                // 1. 拦截所有请求
                .addPathPatterns("/**") 
                
                // 2. 排除不需要认证的路径
                .excludePathPatterns(
                        "/api/v1/users/login", // 登录
                        "/api/v1/users/register",      // 注册
                        "/error"              // Spring Boot 默认错误页
                );
    }
}