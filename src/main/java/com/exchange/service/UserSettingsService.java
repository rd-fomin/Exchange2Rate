package com.exchange.service;

import com.exchange.model.UserSettings;

import java.util.List;

public interface UserSettingsService {

    void save(UserSettings userSettings);

    UserSettings findByUserId(int userId);

    List<UserSettings> findAll();

    void updateUserSettings(UserSettings userSettings);

    void updateUserValues(UserSettings userSettings);

    void deleteByUserId(int userId);

}
