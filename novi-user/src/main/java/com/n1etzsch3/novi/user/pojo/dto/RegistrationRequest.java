package com.n1etzsch3.novi.user.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户注册请求")
public class RegistrationRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4到20之间")
    @Schema(description = "用户名", example = "user123", minLength = 4, maxLength = 20)
    private String username;

    @Size(max = 20, message = "昵称长度不能超过20")
    @Schema(description = "昵称", example = "John Doe", maxLength = 20)
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 30, message = "密码长度必须在8到30之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].{7,}$", message = "密码必须至少包含一个大写字母、一个小写字母、一个数字和一个特殊字符")
    @Schema(description = "密码 (需包含大小写字母、数字和特殊字符)", example = "Password123!")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "john@example.com")
    private String email;
}