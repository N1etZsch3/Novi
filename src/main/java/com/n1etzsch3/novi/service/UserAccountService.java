package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.pojo.dto.*;

import java.util.Map;

public interface UserAccountService {

    void registerUser(RegistrationRequest registrationRequest);

    LoginRespond login(LoginRequest loginRequest);


    UserProfileDto getUserDetailsById(Long userId);

    void updateUserProfile(Long userId, UserProfileUpdateRequest updateRequest);

//    Map<String, Object> getUserPreferences(Long userId);
//
//    Map<String, Object> updateUserPreferences(Long userId, Map<String, Object> preferences);

}
