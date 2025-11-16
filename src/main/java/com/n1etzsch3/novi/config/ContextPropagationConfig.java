package com.n1etzsch3.novi.config;

import com.n1etzsch3.novi.utils.UserIdThreadLocalAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置上下文传播 (Context Propagation)
 * 这对应研究报告中的“方案 B”。
 */
@Configuration
public class ContextPropagationConfig {

    /**
     * 将 UserIdThreadLocalAccessor 注册为 Spring Bean。
     * Spring Boot (3+) 会自动检测到这个 Bean，
     * 并使用它在响应式流的线程切换时自动传播 LoginUserContext。
     */
    @Bean
    public UserIdThreadLocalAccessor userIdThreadLocalAccessor() {
        return new UserIdThreadLocalAccessor();
    }
}