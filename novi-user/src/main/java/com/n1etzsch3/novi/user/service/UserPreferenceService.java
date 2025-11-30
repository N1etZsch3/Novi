package com.n1etzsch3.novi.user.service;

import com.n1etzsch3.novi.user.pojo.dto.NoviPersonaSettings;

public interface UserPreferenceService {

    NoviPersonaSettings getPersonaSettings(Long userId);
    NoviPersonaSettings updatePersonaSettings(Long userId, NoviPersonaSettings settings);

}