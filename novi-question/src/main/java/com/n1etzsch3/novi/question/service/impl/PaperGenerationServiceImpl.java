package com.n1etzsch3.novi.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.n1etzsch3.novi.common.pojo.entity.PaperGenerationRecord;
import com.n1etzsch3.novi.common.pojo.entity.PaperQuestionDetail;
import com.n1etzsch3.novi.question.mapper.PaperGenerationRecordMapper;
import com.n1etzsch3.novi.question.mapper.PaperQuestionDetailMapper;
import com.n1etzsch3.novi.question.mapper.QuestionCategoryMapper;
import com.n1etzsch3.novi.question.pojo.dto.*;
import com.n1etzsch3.novi.question.pojo.entity.QuestionCategory;
import com.n1etzsch3.novi.question.service.PaperGenerationService;
import com.n1etzsch3.novi.question.service.QuestionGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 套卷生成服务实现类
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaperGenerationServiceImpl implements PaperGenerationService {

    private final PaperGenerationRecordMapper paperGenerationRecordMapper;
    private final PaperQuestionDetailMapper paperQuestionDetailMapper;
    private final QuestionCategoryMapper questionCategoryMapper;
    private final QuestionGenerationService questionGenerationService;
    private final ThreadPoolExecutor paperGenerationExecutor;

    @Override
    public void generatePaperAsync(Long userId, PaperGenerationRequest request, SseEmitter emitter) {
        log.info("Starting async paper generation for user: {}, subjectId: {}", userId, request.getSubjectId());

        // 1. 参数验证
        validateRequest(request);

        // 2. 确定题型配置（自动模式 vs 手动模式）
        List<PaperConfigItem> paperConfig = request.getPaperConfig();

        // 自动模式：如果未指定题型配置，则自动查询科目下所有题型
        if (paperConfig == null || paperConfig.isEmpty()) {
            log.info("Auto mode enabled: querying all question types for subject: {}", request.getSubjectId());
            paperConfig = autoGeneratePaperConfig(request.getSubjectId());
            log.info("Auto mode generated {} question types", paperConfig.size());
        }

        // 3. 创建异步任务列表
        List<CompletableFuture<QuestionTypeResult>> futures = new ArrayList<>();
        Map<Integer, PaperConfigItem> configMap = new HashMap<>();

        for (PaperConfigItem config : paperConfig) {
            configMap.put(config.getOrder(), config);

            CompletableFuture<QuestionTypeResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return generateQuestionType(userId, config, request.getEnableThinking());
                } catch (Exception e) {
                    log.error("Failed to generate question type: {}", config.getQuestionTypeCode(), e);
                    return QuestionTypeResult.builder()
                            .questionTypeCode(config.getQuestionTypeCode())
                            .order(config.getOrder())
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build();
                }
            }, paperGenerationExecutor);

            futures.add(future);
        }

        // 3. 等待所有任务完成并收集结果
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    // 从 futures 中获取所有结果（确保顺序和完整性）
                    List<QuestionTypeResult> results = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
                    return results;
                })
                .whenComplete((results, ex) -> {
                    if (ex != null) {
                        log.error("Failed to complete paper generation", ex);
                        emitter.completeWithError(ex);
                        return;
                    }

                    try {
                        // 4. 为每个结果发送 SSE 事件（按顺序）
                        for (QuestionTypeResult result : results) {
                            if (!result.getSuccess()) {
                                emitter.send(createErrorEvent(result));
                            } else {
                                emitter.send(createQuestionEvent(result));
                            }
                        }

                        // 5. 保存套卷记录
                        Long paperId = savePaperRecord(userId, request, results);

                        // 6. 发送完成事件
                        emitter.send(createCompleteEvent(paperId, results));
                        emitter.complete();

                        log.info("Paper generation completed for user: {}, paperId: {}", userId, paperId);
                    } catch (Exception e) {
                        log.error("Failed to send SSE events or save paper", e);
                        emitter.completeWithError(e);
                    }
                });
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(PaperGenerationRequest request) {
        // 验证科目是否存在
        QuestionCategory subject = questionCategoryMapper.selectOne(
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getId, request.getSubjectId())
                        .eq(QuestionCategory::getCategoryType, 1));
        if (subject == null) {
            throw new IllegalArgumentException("科目不存在");
        }

        // 验证题型配置（仅在手动模式下验证）
        if (request.getPaperConfig() != null && !request.getPaperConfig().isEmpty()) {
            // 验证 order 是否唯一
            Set<Integer> orders = request.getPaperConfig().stream()
                    .map(PaperConfigItem::getOrder)
                    .collect(Collectors.toSet());
            if (orders.size() != request.getPaperConfig().size()) {
                throw new IllegalArgumentException("显示顺序(order)不能重复");
            }

            // 验证题型编码是否存在
            for (PaperConfigItem config : request.getPaperConfig()) {
                QuestionCategory questionType = questionCategoryMapper.selectOne(
                        new LambdaQueryWrapper<QuestionCategory>()
                                .eq(QuestionCategory::getCode, config.getQuestionTypeCode())
                                .eq(QuestionCategory::getParentId, request.getSubjectId())
                                .eq(QuestionCategory::getCategoryType, 2));
                if (questionType == null) {
                    throw new IllegalArgumentException("题型不存在: " + config.getQuestionTypeCode());
                }
            }
        }
        // 自动模式下，paperConfig 可以为空，会自动查询科目下所有题型
    }

    /**
     * 自动生成题型配置
     * 查询科目下的所有题型，为每个题型生成默认配置
     */
    private List<PaperConfigItem> autoGeneratePaperConfig(Long subjectId) {
        // 查询科目下的所有题型
        List<QuestionCategory> questionTypes = questionCategoryMapper.selectList(
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getParentId, subjectId)
                        .eq(QuestionCategory::getCategoryType, 2)
                        .orderByAsc(QuestionCategory::getSortOrder));

        if (questionTypes.isEmpty()) {
            throw new IllegalArgumentException("该科目下没有题型，无法自动生成套卷");
        }

        // 为每个题型创建默认配置
        List<PaperConfigItem> configList = new ArrayList<>();
        for (int i = 0; i < questionTypes.size(); i++) {
            QuestionCategory type = questionTypes.get(i);

            // 🔥 从数据库读取生成次数配置
            // 如果未配置或为null，默认为1
            Integer quantity = type.getGenerationCount() != null
                    ? type.getGenerationCount()
                    : 1;

            log.info("题型 {} 配置生成次数: {}", type.getName(), quantity);

            PaperConfigItem config = PaperConfigItem.builder()
                    .questionTypeCode(type.getCode())
                    .quantity(quantity) // 从数据库读取，支持配置化
                    .difficulty("medium") // 默认中等难度
                    .theme(null) // 无特定主题，综合考点
                    .order(i + 1) // 按查询顺序编号
                    .build();
            configList.add(config);
        }

        return configList;
    }

    /**
     * 生成单个题型
     */
    private QuestionTypeResult generateQuestionType(Long userId, PaperConfigItem config, Boolean enableThinking) {
        log.info("Generating question type: {} for user: {}", config.getQuestionTypeCode(), userId);

        try {
            // 1. 查询题型信息
            QuestionCategory questionType = questionCategoryMapper.selectOne(
                    new LambdaQueryWrapper<QuestionCategory>()
                            .eq(QuestionCategory::getCode, config.getQuestionTypeCode())
                            .eq(QuestionCategory::getCategoryType, 2));

            if (questionType == null) {
                throw new IllegalArgumentException("题型不存在: " + config.getQuestionTypeCode());
            }

            // 2. 查询科目信息
            QuestionCategory subject = questionCategoryMapper.selectById(questionType.getParentId());

            // 3. 构建出题请求
            QuestionGenerationRequest questionRequest = new QuestionGenerationRequest(
                    subject.getName(), // 科目名称
                    questionType.getName(), // 题型名称 (fix: 之前错误地传入了code)
                    config.getTheme(),
                    config.getDifficulty(),
                    config.getQuantity(),
                    enableThinking);

            // 4. 调用出题服务
            QuestionGenerationResponse response = questionGenerationService.generateQuestions(userId, questionRequest);

            // 5. 封装结果
            return QuestionTypeResult.builder()
                    .questionTypeCode(config.getQuestionTypeCode())
                    .questionTypeName(questionType.getName())
                    .order(config.getOrder())
                    .difficulty(config.getDifficulty())
                    .quantity(config.getQuantity())
                    .theme(config.getTheme())
                    .questionsJson(response.getQuestions())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate question type: {}", config.getQuestionTypeCode(), e);

            // 查询题型名称以便错误提示
            QuestionCategory questionType = questionCategoryMapper.selectOne(
                    new LambdaQueryWrapper<QuestionCategory>()
                            .eq(QuestionCategory::getCode, config.getQuestionTypeCode()));
            String typeName = questionType != null ? questionType.getName() : config.getQuestionTypeCode();

            return QuestionTypeResult.builder()
                    .questionTypeCode(config.getQuestionTypeCode())
                    .questionTypeName(typeName)
                    .order(config.getOrder())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 保存套卷记录
     */
    @Transactional(rollbackFor = Exception.class)
    protected Long savePaperRecord(Long userId, PaperGenerationRequest request, List<QuestionTypeResult> results) {
        // 计算总题目数
        int totalQuestions = results.stream()
                .filter(QuestionTypeResult::getSuccess)
                .mapToInt(QuestionTypeResult::getQuantity)
                .sum();

        // 查询科目名称
        QuestionCategory subject = questionCategoryMapper.selectById(request.getSubjectId());

        // 生成套卷名称
        String paperName = String.format("%s套卷-%s",
                subject.getName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));

        // 1. 保存套卷记录
        PaperGenerationRecord paperRecord = PaperGenerationRecord.builder()
                .userId(userId)
                .subjectId(request.getSubjectId())
                .paperName(paperName)
                .totalQuestions(totalQuestions)
                .enableThinking(request.getEnableThinking())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        paperGenerationRecordMapper.insert(paperRecord);
        log.info("Saved paper record with ID: {}", paperRecord.getId());

        // 2. 保存题目明细
        for (QuestionTypeResult result : results) {
            if (result.getSuccess()) {
                PaperQuestionDetail detail = PaperQuestionDetail.builder()
                        .paperId(paperRecord.getId())
                        .questionType(result.getQuestionTypeCode())
                        .questionTypeName(result.getQuestionTypeName())
                        .difficulty(result.getDifficulty())
                        .quantity(result.getQuantity())
                        .theme(result.getTheme())
                        .generatedQuestions(result.getQuestionsJson())
                        .displayOrder(result.getOrder())
                        .createdAt(LocalDateTime.now())
                        .build();

                paperQuestionDetailMapper.insert(detail);
            }
        }

        return paperRecord.getId();
    }

    /**
     * 创建题目事件
     */
    private SseEmitter.SseEventBuilder createQuestionEvent(QuestionTypeResult result) throws IOException {
        PaperGenerationResponse response = PaperGenerationResponse.builder()
                .eventType("question")
                .questionType(result.getQuestionTypeCode())
                .questionTypeName(result.getQuestionTypeName())
                .order(result.getOrder())
                .questions(result.getQuestionsJson())
                .timestamp(LocalDateTime.now())
                .build();

        return SseEmitter.event()
                .name("question")
                .data(response);
    }

    /**
     * 创建错误事件
     */
    private SseEmitter.SseEventBuilder createErrorEvent(QuestionTypeResult result) throws IOException {
        PaperGenerationResponse response = PaperGenerationResponse.builder()
                .eventType("error")
                .questionType(result.getQuestionTypeCode())
                .questionTypeName(result.getQuestionTypeName())
                .order(result.getOrder())
                .error(result.getErrorMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return SseEmitter.event()
                .name("error")
                .data(response);
    }

    /**
     * 创建完成事件
     */
    private SseEmitter.SseEventBuilder createCompleteEvent(Long paperId, List<QuestionTypeResult> results)
            throws IOException {
        long successCount = results.stream().filter(QuestionTypeResult::getSuccess).count();
        long failedCount = results.size() - successCount;
        int totalQuestions = results.stream()
                .filter(QuestionTypeResult::getSuccess)
                .mapToInt(QuestionTypeResult::getQuantity)
                .sum();

        PaperGenerationResponse response = PaperGenerationResponse.builder()
                .eventType("complete")
                .paperId(paperId)
                .totalQuestions(totalQuestions)
                .successCount((int) successCount)
                .failedCount((int) failedCount)
                .timestamp(LocalDateTime.now())
                .build();

        return SseEmitter.event()
                .name("complete")
                .data(response);
    }

    @Override
    public List<PaperHistoryItem> getPaperHistory(Long userId) {
        List<PaperGenerationRecord> records = paperGenerationRecordMapper.selectList(
                new LambdaQueryWrapper<PaperGenerationRecord>()
                        .eq(PaperGenerationRecord::getUserId, userId)
                        .orderByDesc(PaperGenerationRecord::getCreatedAt));

        return records.stream()
                .map(record -> {
                    QuestionCategory subject = questionCategoryMapper.selectById(record.getSubjectId());
                    return PaperHistoryItem.builder()
                            .id(record.getId())
                            .paperName(record.getPaperName())
                            .subjectName(subject != null ? subject.getName() : "未知科目")
                            .totalQuestions(record.getTotalQuestions())
                            .enableThinking(record.getEnableThinking())
                            .createdAt(record.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public PaperDetailResponse getPaperDetail(Long paperId, Long userId) {
        // 1. 查询套卷记录
        PaperGenerationRecord record = paperGenerationRecordMapper.selectOne(
                new LambdaQueryWrapper<PaperGenerationRecord>()
                        .eq(PaperGenerationRecord::getId, paperId)
                        .eq(PaperGenerationRecord::getUserId, userId));

        if (record == null) {
            throw new IllegalArgumentException("套卷不存在或无权访问");
        }

        // 2. 查询题目明细
        List<PaperQuestionDetail> details = paperQuestionDetailMapper.selectList(
                new LambdaQueryWrapper<PaperQuestionDetail>()
                        .eq(PaperQuestionDetail::getPaperId, paperId)
                        .orderByAsc(PaperQuestionDetail::getDisplayOrder));

        // 3. 查询科目名称
        QuestionCategory subject = questionCategoryMapper.selectById(record.getSubjectId());

        // 4. 封装响应
        List<PaperDetailResponse.QuestionTypeDetail> detailList = details.stream()
                .map(detail -> PaperDetailResponse.QuestionTypeDetail.builder()
                        .questionType(detail.getQuestionType())
                        .questionTypeName(detail.getQuestionTypeName())
                        .difficulty(detail.getDifficulty())
                        .quantity(detail.getQuantity())
                        .theme(detail.getTheme())
                        .order(detail.getDisplayOrder())
                        .questions(detail.getGeneratedQuestions())
                        .build())
                .collect(Collectors.toList());

        return PaperDetailResponse.builder()
                .id(record.getId())
                .paperName(record.getPaperName())
                .subjectName(subject != null ? subject.getName() : "未知科目")
                .totalQuestions(record.getTotalQuestions())
                .enableThinking(record.getEnableThinking())
                .details(detailList)
                .createdAt(record.getCreatedAt())
                .build();
    }

    @Override
    public void deletePaper(Long paperId, Long userId) {
        // 1. 验证权限
        PaperGenerationRecord record = paperGenerationRecordMapper.selectOne(
                new LambdaQueryWrapper<PaperGenerationRecord>()
                        .eq(PaperGenerationRecord::getId, paperId)
                        .eq(PaperGenerationRecord::getUserId, userId));

        if (record == null) {
            throw new IllegalArgumentException("套卷不存在或无权访问");
        }

        // 2. 删除记录（级联删除明细）
        paperGenerationRecordMapper.deleteById(paperId);
        log.info("Deleted paper with ID: {}", paperId);
    }
}
