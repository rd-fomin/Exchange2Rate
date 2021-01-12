package com.exchange.config;

import com.exchange.Bot;
import com.exchange.component.CurrencyRateComponent;
import com.exchange.component.WorkWithTemporaryUserComponent;
import com.exchange.service.RefreshCurrencyRateService;
import com.exchange.service.UserSettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@PropertySource(value = "classpath:/application.yaml")
public class BotCommonConfiguration {
    private static final Logger LOGGER = LogManager.getLogger(BotCommonConfiguration.class);
    private final UserSettingsService userSettingsService;
    private final RefreshCurrencyRateService refreshCurRateService;
    private final BotConfiguration botConfiguration;

    public BotCommonConfiguration(UserSettingsService userSettingsService, RefreshCurrencyRateService refreshCurRateService, BotConfiguration botConfiguration) {
        this.userSettingsService = userSettingsService;
        this.refreshCurRateService = refreshCurRateService;
        this.botConfiguration = botConfiguration;
    }

    @Bean
    public Bot bot() {
        return new Bot(botConfiguration, tempUserService(), userSettingsService, currencyRateComponent());
    }

    @Bean
    public WorkWithTemporaryUserComponent tempUserService() {
        return new WorkWithTemporaryUserComponent(currencyRateComponent());
    }

    @Bean
    public CurrencyRateComponent currencyRateComponent() {
        var curRate = refreshCurRateService.currencyCurs().orElseThrow(() -> {
            LOGGER.error("Не получилось обновить данные с сайта");
            throw new RuntimeException("Не получилось обновить данные с сайта");
        });
        return new CurrencyRateComponent(curRate.getDate(), curRate.getCurrencies());
    }

    @Scheduled(cron = "${bot.cron.update}")
    public void refreshCurrencyRate() {
        var curRate = refreshCurRateService.currencyCurs().orElseThrow(() -> {
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
