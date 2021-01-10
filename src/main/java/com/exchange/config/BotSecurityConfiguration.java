package com.exchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:/application-security.properties")
public class BotSecurityConfiguration {
    private final String botUserName;
    private final String botToken;

    public BotSecurityConfiguration(@Value("${bot.username}") String botUserName, @Value("${bot.token}") String botToken) {
        this.botUserName = botUserName;
        this.botToken = botToken;
    }

    @Bean
    public BotData botData() {
        return new BotData(botUserName, botToken);
    }

}
