package com.n1etzsch3.novi.question.service;

import com.n1etzsch3.novi.question.pojo.dto.PaperDetailResponse;
import com.n1etzsch3.novi.question.pojo.dto.PaperGenerationRequest;
import com.n1etzsch3.novi.question.pojo.dto.PaperHistoryItem;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 套卷生成服务接口
 * <p>
 * 负责根据配置并发生成套卷，并通过 SSE 推送进度。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
public interface PaperGenerationService {

    /**
     * 异步生成套卷（使用 SSE 推送进度）
     *
     * @param userId  用户ID
     * @param request 组卷请求配置
     * @param emitter SSE 发射器
     */
    void generatePaperAsync(Long userId, PaperGenerationRequest request, SseEmitter emitter);

    /**
     * 获取套卷历史列表
     *
     * @param userId 用户ID
     * @return 历史记录列表
     */
    List<PaperHistoryItem> getPaperHistory(Long userId);

    /**
     * 获取套卷详情
     *
     * @param paperId 套卷ID
     * @param userId  用户ID（用于权限验证）
     * @return 套卷详情
     */
    PaperDetailResponse getPaperDetail(Long paperId, Long userId);

    /**
     * 删除套卷记录
     *
     * @param paperId 套卷ID
     * @param userId  用户ID
     */
    void deletePaper(Long paperId, Long userId);

    /**
     * 批量删除套卷记录
     *
     * @param paperIds 套卷ID列表
     * @param userId   用户ID
     */
    void deletePapers(List<Long> paperIds, Long userId);
}
