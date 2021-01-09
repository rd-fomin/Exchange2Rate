package com.exchange.config;

import com.exchange.Bot;
import com.exchange.CurrencyCursComponent;
import com.exchange.UserSettingsComponent;
import com.exchange.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@PropertySource(value = "classpath:/application.yaml")
public class BotCommonConfiguration {

    @Bean
    public Bot bot(BotData botData, UserSettingsComponent userSettingsComponent, UserSettingsService userSettingsService, CurrencyCursComponent currencyCursComponent) {
        return new Bot(botData, userSettingsComponent, userSettingsService, currencyCursComponent);
    }

    @Bean
    public BotCron botCron(@Value("${bot.cron.update}") String cronGetUpdate, @Value("${bot.cron.notify}") String cronSendNotify) {
        return new BotCron(cronGetUpdate, cronSendNotify);
    }

    @Bean
    public BotUrl botUrl(@Value("${bot.url}") String botUrl) {
        return new BotUrl(botUrl);
    }

    @Bean
    public UserSettingsComponent userSettingsComponent(CurrencyCursComponent currencyCursComponent) {
        return new UserSettingsComponent(currencyCursComponent);
    }

    @Bean
    public CurrencyCursComponent currencyCursComponent(BotUrl botUrl) {
        return new CurrencyCursComponent(botUrl);
    }

    @Scheduled(cron = "${bot.cron.update}")
    public synchronized void refreshCurrencyRate() {
        var currencyCurs = currencyCursComponent(botUrl("http://www.cbr.ru/scripts/XML_daily.asp")).currencyCurs().orElseThrow(() -> {
            throw new RuntimeException("Не получилось обновить данные с сайта");
        });
        currencyCursComponent(botUrl("http://www.cbr.ru/scripts/XML_daily.asp"))
                .setCurrencyMap(currencyCursComponent(botUrl("http://www.cbr.ru/scripts/XML_daily.asp")).listToMap(currencyCurs.getCurrencies()))
                .setCurrencyDate(currencyCurs.getDate());
    }

}
