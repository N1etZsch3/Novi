package com.n1etzsch3.novi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationResponse;
import com.n1etzsch3.novi.domain.dto.QuestionHistoryItem;
import com.n1etzsch3.novi.service.QuestionGenerationService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = QuestionGenerationController.class, properties = "novi.jwt.header-name=Authorization")
class QuestionGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionGenerationService questionGenerationService;

    @MockBean
    private com.n1etzsch3.novi.interceptor.JwtAuthInterceptor jwtAuthInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<LoginUserContext> loginUserContextMock;

    @BeforeEach
    void setUp() throws Exception {
        loginUserContextMock = mockStatic(LoginUserContext.class);
        loginUserContextMock.when(LoginUserContext::getUserId).thenReturn(1L);

        // Allow all requests through the interceptor
        when(jwtAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        loginUserContextMock.close();
    }

    @Test
    void generateQuestions_Success() throws Exception {
        QuestionGenerationRequest request = new QuestionGenerationRequest();
        request.setSubject("Test Subject");
        request.setQuestionType("Test Type");
        request.setDifficulty("medium");
        request.setQuantity(5);

        QuestionGenerationResponse response = new QuestionGenerationResponse(1L, "[]");
        when(questionGenerationService.generateQuestions(eq(1L), any(QuestionGenerationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/questions/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.recordId").value(1));
    }

    @Test
    void generateQuestions_ValidationError() throws Exception {
        QuestionGenerationRequest request = new QuestionGenerationRequest();
        // Missing required fields

        mockMvc.perform(post("/api/v1/questions/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // GlobalExceptionHandler returns 200 with error code
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getGenerationHistory_Success() throws Exception {
        QuestionHistoryItem item = new QuestionHistoryItem();
        item.setId(1L);
        item.setSubject("Test Subject");
        List<QuestionHistoryItem> history = Collections.singletonList(item);

        when(questionGenerationService.getGenerationHistory(1L)).thenReturn(history);

        mockMvc.perform(get("/api/v1/questions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getRecordDetail_Success() throws Exception {
        QuestionGenerationResponse response = new QuestionGenerationResponse(1L, "[]");
        when(questionGenerationService.getRecordDetail(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/questions/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.recordId").value(1));
    }
}
