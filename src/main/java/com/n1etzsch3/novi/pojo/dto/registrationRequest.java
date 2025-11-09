package com.n1etzsch3.novi.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class registrationRequest {

    String username;
    String nickname;
    String password;
    String email;

}
