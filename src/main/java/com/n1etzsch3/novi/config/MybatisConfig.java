package com.n1etzsch3.novi.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.n1etzsch3.novi.mapper")
public class MybatisConfig {
}
