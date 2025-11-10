package com.n1etzsch3.novi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.pojo.dto.LoginRequest;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.pojo.dto.UserProfileUpdateRequest;
import com.n1etzsch3.novi.pojo.dto.registrationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("用户账户控制器 (UserAccountController) 测试")
public class UserAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 辅助方法：用于注册和登录 ---
    private String registerAndLogin(String user, String pass, String email, String nickname) throws Exception {
        // 1. 注册
        registrationRequest regRequest = new registrationRequest();
        regRequest.setUsername(user);
        regRequest.setPassword(pass);
        regRequest.setEmail(email);
        regRequest.setNickname(nickname);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 2. 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(user);
        loginRequest.setPassword(pass);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        // 3. 解析 Token
        String responseString = loginResult.getResponse().getContentAsString();
        Map<String, Object> data = (Map<String, Object>) objectMapper.readValue(responseString, Result.class).getData();
        return (String) data.get("token");
    }

    // --- 1. 注册功能测试 ---
    @Nested
    @DisplayName("1. 用户注册 (/register)")
    class RegistrationTests {

        @Test
        @DisplayName("成功注册")
        public void testRegisterSuccess() throws Exception {
            registrationRequest request = new registrationRequest();
            request.setUsername("test_user_01");
            request.setPassword("ValidPass123!");
            request.setEmail("test01@example.com");
            request.setNickname("Tester");

            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.msg").value("success"));
        }

        @Test
        @DisplayName("失败：用户名已存在")
        public void testRegisterFailsOnDuplicateUsername() throws Exception {
            // 第一次注册 (成功)
            registerAndLogin("duplicate_user", "ValidPass123!", "duplicate@example.com", "Dup");

            // 第二次注册 (失败)
            registrationRequest request2 = new registrationRequest();
            request2.setUsername("duplicate_user"); // 相同的用户名
            request2.setPassword("AnotherPass123!");
            request2.setEmail("another@example.com");

            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("用户名已存在"));
        }

        @Test
        @DisplayName("失败：邮箱已被注册")
        public void testRegisterFailsOnDuplicateEmail() throws Exception {
            // 第一次注册 (成功)
            registerAndLogin("user1", "ValidPass123!", "email_conflict@example.com", "User1");

            // 第二次注册 (失败)
            registrationRequest request2 = new registrationRequest();
            request2.setUsername("user2");
            request2.setPassword("AnotherPass123!");
            request2.setEmail("email_conflict@example.com"); // 相同的邮箱

            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("邮箱已被注册"));
        }

        @Test
        @DisplayName("失败：输入校验 - 密码过弱")
        public void testRegisterFailsWeakPassword() throws Exception {
            registrationRequest request = new registrationRequest();
            request.setUsername("weak_pass_user");
            request.setPassword("12345678"); // 密码过弱
            request.setEmail("weak@example.com");

            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("密码必须至少包含一个大写字母、一个小写字母、一个数字和一个特殊字符"));
        }

        @Test
        @DisplayName("失败：输入校验 - 邮箱格式错误")
        public void testRegisterFailsInvalidEmail() throws Exception {
            registrationRequest request = new registrationRequest();
            request.setUsername("invalid_email_user");
            request.setPassword("ValidPass123!");
            request.setEmail("not-an-email"); // 格式错误

            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("邮箱格式不正确"));
        }
    }

    // --- 2. 登录功能测试 ---
    @Nested
    @DisplayName("2. 用户登录 (/login)")
    class LoginTests {

        @Test
        @DisplayName("成功登录")
        public void testLoginSuccess() throws Exception {
            // 1. 先注册一个用户
            registerAndLogin("login_user", "LoginPass123!", "login@example.com", "LoginUser");

            // 2. 准备登录请求
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("login_user");
            loginRequest.setPassword("LoginPass123!");

            // 3. 模拟登录
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.token").exists());
        }

        @Test
        @DisplayName("失败：密码错误")
        public void testLoginFailsWrongPassword() throws Exception {
            registerAndLogin("login_user_pass", "LoginPass123!", "login_pass@example.com", "LoginUser");

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("login_user_pass");
            loginRequest.setPassword("WrongPassword!"); // 错误的密码

            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
        }

        @Test
        @DisplayName("失败：用户不存在")
        public void testLoginFailsUserNotFound() throws Exception {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("non_existing_user");
            loginRequest.setPassword("AnyPassword");

            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
        }
    }

    // --- 3. 用户资料测试 ---
    @Nested
    @DisplayName("3. 用户资料 (/me)")
    class ProfileTests {

        @Test
        @DisplayName("成功获取当前用户资料")
        public void testGetCurrentUserProfileSuccess() throws Exception {
            String token = registerAndLogin("profile_user", "ProfilePass123!", "profile@example.com", "ProfileTester");

            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.username").value("profile_user"))
                    .andExpect(jsonPath("$.data.email").value("profile@example.com"))
                    .andExpect(jsonPath("$.data.nickname").value("ProfileTester"))
                    .andExpect(jsonPath("$.data.createdAt").exists()); // 验证上次的bug
        }

        @Test
        @DisplayName("失败：未携带Token获取资料")
        public void testGetCurrentUserProfileFailsNoToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")) // 不带 Header
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("INVALID_TOKEN"));
        }

        @Test
        @DisplayName("成功更新用户资料")
        public void testUpdateUserProfileSuccess() throws Exception {
            String token = registerAndLogin("update_user", "UpdatePass123!", "update@example.com", "Updater");

            UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
            updateRequest.setNickname("UpdatedNickname");
            updateRequest.setEmail("new_email@example.com");

            mockMvc.perform(put("/api/v1/users/me")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("UpdatedNickname"))
                    .andExpect(jsonPath("$.data.email").value("new_email@example.com"));
        }

        @Test
        @DisplayName("失败：更新资料时Email冲突")
        public void testUpdateUserProfileFailsEmailConflict() throws Exception {
            // 1. 注册两个用户
            registerAndLogin("user_A", "PassA123!", "user_A@example.com", "UserA");
            String tokenB = registerAndLogin("user_B", "PassB123!", "user_B@example.com", "UserB");

            // 2. 用户B 尝试更新自己的 Email 为 用户A 的 Email
            UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
            updateRequest.setNickname("UserB_NewNick");
            updateRequest.setEmail("user_A@example.com"); // 冲突的Email

            mockMvc.perform(put("/api/v1/users/me")
                            .header("Authorization", "Bearer " + tokenB)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("该邮箱已被其他用户注册"));
        }
    }

    // --- 4. 用户偏好测试 ---
    @Nested
    @DisplayName("4. 用户偏好 (/me/preferences)")
    class PreferencesTests {

        @Test
        @DisplayName("成功更新和获取偏好")
        public void testUpdateAndGetPreferencesSuccess() throws Exception {
            String token = registerAndLogin("pref_user", "PrefPass123!", "pref@example.com", "PrefUser");

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

            // 4. 发送 PUT 请求更新偏好
            mockMvc.perform(put("/api/v1/users/me/preferences")
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

        @Test
        @DisplayName("失败：未授权访问偏好接口")
        public void testPreferencesEndpointsFailNoToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/me/preferences"))
                    .andExpect(status().isUnauthorized());

            Map<String, Object> prefs = Map.of("personality", "witty");
            mockMvc.perform(put("/api/v1/users/me/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(prefs)))
                    .andExpect(status().isUnauthorized());
        }
    }
}