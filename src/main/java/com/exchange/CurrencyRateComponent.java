package com.exchange;

import com.exchange.config.BotUrl;
import com.exchange.model.Currency;
import com.exchange.model.CurrencyRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CurrencyRateComponent {
    private static final Logger LOGGER = LogManager.getLogger(CurrencyRateComponent.class);
    private final BotUrl botUrl;
    private Map<String, Currency> currencyMap;
    private String currencyDate;

    public CurrencyRateComponent(BotUrl botUrl) {
        this.botUrl = botUrl;
        var currencyCurs = currencyCurs().orElseThrow(() -> {
            LOGGER.error("Не получилось обновить данные с сайта");
            throw new RuntimeException("Не получилось обновить данные с сайта");
        });
        var currencyList = currencyCurs.getCurrencies();
        currencyMap = listToMap(currencyList);
        currencyDate = currencyCurs.getDate();
    }

    public Map<String, Currency> getCurrencyMap() {
        return currencyMap;
    }

    public synchronized CurrencyRateComponent setCurrencyMap(Map<String, Currency> currencyMap) {
        this.currencyMap = currencyMap;
        return this;
    }

    public String getCurrencyDate() {
        return currencyDate;
    }

    public synchronized CurrencyRateComponent setCurrencyDate(String currencyDate) {
        this.currencyDate = currencyDate;
        return this;
    }

    public HashMap<String, Currency> listToMap(List<Currency> currencyList) {
        return currencyList.stream()
                .collect(Collectors.toMap(
                        Currency::getCharCode,
                        Function.identity(),
                        (key, currency) -> currency,
                        HashMap::new
                ));
    }

    public Optional<CurrencyRate> currencyCurs() {
        try {
            var url = new URL(botUrl.getUrl());
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
