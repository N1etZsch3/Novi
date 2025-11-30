package com.n1etzsch3.novi.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.chat.mapper.ChatMemoryMapper;
import com.n1etzsch3.novi.user.pojo.dto.LoginRequest;
import com.n1etzsch3.novi.user.pojo.dto.RegistrationRequest;
import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.common.pojo.entity.ChatMessage;
import com.n1etzsch3.novi.common.pojo.entity.ChatSession;
import com.n1etzsch3.novi.chat.service.ChatSessionService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("聊天会话控制器 (ChatSessionController) 测试")
public class ChatSessionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ChatSessionService chatSessionService;

        @MockBean
        private ChatMemoryMapper chatMemoryMapper;

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
        @DisplayName("成功获取会话列表")
        public void testGetSessionListSuccess() throws Exception {
                String token = registerAndLogin("session_user", "SessionPass123!", "session@example.com",
                                "SessionUser");

                ChatSession session = new ChatSession();
                session.setId("sess_1");
                session.setTitle("Test Session");
                session.setCreatedAt(LocalDateTime.now());

                Mockito.when(chatSessionService.getUserSessions(any(Long.class)))
                                .thenReturn(Collections.singletonList(session));

                mockMvc.perform(get("/api/v1/sessions")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1))
                                .andExpect(jsonPath("$.data[0].id").value("sess_1"))
                                .andExpect(jsonPath("$.data[0].title").value("Test Session"));
        }

        @Test
        @DisplayName("成功获取会话消息")
        public void testGetSessionMessagesSuccess() throws Exception {
                String token = registerAndLogin("msg_user", "MsgPass123!", "msg@example.com", "MsgUser");

                ChatMessage message = new ChatMessage();
                message.setId(1L);
                message.setSessionId("sess_1");
                message.setContent("Hello");
                message.setRole(ChatMessage.MessageRole.USER);

                // Mock validation to pass
                Mockito.doNothing().when(chatSessionService).validateSessionOwner(eq("sess_1"), any(Long.class));

                // Mock mapper
                Mockito.when(chatMemoryMapper.selectList(any(Wrapper.class)))
                                .thenReturn(Collections.singletonList(message));

                mockMvc.perform(get("/api/v1/sessions/sess_1/messages")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1))
                                .andExpect(jsonPath("$.data[0].content").value("Hello"));
        }

        @Test
        @DisplayName("成功删除会话")
        public void testDeleteSessionSuccess() throws Exception {
                String token = registerAndLogin("del_user", "DelPass123!", "del@example.com", "DelUser");

                Mockito.doNothing().when(chatSessionService).deleteSession(eq("sess_del"), any(Long.class));

                mockMvc.perform(delete("/api/v1/sessions/sess_del")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1));
        }
}
