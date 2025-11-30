package com.n1etzsch3.novi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.user.pojo.dto.LoginRequest;
import com.n1etzsch3.novi.user.pojo.dto.NoviPersonaSettings;
import com.n1etzsch3.novi.user.pojo.dto.RegistrationRequest;
import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.user.service.UserPreferenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("用户偏好控制器 (UserPreferenceController) 测试")
public class UserPreferenceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserPreferenceService userPreferenceService;

        // --- 辅助方法：用于注册和登录 ---
        private String registerAndLogin(String user, String pass, String email, String nickname) throws Exception {
                // 1. 注册
                RegistrationRequest regRequest = new RegistrationRequest();
                regRequest.setUsername(user);
                regRequest.setPassword(pass);
                regRequest.setEmail(email);
                regRequest.setNickname(nickname);

                mockMvc.perform(post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regRequest)))
                                .andExpect(status().isOk());

                // 2. 登录
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(user);
                loginRequest.setPassword(pass);

                MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                // 3. 解析 Token
                String responseString = loginResult.getResponse().getContentAsString();
                Map<String, Object> data = (Map<String, Object>) objectMapper.readValue(responseString, Result.class)
                                .getData();
                return (String) data.get("token");
        }

        @Test
        @DisplayName("成功获取偏好设置")
        public void testGetSettingsSuccess() throws Exception {
                String token = registerAndLogin("pref_user_2", "PrefPass123!", "pref2@example.com", "PrefUser2");

                NoviPersonaSettings settings = new NoviPersonaSettings();
                settings.setPersonalityMode("witty");
                settings.setUserAddressName("Master");

                Mockito.when(userPreferenceService.getPersonaSettings(any(Long.class)))
                                .thenReturn(settings);

                mockMvc.perform(get("/api/v1/preferences")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1))
                                .andExpect(jsonPath("$.data.personalityMode").value("witty"))
                                .andExpect(jsonPath("$.data.userAddressName").value("Master"));
        }

        @Test
        @DisplayName("成功更新偏好设置")
        public void testUpdateSettingsSuccess() throws Exception {
                String token = registerAndLogin("pref_user_3", "PrefPass123!", "pref3@example.com", "PrefUser3");

                NoviPersonaSettings settings = new NoviPersonaSettings();
                settings.setPersonalityMode("gentle");

                Mockito.when(userPreferenceService.updatePersonaSettings(any(Long.class),
                                any(NoviPersonaSettings.class)))
                                .thenReturn(settings);

                mockMvc.perform(put("/api/v1/preferences")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(settings)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1))
                                .andExpect(jsonPath("$.data.personalityMode").value("gentle"));
        }
}
