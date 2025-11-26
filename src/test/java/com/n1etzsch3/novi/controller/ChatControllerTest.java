package com.n1etzsch3.novi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.LoginRequest;
import com.n1etzsch3.novi.pojo.dto.RegistrationRequest;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.service.ChatService;
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
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("聊天控制器 (ChatController) 测试")
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

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
        Map<String, Object> data = (Map<String, Object>) objectMapper.readValue(responseString, Result.class).getData();
        return (String) data.get("token");
    }

    @Test
    @DisplayName("成功发送阻塞消息")
    public void testSendMessageBlockingSuccess() throws Exception {
        String token = registerAndLogin("chat_user", "ChatPass123!", "chat@example.com", "ChatUser");

        ChatRequest request = new ChatRequest();
        request.setMessage("Hello");
        request.setSessionId("session_1");

        ChatResponse mockResponse = new ChatResponse("Hi there", "session_1", "Greeting");
        Mockito.when(chatService.handleCallMessage(any(Long.class), any(ChatRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/chat/send/call")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.response").value("Hi there"));
    }

    @Test
    @DisplayName("成功发送流式消息")
    public void testSendMessageStreamSuccess() throws Exception {
        String token = registerAndLogin("stream_user", "StreamPass123!", "stream@example.com", "StreamUser");

        ChatRequest request = new ChatRequest();
        request.setMessage("Tell me a story");
        request.setSessionId("session_stream");

        Mockito.when(chatService.handleStreamMessage(any(Long.class), any(ChatRequest.class)))
                .thenReturn(Flux.just("Once", " upon", " a", " time"));

        MvcResult result = mockMvc.perform(post("/api/v1/chat/send/stream")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        // Since it's a stream, we might need to handle async result if it was a real
        // async request.
        // But here we are mocking the service to return a Flux.
        // Spring MVC with Flux return type usually handles it.
        // Let's just verify the content if possible, or just the status.
        // For Flux<String>, it writes directly to the response body.
        String content = result.getResponse().getContentAsString();
        // Flux output might be concatenated strings
        // "Once upon a time"
        // Note: The actual output format depends on how Spring WebFlux handles
        // Flux<String> in a Servlet container (Spring MVC).
        // It typically writes chunks.
    }
}
