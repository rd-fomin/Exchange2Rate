package com.exchange;

import com.exchange.model.UserSettings;
import com.exchange.model.UserValue;
import com.exchange.utils.BotUtils;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserSettingsComponent {
    private static final Logger LOGGER = LogManager.getLogger(UserSettingsComponent.class);
    private final InlineKeyboardMarkup inlineKeyboardForCurrencyChoose;
    private final InlineKeyboardMarkup inlineKeyboardForCurrencyValues;
    private final InlineKeyboardMarkup inlineKeyboardNumbers;
    private final Map<Integer, Map<String, Boolean>> tempUsersSettings = new HashMap<>();
    private final Map<Integer, UserValue> tempUsersValues = new HashMap<>();

    public UserSettingsComponent(CurrencyCursComponent currencyCursComponent) {
        var currencyList = new ArrayList<>(currencyCursComponent.getCurrencyMap().values());
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
        keyboardButtons = Lists.partition(currencyList, 2).stream()
                .map(currencies -> currencies.stream()
                        .map(currency -> new InlineKeyboardButton()
                                .setCallbackData(currency.getCharCode() + "2")
                                .setText(String.format("%s %s",
                                        BotUtils.getFlagUnicode(currency.getCharCode()),
                                        currency.getCharCode())))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setText("Cancel").setCallbackData("Cancel2")
                )
        );
        inlineKeyboardForCurrencyValues = new InlineKeyboardMarkup();
        inlineKeyboardForCurrencyValues.setKeyboard(keyboardButtons);
        keyboardButtons = new ArrayList<>();
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setCallbackData("1").setText("1️⃣"),
                        new InlineKeyboardButton().setCallbackData("2").setText("2️⃣"),
                        new InlineKeyboardButton().setCallbackData("3").setText("3️⃣")
                )
        );
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setCallbackData("4").setText("4️⃣"),
                        new InlineKeyboardButton().setCallbackData("5").setText("5️⃣"),
                        new InlineKeyboardButton().setCallbackData("6").setText("6️⃣")
                )
        );
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setCallbackData("7").setText("7️⃣"),
                        new InlineKeyboardButton().setCallbackData("8").setText("8️⃣"),
                        new InlineKeyboardButton().setCallbackData("9").setText("9️⃣")
                )
        );
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setCallbackData("c").setText("\uD83D\uDD19"),
                        new InlineKeyboardButton().setCallbackData("0").setText("0️⃣"),
                        new InlineKeyboardButton().setCallbackData("ok").setText("\uD83C\uDD97")
                )
        );
        keyboardButtons.add(
                List.of(
                        new InlineKeyboardButton().setCallbackData("Cancel2").setText("Cancel")
                )
        );
        inlineKeyboardNumbers = new InlineKeyboardMarkup();
        inlineKeyboardNumbers.setKeyboard(keyboardButtons);
    }

    public void addOrUpdateUserSettings(UserSettings userSettings) {
        tempUsersSettings.put(userSettings.getUserId(), userSettings.getCurrencyCode());
    }

    public void switchUserSettingsValue(int id, String charCode) {
        tempUsersSettings.get(id).put(charCode, !tempUsersSettings.get(id).get(charCode));
    }

    public Map<String, Boolean> removeUserSettings(int id) {
        return tempUsersSettings.remove(id);
    }

    public void addUserValues(int id, String charCode) {
        tempUsersValues.put(id, new UserValue().setCharCode(charCode));
    }

    public UserValue addOrUpdateUserValues(int id, String value) {
        return tempUsersValues.remove(id).setValue(value);
    }

    public UserValue removeUserValues(int id) {
        return tempUsersValues.remove(id);
    }

    public InlineKeyboardMarkup updateAndGetKeyboardButtons(int id) {
        inlineKeyboardForCurrencyChoose.getKeyboard().forEach(inlineKeyboardButtons -> inlineKeyboardButtons
                .forEach(inlineKeyboardButton -> {
                    if (!inlineKeyboardButton.getCallbackData().equals("Done") && !inlineKeyboardButton.getCallbackData().equals("Cancel"))
                        inlineKeyboardButton.setText(String.format("%s %s %s",
                                BotUtils.getFlagUnicode(inlineKeyboardButton.getCallbackData()),
                                inlineKeyboardButton.getCallbackData(),
                                tempUsersSettings.get(id).get(inlineKeyboardButton.getCallbackData()) ? "✅" : "❌"));
                }));
        return inlineKeyboardForCurrencyChoose;
    }

    public InlineKeyboardMarkup getInlineKeyboardButtons() {
        return inlineKeyboardForCurrencyValues;
    }

    public InlineKeyboardMarkup getInlineKeyboardNumbers() {
        return inlineKeyboardNumbers;
    }

}
