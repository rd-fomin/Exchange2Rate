package com.exchange.service;

import com.exchange.model.UserSettings;

import java.util.List;

public interface UserSettingsService {

    void save(UserSettings userSettings);

    UserSettings findByUserId(int userId);

    List<UserSettings> findAll();

    boolean updateUserSettings(UserSettings userSettings);

    boolean updateUserValues(UserSettings userSettings);

    boolean delete(int id);

}
