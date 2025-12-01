package com.n1etzsch3.novi.user.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户资料更新请求")
public class UserProfileUpdateRequest {

    @Size(max = 20, message = "昵称长度不能超过20")
    @Schema(description = "新昵称", example = "Johnny")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "新邮箱", example = "johnny@example.com")
    private String email;

    // --- 密码修改相关 ---

    @Size(min = 8, max = 30, message = "新密码长度需在8-30之间")
    @Schema(description = "新密码 (若不修改则留空)", example = "NewPassword123!")
    private String newPassword;

    /**
     * 修改敏感信息（如密码）时，通常需要验证旧密码以确保安全
     */
    @Schema(description = "当前密码 (修改密码时必填)", example = "Password123!")
    private String currentPassword;
}