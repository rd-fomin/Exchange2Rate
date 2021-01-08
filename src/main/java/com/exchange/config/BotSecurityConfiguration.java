package com.exchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:/application-security.properties")
public class BotSecurityConfiguration {

    @Bean
    public BotData botValues(@Value("${bot.username}") String botUserName, @Value("${bot.token}") String botToken) {
        return new BotData(botUserName, botToken);
    }

}
