package com.n1etzsch3.novi.user.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "用户个人资料信息")
public class UserProfileDto {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "user123")
    private String username;

    @Schema(description = "昵称", example = "John Doe")
    private String nickname;

    @Schema(description = "邮箱", example = "john@example.com")
    private String email;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

}
