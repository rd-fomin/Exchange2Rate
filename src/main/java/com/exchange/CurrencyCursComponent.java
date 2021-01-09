package com.exchange;

import com.exchange.config.BotUrl;
import com.exchange.model.Currency;
import com.exchange.model.CurrencyCurs;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CurrencyCursComponent {
    private static final Logger LOGGER = LogManager.getLogger(CurrencyCursComponent.class);
    private final BotUrl botUrl;
    private Map<String, Currency> currencyMap;
    private String currencyDate;

    public CurrencyCursComponent(BotUrl botUrl) {
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

    public synchronized CurrencyCursComponent setCurrencyMap(Map<String, Currency> currencyMap) {
        this.currencyMap = currencyMap;
        return this;
    }

    public String getCurrencyDate() {
        return currencyDate;
    }

    public synchronized CurrencyCursComponent setCurrencyDate(String currencyDate) {
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

    public Optional<CurrencyCurs> currencyCurs() {
        try {
            var url = new URL(botUrl.getUrl());
            var urlConnection = (HttpURLConnection) url.openConnection();
            try (
                    var inputStream = urlConnection.getInputStream();
                    var bufReader = new BufferedReader(new InputStreamReader(inputStream, "windows-1251"))
            ) {
                var exchangeRateLine = new StringReader(bufReader.lines().collect(Collectors.joining()));
                var unmarshaller = JAXBContext.newInstance(CurrencyCurs.class).createUnmarshaller();
                var currencyCurs = (CurrencyCurs) unmarshaller.unmarshal(exchangeRateLine);
                return Optional.of(currencyCurs);
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Something went wrong with connection: " + e.getMessage());
        } catch (JAXBException e) {
            LOGGER.error("Something went wrong with unmarshal: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }

}
