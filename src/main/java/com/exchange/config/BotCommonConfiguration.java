package com.exchange.config;

import com.exchange.Bot;
import com.exchange.CurrencyRateComponent;
import com.exchange.TempUserComponent;
import com.exchange.service.UserSettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@PropertySource(value = "classpath:/application.yaml")
public class BotCommonConfiguration {
    private static final Logger LOGGER = LogManager.getLogger(BotCommonConfiguration.class);
    private final BotSecurityConfiguration botSecurityConfig;
    private final String botUrl;
    private UserSettingsService userSettingsService;

    public BotCommonConfiguration(BotSecurityConfiguration botSecurityConfig, @Value("${bot.url}") String botUrl) {
        this.botSecurityConfig = botSecurityConfig;
        this.botUrl = botUrl;
    }

    @Bean
    public Bot bot(TempUserComponent tempUserComponent, UserSettingsService userSettingsService, CurrencyRateComponent currencyRateComponent) {
        this.userSettingsService = userSettingsService;
        return new Bot(botSecurityConfig.botData(), tempUserComponent, userSettingsService, currencyRateComponent);
    }

    @Bean
    public BotUrl botUrl() {
        return new BotUrl(botUrl);
    }

    @Bean
    public TempUserComponent tempUserComponent(CurrencyRateComponent currencyRateComponent) {
        return new TempUserComponent(currencyRateComponent);
    }

    @Bean
    public CurrencyRateComponent curRateComponent(BotUrl botUrl) {
        return new CurrencyRateComponent(botUrl);
    }

    @Scheduled(cron = "${bot.cron.update}")
    public void refreshCurrencyRate() {
        var curRateComponent = curRateComponent(botUrl());
        var curRate = curRateComponent.currencyCurs().orElseThrow(() -> {
            LOGGER.error("Не получилось обновить данные с сайта");
            throw new RuntimeException("Не получилось обновить данные с сайта");
        });
        if (curRate.getDate().equals(curRateComponent.getCurrencyDate())) {
            LOGGER.warn("Данные на сайте ЦБ еще не обновились");
        } else {
            curRateComponent
                    .setCurrencyMap(curRateComponent.listToMap(curRate.getCurrencies()))
                    .setCurrencyDate(curRate.getDate());
            LOGGER.info("Данные обновлены");
            bot(tempUserComponent(curRateComponent), userSettingsService, curRateComponent).sendMsgWithNotification();
        }
    }

}
