package com.n1etzsch3.novi.user.service;

import com.n1etzsch3.novi.common.pojo.dto.*;
import com.n1etzsch3.novi.user.pojo.dto.*;

public interface UserAccountService {

    void registerUser(RegistrationRequest registrationRequest);

    LoginRespond login(LoginRequest loginRequest);

    UserProfileDto getUserDetailsById(Long userId);

    void updateUserProfile(Long userId, UserProfileUpdateRequest updateRequest);

    java.util.Map<String, Object> getUserPreferences(Long userId);

    java.util.Map<String, Object> updateUserPreferences(Long userId, java.util.Map<String, Object> preferences);

}
