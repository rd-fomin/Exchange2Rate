package com.exchange.service;

import com.exchange.model.UserSettings;
import com.exchange.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UserSettingsServiceImpl(UserSettingsRepository UserSettingsRepository) {
        this.userSettingsRepository = UserSettingsRepository;
    }

    @Override
    public void save(UserSettings userSettings) {
        userSettingsRepository.save(userSettings);
    }

    @Override
    public UserSettings findByUserId(int userId) {
        return userSettingsRepository.findByUserId(userId);
    }

    @Override
    public List<UserSettings> findAll() {
        return userSettingsRepository.findAll();
    }

    @Override
    public boolean update(UserSettings userSettings) {
        if (userSettingsRepository.existsByUserId(userSettings.getUserId())) {
            userSettingsRepository.updateUserSettings(userSettings.getUserId(), userSettings.getCurrencyCode());
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        if (userSettingsRepository.existsById(id)) {
            userSettingsRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
