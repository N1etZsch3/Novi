package com.n1etzsch3.novi.user.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户登录响应")
public class LoginRespond {
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    @Schema(description = "用户名", example = "user123")
    private String username;
}
