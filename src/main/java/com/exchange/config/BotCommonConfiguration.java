package com.exchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:/application.yaml")
public class BotCommonConfiguration {

    @Bean
    public BotCron botCron(@Value("${bot.cron.update}") String cronGetUpdate, @Value("${bot.cron.notify}") String cronSendNotify) {
        return new BotCron(cronGetUpdate, cronSendNotify);
    }

    @Bean
    public BotUrl botUrl(@Value("${bot.url}") String botUrl) {
        return new BotUrl(botUrl);
    }

}
