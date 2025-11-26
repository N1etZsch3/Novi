package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.mapper.ChatSessionMapper;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.ChatRequest;
import com.n1etzsch3.novi.pojo.dto.ChatResponse;
import com.n1etzsch3.novi.pojo.dto.NoviPersonaSettings;
import com.n1etzsch3.novi.pojo.dto.StreamEvent;
import com.n1etzsch3.novi.pojo.entity.ChatSession;
import com.n1etzsch3.novi.pojo.entity.UserAccount;
import com.n1etzsch3.novi.service.ChatService;
import com.n1etzsch3.novi.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserPreferenceService userPreferenceService;
    private final com.n1etzsch3.novi.service.AiPromptConfigService aiPromptConfigService;

    /**
     * 辅助方法：创建复合键
     */
    private String createCompositeKey(Long userId, String sessionId) {
        return userId + ":" + sessionId;
    }

    private record SessionInfo(String sessionId, String title) {
    }

    /**
     * 辅助方法：获取或创建会话
     */
    private SessionInfo getOrCreateSession(Long userId, String requestedSessionId, String messageContent) {
        boolean isNewSession = !StringUtils.hasText(requestedSessionId);
        String finalSessionId = isNewSession ? UUID.randomUUID().toString() : requestedSessionId;
        String finalTitle = null;

        if (isNewSession) {
            ChatSession session = new ChatSession();
            session.setId(finalSessionId);
            session.setUserId(userId);
            finalTitle = messageContent != null && messageContent.length() > 20
                    ? messageContent.substring(0, 20) + "..."
                    : messageContent;
            session.setTitle(finalTitle);
            chatSessionMapper.createSession(session);
            log.info("创建新会话 (DB): {}, 标题: {}", finalSessionId, finalTitle);
        } else {
            int rows = chatSessionMapper.updateLastActiveTime(finalSessionId);
            if (rows == 0) {
                log.warn("会话 {} 在数据库不存在，重新创建", finalSessionId);
                finalTitle = "恢复的会话";
                ChatSession session = new ChatSession();
                session.setId(finalSessionId);
                session.setUserId(userId);
                session.setTitle(finalTitle);
                chatSessionMapper.createSession(session);
            } else {
                finalTitle = null;
            }
        }
        return new SessionInfo(finalSessionId, finalTitle);
    }

    // --- 1. 阻塞式调用 ---
    @Override
    public ChatResponse handleCallMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // 1. 准备 System Message
        Message systemMessage = buildSystemMessage(userId, userMessage);

        // 2. 获取 Session
        SessionInfo sessionInfo = getOrCreateSession(userId, request.getSessionId(), userMessage);
        String compositeKey = createCompositeKey(userId, sessionInfo.sessionId());

        // 3. 调用 AI
        String AIResponse = chatClient.prompt()
                .messages(systemMessage) // 注入动态 System Prompt
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, compositeKey))
                .call()
                .content();

        return new ChatResponse(AIResponse, sessionInfo.sessionId(), sessionInfo.title());
    }

    // --- 2. 流式调用 ---
    @Override
    public Flux<String> handleStreamMessage(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        // 1. 【新增】准备 System Message (逻辑与阻塞式一致)
        Message systemMessage = buildSystemMessage(userId, userMessage);

        // 2. 获取 Session
        SessionInfo sessionInfo = getOrCreateSession(userId, request.getSessionId(), userMessage);
        String sessionIdToUse = sessionInfo.sessionId();
        String compositeKey = createCompositeKey(userId, sessionIdToUse);

        // 3. 创建元数据事件
        StreamEvent metadataEvent = StreamEvent.metadata(sessionIdToUse, sessionInfo.title());
        Flux<StreamEvent> metadataStream = Flux.just(metadataEvent);

        // 4. 【修改】AI 内容流 - 注入 systemMessage
        Flux<StreamEvent> contentStream = chatClient.prompt()
                .messages(systemMessage) // <--- 关键：这里也要注入 System Prompt
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, compositeKey))
                .stream()
                .content()
                .map(StreamEvent::content);

        // 5. 拼接并序列化
        return Flux.concat(metadataStream, contentStream)
                .<String>handle((event, sink) -> {
                    try {
                        sink.next(objectMapper.writeValueAsString(event));
                    } catch (JsonProcessingException e) {
                        log.error("序列化失败", e);
                        sink.error(new RuntimeException("Stream serialization error", e));
                    }
                })
                .doOnError(e -> log.error("流式处理错误, 用户: {}", userId, e))
                .doOnComplete(() -> log.info("流式响应完成, 会话: {}", sessionIdToUse));
    }

    /**
     * 【新增】抽取公共方法，构建动态的 System Message
     * 避免代码重复，保证流式和阻塞式的人设一致
     */
    private Message buildSystemMessage(Long userId, String userMessage) {
        // 1. 获取用户信息
        UserAccount user = userAccountMapper.findById(userId);

        // 2. 获取用户偏好
        NoviPersonaSettings settings = userPreferenceService.getPersonaSettings(userId);
        if (settings == null) {
            settings = new NoviPersonaSettings(); // 兜底
        }

        // 3. 确定称呼 (逻辑：偏好设置中的称呼 > 账号昵称 > "老铁")
        String nickname = StringUtils.hasText(settings.getUserAddressName())
                ? settings.getUserAddressName()
                : (StringUtils.hasText(user.getNickname()) ? user.getNickname() : "老铁");

        // 4. 构建自然语言的人设描述 (将配置项转换为 Prompt)
        String personalityDesc = buildPersonalityDescription(settings);

        log.info("用户 {} 的最终人设 Prompt: {}", userId, personalityDesc);

        // 5. 获取记忆 (TODO: 接入向量数据库或检索逻辑)
        String memories = "（暂无特殊记忆，就像平时一样闲聊）";

        // === 修改开始：增强时间感知 ===
        LocalDateTime now = LocalDateTime.now();

        // 1. 基础时间: 16:30
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        // 2. 日期与星期: 2025年11月21日 星期五
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        String weekStr = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);

        // 3. 简单的季节/体感推断 (根据月份给 AI 一个环境暗示)
        int month = now.getMonthValue();
        String seasonHint;
        if (month >= 3 && month <= 5)
            seasonHint = "春季，气温回暖";
        else if (month >= 6 && month <= 8)
            seasonHint = "夏季，天气炎热";
        else if (month >= 9 && month <= 11)
            seasonHint = "秋季，天气转凉";
        else
            seasonHint = "冬季，天气寒冷";

        // === 修改结束 ===

        // 6. 构建并返回 System Message
        String systemPromptTemplateStr = aiPromptConfigService.getSystemPromptTemplate();
        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptTemplateStr);
        Map<String, Object> promptVars = Map.of(
                "nickname", nickname,
                "personality", personalityDesc,
                "current_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                "memories", memories,
                "user_message", userMessage,
                "current_date", dateStr + " " + weekStr,
                "season_hint", seasonHint);

        return systemTemplate.createMessage(promptVars);
    }

    /**
     * 【新增辅助方法】将结构化的偏好设置转换为 AI 能理解的自然语言描述
     */
    private String buildPersonalityDescription(NoviPersonaSettings settings) {
        StringBuilder desc = new StringBuilder();

        // A. 处理核心性格 (Personality Mode)
        String mode = settings.getPersonalityMode();
        // 防止 null
        if (mode == null)
            mode = "default";

        switch (mode) {
            case "default" -> desc.append(aiPromptConfigService.getPersonalityDescription("personality_default"));
            case "witty" -> desc.append(aiPromptConfigService.getPersonalityDescription("personality_witty"));
            case "gentle" -> desc.append(aiPromptConfigService.getPersonalityDescription("personality_gentle"));
            case "professional" ->
                desc.append(aiPromptConfigService.getPersonalityDescription("personality_professional"));
            case "tsundere" -> desc.append(aiPromptConfigService.getPersonalityDescription("personality_tsundere"));
            default -> {
                // 如果不是预设值，说明是用户自定义的 Prompt，直接使用
                desc.append(mode);
            }
        }

        // B. 处理语气风格 (Tone Style)
        String tone = settings.getToneStyle();
        if ("emoji_heavy".equals(tone)) {
            desc.append(" 另外，请在每句话中大量使用Emoji表情(✨、🎉、😂等)来表达情绪，显得非常活泼。");
        } else if ("concise".equals(tone)) {
            desc.append(" 回复必须非常简短，惜字如金，能用两个字说完绝不用三个字。");
        } else if ("verbose".equals(tone)) {
            desc.append(" 可以稍微话痨一点，多发散思维，多聊聊细节。");
        }

        // C. 处理语言限制 (Language)
        String lang = settings.getLanguage();
        if ("zh_CN".equals(lang)) {
            desc.append(" 请全程强制使用中文回复，即使我用英文问你。");
        } else if ("en_US".equals(lang)) {
            desc.append("必须全程英文回答！即便我用中文问你，你的回复也必须是英文！严禁出现中文！");
        }

        return desc.toString();
    }
}