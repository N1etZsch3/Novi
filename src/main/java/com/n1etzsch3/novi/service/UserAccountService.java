package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.pojo.dto.LoginRequest;
import com.n1etzsch3.novi.pojo.dto.LoginRespond;
import com.n1etzsch3.novi.pojo.dto.UserProfileDto;
import com.n1etzsch3.novi.pojo.dto.registrationRequest;

public interface UserAccountService {

    void registerUser(registrationRequest registrationRequest);

    LoginRespond login(LoginRequest loginRequest);


    UserProfileDto getUserDetailsByUsername(String username);
}
