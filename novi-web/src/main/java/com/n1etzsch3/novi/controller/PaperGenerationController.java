package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.common.utils.LoginUserContext;
import com.n1etzsch3.novi.question.pojo.dto.PaperDetailResponse;
import com.n1etzsch3.novi.question.pojo.dto.PaperGenerationRequest;
import com.n1etzsch3.novi.question.pojo.dto.PaperHistoryItem;
import com.n1etzsch3.novi.question.service.PaperGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 套卷生成控制器
 * <p>
 * 提供套卷生成相关接口，包括并发生成套卷、查询历史记录等。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/api/v1/papers")
@RequiredArgsConstructor
@Slf4j
public class PaperGenerationController {

    private final PaperGenerationService paperGenerationService;

    /**
     * 生成套卷（SSE 流式推送）
     * <p>
     * 使用 Server-Sent Events 实时推送生成进度。
     * 前端应监听以下事件：
     * <ul>
     * <li>question - 单个题型生成完成</li>
     * <li>error - 某个题型生成失败</li>
     * <li>complete - 所有题型生成完成</li>
     * </ul>
     * </p>
     *
     * @param request 组卷请求参数
     * @return SSE Emitter
     */
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generatePaper(@RequestBody @Validated PaperGenerationRequest request) {
        Long userId = LoginUserContext.getUserId();
        log.info("Received paper generation request from user: {}, subjectId: {}, questionTypes: {}",
                userId, request.getSubjectId(), request.getPaperConfig().size());

        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5分钟超时

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout for user: {}", userId);
            emitter.complete();
        });

        emitter.onError((ex) -> {
            log.error("SSE connection error for user: {}", userId, ex);
            emitter.completeWithError(ex);
        });

        // 异步执行组卷任务
        paperGenerationService.generatePaperAsync(userId, request, emitter);

        return emitter;
    }

    /**
     * 获取套卷历史记录列表
     *
     * @return 历史记录列表
     */
    @GetMapping("/history")
    public Result<List<PaperHistoryItem>> getPaperHistory() {
        Long userId = LoginUserContext.getUserId();
        log.debug("Fetching paper history for user: {}", userId);
        return Result.success(paperGenerationService.getPaperHistory(userId));
    }

    /**
     * 获取套卷详情
     *
     * @param paperId 套卷ID
     * @return 套卷详情
     */
    @GetMapping("/{paperId}")
    public Result<PaperDetailResponse> getPaperDetail(@PathVariable Long paperId) {
        Long userId = LoginUserContext.getUserId();
        log.debug("Fetching paper detail for user: {}, paperId: {}", userId, paperId);
        return Result.success(paperGenerationService.getPaperDetail(paperId, userId));
    }

    /**
     * 删除套卷记录
     *
     * @param paperId 套卷ID
     * @return 成功提示
     */
    @DeleteMapping("/{paperId}")
    public Result<Void> deletePaper(@PathVariable Long paperId) {
        Long userId = LoginUserContext.getUserId();
        log.info("Deleting paper for user: {}, paperId: {}", userId, paperId);
        paperGenerationService.deletePaper(paperId, userId);
        return Result.success(null);
    }
}
