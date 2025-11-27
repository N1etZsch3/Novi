package com.n1etzsch3.novi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Novi 人设设置 DTO
 * <p>
 * 代表用户对 AI 人设的偏好设置。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoviPersonaSettings {

    /**
     * 核心性格模式。
     * <p>
     * 选项: "default", "witty", "gentle", "tsundere", "professional"。
     * </p>
     */
    private String personalityMode = "default";

    /**
     * Novi 对用户的称呼。
     * <p>
     * 例如: "主人", "亲爱的", "兄弟", 或用户的昵称。
     * </p>
     */
    private String userAddressName;

    /**
     * 语气风格。
     * <p>
     * 选项: "normal", "emoji_heavy", "concise", "verbose"。
     * </p>
     */
    private String toneStyle = "normal";

    /**
     * 语言限制。
     * <p>
     * 选项: null (自动), "zh_CN", "en_US"。
     * </p>
     */
    private String language;
}