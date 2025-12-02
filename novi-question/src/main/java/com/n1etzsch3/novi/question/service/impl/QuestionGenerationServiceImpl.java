package com.n1etzsch3.novi.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.aiconfig.factory.DynamicChatModelFactory;
import com.n1etzsch3.novi.question.pojo.dto.QuestionGenerationRequest;
import com.n1etzsch3.novi.question.pojo.dto.QuestionGenerationResponse;
import com.n1etzsch3.novi.question.pojo.dto.QuestionHistoryItem;
import com.n1etzsch3.novi.common.pojo.entity.QuestionExample;
import com.n1etzsch3.novi.common.pojo.entity.QuestionGenerationRecord;
import com.n1etzsch3.novi.question.mapper.QuestionExampleMapper;
import com.n1etzsch3.novi.question.mapper.QuestionGenerationRecordMapper;
import com.n1etzsch3.novi.question.service.QuestionGenerationService;
import com.n1etzsch3.novi.question.utils.QuestionPromptBuilder;
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

        // 0. 校验数量 (防止滥用，最多3道)
        if (request.getQuantity() > 3) {
            throw new IllegalArgumentException("为了保证质量，每次最多生成3道题目。");
        }

        // 1. 查询示例题目 (Few-Shot)
        // 优化：优先查询指定难度的示例，如果没有，则降级使用 medium 难度
        List<QuestionExample> examples = questionExampleMapper.selectList(
                new LambdaQueryWrapper<QuestionExample>()
                        .eq(QuestionExample::getSubject, request.getSubject())
                        .eq(QuestionExample::getQuestionType, request.getQuestionType())
                        .eq(QuestionExample::getDifficulty, request.getDifficulty())
                        .last("LIMIT 3"));

        if (examples.isEmpty()) {
            log.info("No examples found for difficulty: {}, falling back to 'medium'", request.getDifficulty());
            examples = questionExampleMapper.selectList(
                    new LambdaQueryWrapper<QuestionExample>()
                            .eq(QuestionExample::getSubject, request.getSubject())
                            .eq(QuestionExample::getQuestionType, request.getQuestionType())
                            .eq(QuestionExample::getDifficulty, "medium")
                            .last("LIMIT 3"));
        }

        // 2. 循环调用AI生成题目
        List<Object> allQuestions = new java.util.ArrayList<>();
        int quantity = request.getQuantity();

        // 创建单题请求对象 (用于构建提示词)
        QuestionGenerationRequest singleRequest = new QuestionGenerationRequest(
                request.getSubject(),
                request.getQuestionType(),
                request.getTheme(),
                request.getDifficulty(),
                1 // 强制每次只生成1道
        );

        for (int i = 0; i < quantity; i++) {
            try {
                // 构建提示词
                String promptText = questionPromptBuilder.buildPrompt(singleRequest, examples);
                log.info("Generating question {}/{}. Prompt: {}", i + 1, quantity, promptText);

                // 调用AI (带重试)
                String jsonContent = callAiWithRetry(promptText);

                // 解析并添加到总列表
                // AI返回的是一个数组，我们需要把里面的元素取出来放进总列表
                com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonContent);
                if (rootNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                        allQuestions.add(node);
                    }
                } else {
                    allQuestions.add(rootNode);
                }

            } catch (Exception e) {
                log.error("Failed to generate question {}/{}", i + 1, quantity, e);
                // 可以选择继续生成剩下的，或者直接报错。这里选择继续，尽可能返回部分结果
                // 但如果一个都生成不出来，最后会是空的
            }
        }

        if (allQuestions.isEmpty()) {
            throw new RuntimeException("多次尝试后未能生成任何题目。");
        }

        // 3. 序列化结果
        String finalJson;
        try {
            finalJson = objectMapper.writeValueAsString(allQuestions);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化生成的题目失败", e);
        }

        // 4. 保存记录
        QuestionGenerationRecord record = QuestionGenerationRecord.builder()
                .userId(userId)
                .subject(request.getSubject())
                .questionType(request.getQuestionType())
                .theme(request.getTheme())
                .difficulty(request.getDifficulty())
                .quantity(request.getQuantity())
                .generatedQuestions(finalJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        questionGenerationRecordMapper.insert(record);
        log.info("Saved question generation record with ID: {}", record.getId());

        // 5. 返回结果
        return new QuestionGenerationResponse(record.getId(), finalJson);
    }

    /**
     * 调用AI并带有重试机制
     */
    private String callAiWithRetry(String promptText) {
        int maxRetries = 1;
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            try {
                attempt++;
                OpenAiChatModel chatModel = dynamicChatModelFactory.createChatModel();
                ChatResponse response = chatModel.call(new Prompt(promptText));
                String rawContent = response.getResults().get(0).getOutput().getText();
                // log.debug("AI Response (Attempt {}): {}", attempt, rawContent);

                // 清理和解析JSON
                String jsonContent = cleanJsonContent(rawContent);
                validateJson(jsonContent);

                return jsonContent;
            } catch (Exception e) {
                log.warn("AI generation failed on attempt {}/{}: {}", attempt, maxRetries + 1, e.getMessage());
                lastException = e;
            }
        }
        throw new RuntimeException("重试后未能生成有效的题目", lastException);
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
            throw new IllegalArgumentException("记录不存在或无权访问");
        }

        return new QuestionGenerationResponse(record.getId(), record.getGeneratedQuestions());
    }

    @Override
    public void deleteGenerationRecord(Long recordId, Long userId) {
        // 1. Check if the record exists and belongs to the user
        QuestionGenerationRecord record = questionGenerationRecordMapper.selectOne(
                new LambdaQueryWrapper<QuestionGenerationRecord>()
                        .eq(QuestionGenerationRecord::getId, recordId)
                        .eq(QuestionGenerationRecord::getUserId, userId));

        if (record == null) {
            throw new IllegalArgumentException("记录不存在或无权访问");
        }

        // 2. Delete the record
        questionGenerationRecordMapper.deleteById(recordId);
        log.info("Deleted question generation record with ID: {}", recordId);
    }

    @Override
    public void deleteGenerationRecords(List<Long> recordIds, Long userId) {
        if (recordIds == null || recordIds.isEmpty()) {
            return;
        }

        // 批量删除属于该用户的记录
        // 使用 delete(wrapper) 确保只能删除自己的记录
        int deletedCount = questionGenerationRecordMapper.delete(
                new LambdaQueryWrapper<QuestionGenerationRecord>()
                        .in(QuestionGenerationRecord::getId, recordIds)
                        .eq(QuestionGenerationRecord::getUserId, userId));

        log.info("Batch deleted {} question generation records for user {}", deletedCount, userId);
    }

    /**
     * 清理AI返回的内容，提取有效的JSON部分
     */
    private String cleanJsonContent(String content) {
        if (content == null) {
            return "[]";
        }
        String cleaned = content.trim();

        // 寻找第一个 '{' 或 '['
        int firstBrace = cleaned.indexOf('{');
        int firstBracket = cleaned.indexOf('[');

        int start = -1;
        if (firstBrace != -1 && firstBracket != -1) {
            start = Math.min(firstBrace, firstBracket);
        } else if (firstBrace != -1) {
            start = firstBrace;
        } else if (firstBracket != -1) {
            start = firstBracket;
        }

        if (start == -1) {
            return "[]"; // 没有找到JSON起始符号
        }

        // 寻找最后一个 '}' 或 ']'
        int lastBrace = cleaned.lastIndexOf('}');
        int lastBracket = cleaned.lastIndexOf(']');

        int end = -1;
        if (lastBrace != -1 && lastBracket != -1) {
            end = Math.max(lastBrace, lastBracket);
        } else if (lastBrace != -1) {
            end = lastBrace;
        } else if (lastBracket != -1) {
            end = lastBracket;
        }

        if (end == -1 || end < start) {
            return "[]"; // 没有找到JSON结束符号或顺序错误
        }

        return cleaned.substring(start, end + 1);
    }

    /**
     * 验证JSON格式是否有效
     */
    private void validateJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON from AI: {}", json, e);
            throw new RuntimeException("AI 生成了无效的数据", e);
        }
    }
}
