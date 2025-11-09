package com.n1etzsch3.novi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.pojo.dto.registrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest // 1. 这是一个完整的 Spring Boot 测试
@AutoConfigureMockMvc // 2. 自动配置 MockMvc，用于模拟 HTTP 请求
@Transactional // 3. 【重要】测试后回滚事务，防止污染数据库
public class UserAccountControllerTest {

    @Autowired
    private MockMvc mockMvc; // 4. 模拟请求的工具

    @Autowired
    private ObjectMapper objectMapper; // 5. 用于将 Java 对象转为 JSON 字符串

    /**
     * 测试成功注册
     */
    @Test
    public void testRegisterSuccess() throws Exception {
        // 1. 准备一个 DTO
        registrationRequest request = new registrationRequest();
        request.setUsername("test_user_01"); // 确保这个用户名不存在
        request.setPassword("Password123!");
        request.setEmail("test01@example.com");
        request.setNickname("Tester");

        // 2. 模拟 POST 请求
        mockMvc.perform(post("/api/v1/users/register") // 你的 Controller 路径
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))) // 3. 将 DTO 转为 JSON 作为请求体
                
                // 4. 验证响应
                .andExpect(status().isOk()) // 期望 HTTP 状态码 200
                .andExpect(jsonPath("$.code").value(1)) // 期望 Result.code == 1
                .andExpect(jsonPath("$.msg").value("success")); // 期望 Result.msg == "success"
    }

    /**
     * 测试重复注册失败
     */
    @Test
    public void testRegisterFailsOnDuplicateUsername() throws Exception {
        // --- 第一次注册 (确保用户存在) ---
        registrationRequest request1 = new registrationRequest();
        request1.setUsername("duplicate_user");
        request1.setPassword("Password123!");
        request1.setEmail("duplicate@example.com");
        
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk()) // 第一次应该成功
                .andExpect(jsonPath("$.code").value(1));

        // --- 第二次注册 (使用相同的用户名) ---
        registrationRequest request2 = new registrationRequest();
        request2.setUsername("duplicate_user"); // 相同的用户名
        request2.setPassword("AnotherPass!");
        request2.setEmail("another@example.com");

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))

                // 期望被 GlobalExceptionHandler 捕获并返回错误
                .andExpect(status().isOk()) // 假设你的G.E.H.返回200
                .andExpect(jsonPath("$.code").value(0)) // 期望 Result.code == 0
                .andExpect(jsonPath("$.msg").value("用户名已存在")); // 期望是 Service 抛出的错误信息
    }
}