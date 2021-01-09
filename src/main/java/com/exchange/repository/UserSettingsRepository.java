package com.exchange.repository;

import com.exchange.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Map;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Integer> {

    UserSettings findByUserId(int userId);

    void deleteByUserId(int userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserSettings u SET u.currencyCode = :currencyCode WHERE u.userId = :userId")
    void updateUserSettings(@Param("userId") int userId, @Param("currencyCode") Map<String, Boolean> currencyCode);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserSettings u SET u.currencyValue = :currencyValue WHERE u.userId = :userId")
    void updateUserValues(@Param("userId") int userId, @Param("currencyValue") Map<String, String> currencyValue);

}
