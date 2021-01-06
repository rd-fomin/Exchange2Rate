package com.exchange;

import com.exchange.model.Currency;
import com.exchange.model.UserSettings;
import com.exchange.utils.BotUtils;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@PropertySource(value = "classpath:/application.properties")
public class UserSettingsComponent {
    private Map<String, Currency> currencyMap;
    private String currencyDate;
    private final InlineKeyboardMarkup inlineKeyboardForCurrencyChoose;
    private final Map<Integer, Map<String, Boolean>> tempUsersSettings = new HashMap<>();

    public UserSettingsComponent() {
        var currencyCurs = BotUtils.getCurrencyCursFromSite().orElseThrow();
        var currencyList = currencyCurs.getValutes();
        currencyMap = listToMap(currencyList);
        currencyDate = currencyCurs.getDate();
        var keyboardButtons = Lists.partition(currencyList, 2).stream()
                .map(currencies -> currencies.stream()
                        .map(currency -> new InlineKeyboardButton().setCallbackData(currency.getCharCode()))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setText("Cancel").setCallbackData("Cancel"),
                        new InlineKeyboardButton().setText("Done").setCallbackData("Done")
                )
        );
        inlineKeyboardForCurrencyChoose = new InlineKeyboardMarkup();
        inlineKeyboardForCurrencyChoose.setKeyboard(keyboardButtons);
    }

    public Map<String, Currency> getCurrencyMap() {
        return currencyMap;
    }

    public String getCurrencyDate() {
        return currencyDate;
    }

    public void addOrUpdateTempUser(UserSettings userSettings) {
        tempUsersSettings.put(userSettings.getUserId(), userSettings.getCurrencyCode());
    }

    private Map<String, Boolean> getCurrencyForTempUser(int id) {
        return tempUsersSettings.get(id);
    }

    public void switchValueForTempUser(int id, String charCode) {
        tempUsersSettings.get(id).put(charCode, !tempUsersSettings.get(id).get(charCode));
    }

    public Map<String, Boolean> removeTempUserSettings(int id) {
        return tempUsersSettings.remove(id);
    }

    public InlineKeyboardMarkup updateAndGetKeyboardButtons(int id) {
        inlineKeyboardForCurrencyChoose.getKeyboard().forEach(inlineKeyboardButtons -> inlineKeyboardButtons
                .forEach(inlineKeyboardButton -> {
                    if (!inlineKeyboardButton.getCallbackData().equals("Done") && !inlineKeyboardButton.getCallbackData().equals("Cancel"))
                        inlineKeyboardButton.setText(String.format("%s %s %s",
                                BotUtils.getFlagUnicode(inlineKeyboardButton.getCallbackData()),
                                inlineKeyboardButton.getCallbackData(),
                                getCurrencyForTempUser(id).get(inlineKeyboardButton.getCallbackData()) ? "✅" : "❌"));
                }));
        return inlineKeyboardForCurrencyChoose;
    }

    private HashMap<String, Currency> listToMap(List<Currency> currencyList) {
        return currencyList.stream()
                .collect(Collectors.toMap(
                        Currency::getCharCode,
                        Function.identity(),
                        (key, currency) -> currency,
                        HashMap::new
                ));
    }

    @Scheduled(cron = "${bot.cron}")
    public void refreshCurrencyRate() {
        var currencyCurs = BotUtils.getCurrencyCursFromSite().orElseThrow();
        currencyMap = listToMap(currencyCurs.getValutes());
        currencyDate = currencyCurs.getDate();
    }

}
