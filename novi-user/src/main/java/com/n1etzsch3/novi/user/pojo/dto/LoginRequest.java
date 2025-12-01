package com.n1etzsch3.novi.user.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class LoginRequest {
    @NotBlank(message = "Username cannot be empty")
    @Schema(description = "用户名", example = "user123")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Schema(description = "密码", example = "password123")
    private String password;
}