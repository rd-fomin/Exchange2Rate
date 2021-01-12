package com.exchange.service;

import com.exchange.config.BotConfiguration;
import com.exchange.model.CurrencyRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RefreshCurrencyRateService {
    private static final Logger LOGGER = LogManager.getLogger(RefreshCurrencyRateService.class);
    private final BotConfiguration botConfiguration;

    public RefreshCurrencyRateService(BotConfiguration botConfiguration) {
        this.botConfiguration = botConfiguration;
    }

    public Optional<CurrencyRate> currencyCurs() {
        try {
            var url = new URL(botConfiguration.getBotUrl());
            var urlConnection = (HttpURLConnection) url.openConnection();
            try (
                    var inputStream = urlConnection.getInputStream();
                    var bufReader = new BufferedReader(new InputStreamReader(inputStream, "windows-1251"))
            ) {
                var exchangeRateLine = new StringReader(bufReader.lines().collect(Collectors.joining()));
                var unmarshaller = JAXBContext.newInstance(CurrencyRate.class).createUnmarshaller();
                var currencyCurs = (CurrencyRate) unmarshaller.unmarshal(exchangeRateLine);
                LOGGER.info(MessageFormat.format("Данные с сайта ЦБ получены: {0}", currencyCurs));
                return Optional.of(currencyCurs);
            }
        } catch (MalformedURLException e) {
            LOGGER.error(MessageFormat.format("Что-то не так с подключением: {0}", e.getMessage()));
        } catch (JAXBException e) {
            LOGGER.error(MessageFormat.format("Что-то не так с преобразованием объекта из XML: {0}", e.getMessage()));
        } catch (IOException e) {
            LOGGER.error(MessageFormat.format("Что-то не так: {0}", e.getMessage()));
        }
        return Optional.empty();
    }

}
