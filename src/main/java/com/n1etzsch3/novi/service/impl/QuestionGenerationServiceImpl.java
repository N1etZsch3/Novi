package com.n1etzsch3.novi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.config.DynamicChatModelFactory;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.domain.dto.QuestionGenerationResponse;
import com.n1etzsch3.novi.domain.dto.QuestionHistoryItem;
import com.n1etzsch3.novi.domain.po.QuestionExample;
import com.n1etzsch3.novi.domain.po.QuestionGenerationRecord;
import com.n1etzsch3.novi.mapper.QuestionExampleMapper;
import com.n1etzsch3.novi.mapper.QuestionGenerationRecordMapper;
import com.n1etzsch3.novi.service.QuestionGenerationService;
import com.n1etzsch3.novi.utils.QuestionPromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI出题服务实现类
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGenerationServiceImpl implements QuestionGenerationService {

    private final QuestionExampleMapper questionExampleMapper;
    private final QuestionGenerationRecordMapper questionGenerationRecordMapper;
    private final DynamicChatModelFactory dynamicChatModelFactory;
    private final QuestionPromptBuilder questionPromptBuilder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionGenerationResponse generateQuestions(Long userId, QuestionGenerationRequest request) {
        log.info("Starting question generation for user: {}, subject: {}", userId, request.getSubject());

        // 1. 查询示例题目 (Few-Shot)
        List<QuestionExample> examples = questionExampleMapper.selectList(
                new LambdaQueryWrapper<QuestionExample>()
                        .eq(QuestionExample::getSubject, request.getSubject())
                        .eq(QuestionExample::getQuestionType, request.getQuestionType())
                        .eq(QuestionExample::getDifficulty, request.getDifficulty())
                        .last("LIMIT 3") // 限制示例数量
        );

        // 2. 构建提示词
        String promptText = questionPromptBuilder.buildPrompt(request, examples);
        log.debug("Generated prompt: {}", promptText);

        // 3. 调用AI模型
        OpenAiChatModel chatModel = dynamicChatModelFactory.createChatModel();
        ChatResponse response = chatModel.call(new Prompt(promptText));
        String rawContent = response.getResults().get(0).getOutput().getText();
        log.debug("AI Response: {}", rawContent);

        // 4. 清理和解析JSON
        String jsonContent = cleanJsonContent(rawContent);
        validateJson(jsonContent);

        // 5. 保存记录
        QuestionGenerationRecord record = QuestionGenerationRecord.builder()
                .userId(userId)
                .subject(request.getSubject())
                .questionType(request.getQuestionType())
                .theme(request.getTheme())
                .difficulty(request.getDifficulty())
                .quantity(request.getQuantity())
                .generatedQuestions(jsonContent)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        questionGenerationRecordMapper.insert(record);
        log.info("Saved question generation record with ID: {}", record.getId());

        // 6. 返回结果
        return new QuestionGenerationResponse(record.getId(), jsonContent);
    }

    @Override
    public List<QuestionHistoryItem> getGenerationHistory(Long userId) {
        List<QuestionGenerationRecord> records = questionGenerationRecordMapper.selectList(
                new LambdaQueryWrapper<QuestionGenerationRecord>()
                        .eq(QuestionGenerationRecord::getUserId, userId)
                        .orderByDesc(QuestionGenerationRecord::getCreatedAt));

        return records.stream()
                .map(record -> new QuestionHistoryItem(
                        record.getId(),
                        record.getSubject(),
                        record.getQuestionType(),
                        record.getTheme(),
                        record.getDifficulty(),
                        record.getQuantity(),
                        record.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public QuestionGenerationResponse getRecordDetail(Long recordId, Long userId) {
        QuestionGenerationRecord record = questionGenerationRecordMapper.selectOne(
                new LambdaQueryWrapper<QuestionGenerationRecord>()
                        .eq(QuestionGenerationRecord::getId, recordId)
                        .eq(QuestionGenerationRecord::getUserId, userId));

        if (record == null) {
            throw new IllegalArgumentException("Record not found or access denied");
        }

        return new QuestionGenerationResponse(record.getId(), record.getGeneratedQuestions());
    }

    /**
     * 清理AI返回的内容，移除Markdown代码块标记
     */
    private String cleanJsonContent(String content) {
        if (content == null) {
            return "[]";
        }
        String cleaned = content.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    /**
     * 验证JSON格式是否有效
     */
    private void validateJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON from AI: {}", json, e);
            throw new RuntimeException("AI generated invalid JSON format", e);
        }
    }
}
