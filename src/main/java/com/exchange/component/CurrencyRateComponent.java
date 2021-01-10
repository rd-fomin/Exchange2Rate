package com.exchange.component;

import com.exchange.model.Currency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CurrencyRateComponent {
    private String currencyDate;
    private Map<String, Currency> currencyMap;

    public CurrencyRateComponent(String currencyDate, List<Currency> currencyList) {
        this.currencyDate = currencyDate;
        this.currencyMap = listToMap(currencyList);
    }

    public CurrencyRateComponent(String currencyDate, Map<String, Currency> currencyMap) {
        this.currencyDate = currencyDate;
        this.currencyMap = currencyMap;
    }

    public synchronized Map<String, Currency> getCurrencyMap() {
        return currencyMap;
    }

    public synchronized String getCurrencyDate() {
        return currencyDate;
    }

    public synchronized CurrencyRateComponent setCurrencyDateAndMap(String currencyDate, Map<String, Currency> currencyMap) {
        this.currencyDate = currencyDate;
        this.currencyMap = currencyMap;
        return this;
    }

    public synchronized CurrencyRateComponent setCurrencyDateAndMap(String currencyDate, List<Currency> currencyList) {
        this.currencyDate = currencyDate;
        this.currencyMap = listToMap(currencyList);
        return this;
    }

    private Map<String, Currency> listToMap(List<Currency> currencyList) {
        return currencyList.stream()
                .collect(Collectors.toMap(
                        Currency::getCharCode,
                        Function.identity(),
                        (key, currency) -> currency,
                        HashMap::new
                ));
    }

}
