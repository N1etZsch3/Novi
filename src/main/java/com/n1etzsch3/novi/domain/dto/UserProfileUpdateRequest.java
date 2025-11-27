package com.n1etzsch3.novi.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 20, message = "昵称长度不能超过20")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    // --- 密码修改相关 ---

    @Size(min = 8, max = 30, message = "新密码长度需在8-30之间")
    private String newPassword;

    /**
     * 修改敏感信息（如密码）时，通常需要验证旧密码以确保安全
     */
    private String currentPassword;
}