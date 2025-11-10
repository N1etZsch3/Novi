package com.n1etzsch3.novi.pojo.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class LoginRequest {
    private String username;
    private String password;
}
