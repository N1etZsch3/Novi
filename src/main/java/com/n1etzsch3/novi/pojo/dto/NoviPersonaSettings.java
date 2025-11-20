package com.n1etzsch3.novi.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoviPersonaSettings {

    /**
     * 核心性格模式
     * 可选值: "default"(默认), "witty"(风趣), "gentle"(温柔), "tsundere"(傲娇), "professional"(专业)
     */
    private String personalityMode = "default";

    /**
     * Novi 对用户的称呼
     * 例如: "主人", "亲爱的", "老兄", 或者用户的昵称
     */
    private String userAddressName;

    /**
     * 语气风格
     * 可选值: "normal"(正常), "emoji_heavy"(多表情), "concise"(简洁), "verbose"(话痨)
     */
    private String toneStyle = "normal";

    /**
     * 回复语言限制
     * 可选值: null (自动), "zh_CN", "en_US"
     */
    private String language;
}