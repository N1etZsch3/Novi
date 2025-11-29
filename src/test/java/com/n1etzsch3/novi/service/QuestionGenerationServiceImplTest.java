package com.n1etzsch3.novi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.config.DynamicChatModelFactory;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationResponse;
import com.n1etzsch3.novi.domain.dto.QuestionHistoryItem;
import com.n1etzsch3.novi.domain.po.QuestionGenerationRecord;
import com.n1etzsch3.novi.mapper.QuestionExampleMapper;
import com.n1etzsch3.novi.mapper.QuestionGenerationRecordMapper;
import com.n1etzsch3.novi.service.impl.QuestionGenerationServiceImpl;
import com.n1etzsch3.novi.utils.QuestionPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionGenerationServiceImplTest {

    @Mock
    private QuestionExampleMapper questionExampleMapper;

    @Mock
    private QuestionGenerationRecordMapper questionGenerationRecordMapper;

    @Mock
    private DynamicChatModelFactory dynamicChatModelFactory;

    @Mock
    private QuestionPromptBuilder questionPromptBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OpenAiChatModel chatModel;

    @InjectMocks
    private QuestionGenerationServiceImpl questionGenerationService;

    private QuestionGenerationRequest request;

    @BeforeEach
    void setUp() {
        request = new QuestionGenerationRequest();
        request.setSubject("Test Subject");
        request.setQuestionType("Test Type");
        request.setDifficulty("medium");
        request.setQuantity(5);
    }

    @Test
    void generateQuestions_Success() throws Exception {
        // Mock dependencies
        when(questionExampleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(questionPromptBuilder.buildPrompt(any(), any())).thenReturn("Test Prompt");
        when(dynamicChatModelFactory.createChatModel()).thenReturn(chatModel);

        // Mock AI response
        String jsonResponse = "[{\"content\":\"Question 1\"}]";
        AssistantMessage message = new AssistantMessage(jsonResponse);
        Generation generation = new Generation(message);
        ChatResponse chatResponse = new ChatResponse(Collections.singletonList(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // Mock ObjectMapper
        when(objectMapper.readTree(anyString())).thenReturn(null); // Just verify it doesn't throw exception

        // Mock DB save
        when(questionGenerationRecordMapper.insert(any(QuestionGenerationRecord.class))).thenAnswer(invocation -> {
            QuestionGenerationRecord record = invocation.getArgument(0);
            record.setId(1L);
            return 1;
        });

        // Execute
        QuestionGenerationResponse response = questionGenerationService.generateQuestions(1L, request);

        // Verify
        assertNotNull(response);
        assertEquals(1L, response.getRecordId());
        assertEquals(jsonResponse, response.getQuestions());

        verify(questionGenerationRecordMapper).insert(any(QuestionGenerationRecord.class));
    }

    @Test
    void generateQuestions_DirtyJson() {
        // Mock dependencies
        when(questionExampleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(questionPromptBuilder.buildPrompt(any(), any())).thenReturn("Test Prompt");
        when(dynamicChatModelFactory.createChatModel()).thenReturn(chatModel);

        // Mock AI response with dirty JSON (markdown and extra text)
        String dirtyJson = "Here is the result:\n```json\n[{\"content\":\"Question 1\"}]\n```\nHope it helps!";
        String expectedJson = "[{\"content\":\"Question 1\"}]";

        AssistantMessage message = new AssistantMessage(dirtyJson);
        Generation generation = new Generation(message);
        ChatResponse chatResponse = new ChatResponse(Collections.singletonList(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // Mock DB save
        when(questionGenerationRecordMapper.insert(any(QuestionGenerationRecord.class))).thenAnswer(invocation -> {
            QuestionGenerationRecord record = invocation.getArgument(0);
            record.setId(1L);
            return 1;
        });

        // Execute
        QuestionGenerationResponse response = questionGenerationService.generateQuestions(1L, request);

        // Verify
        assertNotNull(response);
        assertEquals(expectedJson, response.getQuestions());
    }

    @Test
    void getGenerationHistory_Success() {
        // Mock DB
        QuestionGenerationRecord record = QuestionGenerationRecord.builder()
                .id(1L)
                .subject("Test Subject")
                .questionType("Test Type")
                .createdAt(LocalDateTime.now())
                .build();
        when(questionGenerationRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(record));

        // Execute
        List<QuestionHistoryItem> history = questionGenerationService.getGenerationHistory(1L);

        // Verify
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(1L, history.get(0).getId());
        assertEquals("Test Subject", history.get(0).getSubject());
    }
}
