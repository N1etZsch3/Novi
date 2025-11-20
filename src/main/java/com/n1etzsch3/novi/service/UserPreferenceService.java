package com.n1etzsch3.novi.service;

import com.n1etzsch3.novi.pojo.dto.NoviPersonaSettings;

public interface UserPreferenceService {

    NoviPersonaSettings getPersonaSettings(Long userId);
    NoviPersonaSettings updatePersonaSettings(Long userId, NoviPersonaSettings settings);

}