package com.n1etzsch3.novi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.pojo.dto.LoginRequest;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.pojo.dto.registrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


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
        mockMvc.perform(post("/api/v1/users/register")
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
                .andExpect(status().isOk()) // 假设G.E.H.返回200
                .andExpect(jsonPath("$.code").value(0)) // 期望 Result.code == 0
                .andExpect(jsonPath("$.msg").value("用户名已存在")); // 期望是 Service 抛出的错误信息
    }

    /**
     * 辅助方法：注册一个用于登录测试的用户
     */
    private void registerUserForLoginTest() throws Exception {
        registrationRequest request = new registrationRequest();
        request.setUsername("login_user");
        request.setPassword("LoginPass123!");
        request.setEmail("login@example.com");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    /**
     * 测试成功登录
     */
    @Test
    public void testLoginSuccess() throws Exception {
        // 1. 先注册一个用户
        registerUserForLoginTest();

        // 2. 准备登录请求
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("login_user");
        loginRequest.setPassword("LoginPass123!");

        // 3. 模拟登录
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                // 4. 验证响应
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.userId").exists()) // 检查 userId 是否存在
                .andExpect(jsonPath("$.data.token").exists()); // 检查 token 是否存在
    }

    /**
     * 测试密码错误
     */
    @Test
    public void testLoginFailsWrongPassword() throws Exception {
        // 1. 先注册一个用户
        registerUserForLoginTest();

        // 2. 准备登录请求 (密码错误)
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("login_user");
        loginRequest.setPassword("WrongPassword!"); // 错误的密码

        // 3. 模拟登录
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                // 4. 验证业务异常
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    /**
     * 测试用户不存在
     */
    @Test
    public void testLoginFailsUserNotFound() throws Exception {
        // (不需要注册用户)

        // 1. 准备登录请求 (用户不存在)
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("non_existing_user");
        loginRequest.setPassword("AnyPassword");

        // 2. 模拟登录
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                // 3. 验证业务异常
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    /**
     * 测试获取当前用户资料 (需要鉴权)
     */
    @Test
    public void testGetCurrentUserProfileSuccess() throws Exception {
        // --- 1. 注册一个新用户 ---
        registrationRequest request = new registrationRequest();
        request.setUsername("profile_user");
        request.setPassword("ProfilePass123!");
        request.setEmail("profile@example.com");
        request.setNickname("ProfileTester");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // --- 2. 登录该用户以获取 Token ---
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("profile_user");
        loginRequest.setPassword("ProfilePass123!");

        String responseString = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn().getResponse().getContentAsString();

        // 2.1 解析响应，获取 Token
        // (注意：这里我们假设你已经创建了 LoginRespond.java)
        Result result = objectMapper.readValue(responseString, Result.class);
        // ObjectMapper 会将 data 转为 LinkedHashMap，我们需要手动转一下
        String token = (String) ((java.util.LinkedHashMap) result.getData()).get("token");


        // --- 3. 携带 Token 访问 /me 接口 ---
        mockMvc.perform(get("/api/v1/users/me") // 使用 GET
                        .header("Authorization", "Bearer " + token)) // 必须携带 Token

                // --- 4. 验证响应 ---
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("profile_user"))
                .andExpect(jsonPath("$.data.email").value("profile@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("ProfileTester"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    /**
     * 测试未携带 Token 访问受保护接口
     */
    @Test
    public void testGetCurrentUserProfileFailsNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")) // 不带 Header
                // 期望 401 Unauthorized (由 JwtAuthInterceptor 返回)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("INVALID_TOKEN"));
    }

    /**
     * 辅助方法：注册并登录一个用户，返回 Token
     */
    private String getAuthTokenForTest(String username, String password) throws Exception {
        // 1. 注册
        registrationRequest request = new registrationRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setEmail(username + "@example.com");
        request.setNickname("Test " + username);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 2. 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String responseString = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 3. 解析 Token
        // (注意: Result.data 会被 Jackson 解析为 LinkedHashMap)
        Map<String, Object> data = (Map<String, Object>) objectMapper.readValue(responseString, Result.class).getData();
        return (String) data.get("token");
    }

    /**
     * 测试：成功更新和获取用户偏好
     */
    @Test
    public void testUpdateAndGetPreferencesSuccess() throws Exception {
        // 1. 获取一个有效 Token
        String token = getAuthTokenForTest("pref_user", "PrefPass123!");

        // 2. 检查初始偏好 (应为空)
        mockMvc.perform(get("/api/v1/users/me/preferences")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isEmpty()); // 期望是一个空Map {}

        // 3. 准备新的偏好数据
        Map<String, Object> newPreferences = new HashMap<>();
        newPreferences.put("personality", "witty");
        newPreferences.put("voice", "female_A");
        newPreferences.put("response_length", "concise");

        // 4. 发送 PUT 请求更新偏好
        mockMvc.perform(put("/api/v1/users/me/preferences") // 使用 PUT
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPreferences)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.personality").value("witty"));

        // 5. 再次 GET 请求，验证数据是否已持久化
        mockMvc.perform(get("/api/v1/users/me/preferences")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.personality").value("witty"))
                .andExpect(jsonPath("$.data.voice").value("female_A"));
    }

    /**
     * 测试：未授权访问偏好接口
     */
    @Test
    public void testPreferencesEndpointsFailNoToken() throws Exception {
        // 1. 测试 GET
        mockMvc.perform(get("/api/v1/users/me/preferences"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("INVALID_TOKEN"));

        // 2. 测试 PUT
        Map<String, Object> prefs = Map.of("personality", "witty");
        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefs)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("INVALID_TOKEN"));
    }


}