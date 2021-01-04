package com.exchange;

import com.exchange.model.Currency;
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
public class SettingsComponent {
    public Map<String, Currency> currencyMap;
    public final InlineKeyboardMarkup inlineKeyboardMarkup;
    public final Map<Integer, Map<String, Currency>> usersSettings = new HashMap<>();
    public final Map<Integer, Map<String, Currency>> tempUsersSettings = new HashMap<>();

    public SettingsComponent() {
        var currencyList = BotUtils.getCurrencyCursFromSite().orElseThrow();
        currencyMap = listToMap(currencyList);
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
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboardButtons);
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

    public String convertMapToString(int id) {
        return usersSettings.get(id).values().stream()
                .filter(Currency::isSelected)
                .map(currency -> String.format("%s %s\n", BotUtils.getFlagUnicode(currency.getCharCode()), currency.getCharCode()))
                .collect(Collectors.joining());
    }

    public void addOrUpdateUser(int id) {
        addOrUpdateUser(id, currencyMap);
    }

    private void addOrUpdateUser(int id, Map<String, Currency> currencyMap) {
        usersSettings.put(id, new HashMap<>(currencyMap));
    }

    public Map<String, Currency> getCurrencyForUser(int id) {
        return usersSettings.get(id);
    }

    public void addOrUpdateTempUser(int id) {
        Map<String, Currency> map = new HashMap<>();
        usersSettings.get(id).forEach((key, value) -> {
            try {
                map.put(key, value.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        tempUsersSettings.put(id, map);
    }

    public Map<String, Currency> getCurrencyForTempUser(int id) {
        return tempUsersSettings.get(id);
    }

    public void saveSettings(int id) {
        addOrUpdateUser(id, tempUsersSettings.remove(id));
    }

    public void cancelSettings(int id) {
        tempUsersSettings.remove(id);
    }

    public InlineKeyboardMarkup updateAndGetKeyboardButtons(int id) {
        inlineKeyboardMarkup.getKeyboard().forEach(inlineKeyboardButtons -> inlineKeyboardButtons
                .forEach(inlineKeyboardButton -> {
                    if (!inlineKeyboardButton.getCallbackData().equals("Done") && !inlineKeyboardButton.getCallbackData().equals("Cancel"))
                        inlineKeyboardButton.setText(String.format("%s %s %s",
                                BotUtils.getFlagUnicode(inlineKeyboardButton.getCallbackData()),
                                inlineKeyboardButton.getCallbackData(),
                                getCurrencyForTempUser(id).get(inlineKeyboardButton.getCallbackData()).isSelected() ? "✅" : "❌"));
                }));
        return inlineKeyboardMarkup;
    }

    @Scheduled(cron = "${bot.cron}")
    public void refreshCurrencyRate() {
        currencyMap = listToMap(BotUtils.getCurrencyCursFromSite().orElseThrow());
    }

}
