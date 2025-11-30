package com.n1etzsch3.novi.user.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserProfileDto {

    private Integer id;
    private String username;
    private String nickname;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
