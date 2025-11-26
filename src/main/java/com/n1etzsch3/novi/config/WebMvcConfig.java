package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                // 1. 拦截所有请求
                .addPathPatterns("/**")

                // 2. 排除不需要认证的路径
                .excludePathPatterns(
                        "/api/v1/users/login", // 登录
                        "/api/v1/users/register", // 注册
                        "/error", // Spring Boot 默认错误页
                        "/index.html",
                        "/api/prompt/config/**" // Exclude prompt config for testing/admin
                );
    }

    /**
     * 处理跨域问题
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(false) // 不允许携带凭证
                .maxAge(3600); // 预检请求的缓存时间
    }

}