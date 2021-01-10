package com.exchange.config;

import com.exchange.Bot;
import com.exchange.component.CurrencyRateComponent;
import com.exchange.component.TempUserComponent;
import com.exchange.service.RefreshCurrencyRateService;
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
    private final UserSettingsService userSettingsService;

    public BotCommonConfiguration(BotSecurityConfiguration botSecurityConfig, @Value("${bot.url}") String botUrl, UserSettingsService userSettingsService) {
        this.botSecurityConfig = botSecurityConfig;
        this.botUrl = botUrl;
        this.userSettingsService = userSettingsService;
    }

    @Bean
    public Bot bot() {
        return new Bot(botSecurityConfig.botData(), tempUserService(), userSettingsService, currencyRateComponent());
    }

    @Bean
    public BotUrl botUrl() {
        return new BotUrl(botUrl);
    }

    @Bean
    public TempUserComponent tempUserService() {
        return new TempUserComponent(currencyRateComponent());
    }

    @Bean
    public CurrencyRateComponent currencyRateComponent() {
        var curRateService = refreshCurrencyRateService();
        var curRate = curRateService.currencyCurs().orElseThrow(() -> {
            LOGGER.error("Не получилось обновить данные с сайта");
            throw new RuntimeException("Не получилось обновить данные с сайта");
        });
        return new CurrencyRateComponent(curRate.getDate(), curRate.getCurrencies());
    }

    @Bean
    public RefreshCurrencyRateService refreshCurrencyRateService() {
        return new RefreshCurrencyRateService(botUrl());
    }

    @Scheduled(cron = "${bot.cron.update}")
    public void refreshCurrencyRate() {
        var curRateService = refreshCurrencyRateService();
        var curRate = curRateService.currencyCurs().orElseThrow(() -> {
            LOGGER.error("Не получилось обновить данные с сайта");
            throw new RuntimeException("Не получилось обновить данные с сайта");
        });
        if (curRate.getDate().equals(currencyRateComponent().getCurrencyDate())) {
            LOGGER.warn("Данные на сайте ЦБ еще не обновились");
        } else {
            currencyRateComponent().setCurrencyDateAndMap(curRate.getDate(), curRate.getCurrencies());
            LOGGER.info("Данные обновлены");
            bot().sendMsgWithNotification();
            LOGGER.info("Уведомления пользователям отправлены");
        }
    }

}
