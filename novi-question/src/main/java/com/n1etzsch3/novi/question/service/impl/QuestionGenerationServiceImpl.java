package com.n1etzsch3.novi.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.aiconfig.factory.DynamicChatModelFactory;
import com.n1etzsch3.novi.aiconfig.service.AiModelConfigService;
import com.n1etzsch3.novi.common.pojo.entity.AiModelConfig;
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
    private final AiModelConfigService aiModelConfigService;
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

        // 检查是否应该启用深度思考
        boolean shouldEnableThinking = shouldEnableThinking(request.getEnableThinking());
        log.info("Deep thinking enabled: {}", shouldEnableThinking);

        // 创建单题请求对象 (用于构建提示词)
        QuestionGenerationRequest singleRequest = new QuestionGenerationRequest(
                request.getSubject(),
                request.getQuestionType(),
                request.getTheme(),
                request.getDifficulty(),
                1, // 强制每次只生成1道
                shouldEnableThinking // 传递深度思考配置
        );

        for (int i = 0; i < quantity; i++) {
            try {
                // 构建提示词
                String promptText = questionPromptBuilder.buildPrompt(singleRequest, examples);
                log.info("Generating question {}/{}. Prompt: {}", i + 1, quantity, promptText);

                // 调用AI (带重试，传递深度思考配置)
                String jsonContent = callAiWithRetry(promptText, shouldEnableThinking);

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
                .enableThinking(shouldEnableThinking) // 保存是否使用了深度思考
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
     * 检查是否应该启用深度思考
     * <p>
     * 只有当用户请求启用深度思考，且当前激活的模型支持深度思考时，才返回true
     * </p>
     *
     * @param requestEnableThinking 用户请求是否启用深度思考
     * @return 是否应该启用深度思考
     */
    private boolean shouldEnableThinking(Boolean requestEnableThinking) {
        // 如果用户没有请求启用深度思考，直接返回false
        if (requestEnableThinking == null || !requestEnableThinking) {
            return false;
        }

        // 检查当前激活的模型是否支持深度思考
        AiModelConfig activeModel = aiModelConfigService.getActiveModel();
        if (activeModel == null || !Boolean.TRUE.equals(activeModel.getEnableThinking())) {
            log.warn("Deep thinking requested but current model '{}' does not support it. Falling back to normal mode.",
                    activeModel != null ? activeModel.getModelName() : "None");
            return false;
        }

        log.info("Deep thinking enabled for model: {}", activeModel.getModelName());
        return true;
    }

    /**
     * 调用AI并带有重试机制
     *
     * @param promptText     提示词文本
     * @param enableThinking 是否启用深度思考
     * @return AI生成的JSON内容
     */
    /**
     * 调用AI并带有重试机制
     *
     * @param promptText     提示词文本
     * @param enableThinking 是否启用深度思考
     * @return AI生成的JSON内容
     */
    private String callAiWithRetry(String promptText, boolean enableThinking) {
        int maxRetries = 1;
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            try {
                attempt++;
                org.springframework.ai.chat.model.ChatModel chatModel = dynamicChatModelFactory.createChatModel();

                // 构建 Prompt 和 Options
                Prompt prompt;
                if (enableThinking && chatModel instanceof com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel) {
                    // DashScope 深度思考模式必须使用流式调用，并且需要特定的 Options
                    com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel dashScopeChatModel = (com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel) chatModel;
                    com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions options = com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions
                            .builder()
                            .withModel(dashScopeChatModel.getDefaultOptions().getModel())
                            .withEnableThinking(true)
                            .withEnableSearch(false) // 思考模式建议关闭搜索
                            .build();
                    prompt = new org.springframework.ai.chat.prompt.Prompt(promptText, options);
                    log.info("Using DashScope Deep Thinking mode (Streaming required)");

                    // 使用流式调用并聚合结果
                    // 注册 ReasoningContentAdvisor 以提取思考过程
                    StringBuilder contentBuilder = new StringBuilder();

                    chatModel.stream(prompt)
                            .doOnNext(chatResponse -> {
                                if (chatResponse.getResults() != null && !chatResponse.getResults().isEmpty()) {
                                    // 尝试提取思考内容并打印日志 (模拟 Advisor 的行为)
                                    org.springframework.ai.chat.metadata.ChatGenerationMetadata metadata = chatResponse
                                            .getResults().get(0)
                                            .getMetadata();
                                    if (metadata != null) {
                                        Object reasoning = metadata.get("reasoning_content");
                                        if (reasoning == null) {
                                            reasoning = metadata.get("reasoningContent");
                                        }
                                        if (reasoning != null
                                                && org.springframework.util.StringUtils.hasText(reasoning.toString())) {
                                            log.debug("Thinking: {}", reasoning);
                                        }
                                    }

                                    // 聚合实际内容
                                    String part = chatResponse.getResults().get(0).getOutput().getText();
                                    if (part != null) {
                                        contentBuilder.append(part);
                                    }
                                }
                            })
                            .blockLast(); // 阻塞直到流结束

                    String rawContent = contentBuilder.toString();
                    log.info("AI Raw Content Length: {}", rawContent.length());

                    String jsonContent = cleanJsonContent(rawContent);
                    validateJson(jsonContent);
                    return jsonContent;

                } else {
                    if (enableThinking) {
                        log.warn(
                                "Deep thinking requested but model is not DashScopeChatModel. Falling back to standard call.");
                    }
                    // 普通模式，可以使用 call 或 stream
                    prompt = new org.springframework.ai.chat.prompt.Prompt(promptText);
                    ChatResponse response = chatModel.call(prompt);
                    String rawContent = response.getResults().get(0).getOutput().getText();
                    log.info("AI Raw Content Length: {}", rawContent != null ? rawContent.length() : 0);

                    String jsonContent = cleanJsonContent(rawContent);
                    validateJson(jsonContent);
                    return jsonContent;
                }

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
                        record.getEnableThinking(), // 包含深度思考标记
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
