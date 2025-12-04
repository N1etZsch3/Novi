package com.n1etzsch3.novi.question.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.aiconfig.factory.DynamicChatModelFactory;
import com.n1etzsch3.novi.aiconfig.service.AiModelConfigService;
import com.n1etzsch3.novi.common.pojo.entity.QuestionGenerationRecord;
import com.n1etzsch3.novi.question.mapper.QuestionExampleMapper;
import com.n1etzsch3.novi.question.mapper.QuestionGenerationRecordMapper;
import com.n1etzsch3.novi.question.pojo.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.question.pojo.dto.QuestionGenerationResponse;
import com.n1etzsch3.novi.question.service.impl.QuestionGenerationServiceImpl;
import com.n1etzsch3.novi.question.utils.QuestionPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuestionGenerationServiceImplTest {

    @Mock
    private QuestionExampleMapper questionExampleMapper;
    @Mock
    private QuestionGenerationRecordMapper questionGenerationRecordMapper;
    @Mock
    private DynamicChatModelFactory dynamicChatModelFactory;
    @Mock
    private AiModelConfigService aiModelConfigService;
    @Mock
    private QuestionPromptBuilder questionPromptBuilder;

    @Mock
    private ChatModel chatModel;

    private QuestionGenerationServiceImpl questionGenerationService;

    @BeforeEach
    void setUp() {
        questionGenerationService = new QuestionGenerationServiceImpl(
                questionExampleMapper,
                questionGenerationRecordMapper,
                dynamicChatModelFactory,
                aiModelConfigService,
                questionPromptBuilder,
                new ObjectMapper() // Use real ObjectMapper
        );
    }

    @Test
    void testGenerateQuestions_Success() {
        // Arrange
        Long userId = 1L;
        QuestionGenerationRequest request = new QuestionGenerationRequest();
        request.setSubject("English");
        request.setQuestionType("Grammar");
        request.setDifficulty("medium");
        request.setQuantity(1);
        request.setEnableThinking(false);

        // Mock Examples (return empty list to trigger fallback or just proceed)
        when(questionExampleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Mock ChatModel creation
        when(dynamicChatModelFactory.createChatModel()).thenReturn(chatModel);

        // Mock AI Response
        String jsonResponse = "[{\"question\":\"test question\",\"options\":[\"A\",\"B\"],\"answer\":\"A\"}]";
        Generation generation = new Generation(new org.springframework.ai.chat.messages.AssistantMessage(jsonResponse));
        ChatResponse chatResponse = new ChatResponse(Collections.singletonList(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // Mock Prompt Builder
        when(questionPromptBuilder.buildPrompt(any(), any())).thenReturn("test prompt");

        // Mock Record Mapper Insert
        when(questionGenerationRecordMapper.insert(any(QuestionGenerationRecord.class))).thenAnswer(invocation -> {
            QuestionGenerationRecord record = invocation.getArgument(0);
            record.setId(100L);
            return 1;
        });

        // Act
        QuestionGenerationResponse response = questionGenerationService.generateQuestions(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getRecordId());
        verify(questionGenerationRecordMapper).insert(any(QuestionGenerationRecord.class));
        verify(chatModel).call(any(Prompt.class));
    }
}
