package com.exchange;

import com.exchange.config.BotData;
import com.exchange.model.UserSettings;
import com.exchange.model.UserValue;
import com.exchange.service.UserSettingsService;
import com.exchange.utils.BotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Bot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LogManager.getLogger(Bot.class);
    private final BotData botData;
    private final UserSettingsComponent userSettingsComponent;
    private final UserSettingsService userSettingsService;
    private final CurrencyCursComponent currencyCursComponent;

    public Bot(BotData botData, UserSettingsComponent userSettingsComponent, UserSettingsService userSettingsService, CurrencyCursComponent currencyCursComponent) {
        this.botData = botData;
        this.userSettingsComponent = userSettingsComponent;
        this.userSettingsService = userSettingsService;
        this.currencyCursComponent = currencyCursComponent;
    }

    @Override
    public String getBotUsername() {
        return botData.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return botData.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        LOGGER.info(update.toString());
        if (update.hasMessage()) {
            var message = update.getMessage();
            if (message != null && message.hasText()) {
                var userId = message.getFrom().getId();
                switch (message.getText()) {
                    case "/start" -> {
                        userSettingsService.save(
                                new UserSettings().setUserId(userId)
                        );
                        sendMsg(message, "You are welcome!!!");
                    }
                    case "/showrates" -> {
                        Map<String, Boolean> objectMap = userSettingsService.findByUserId(userId).getCurrencyCode();
                        var collect = currencyCursComponent.getCurrencyMap().values().stream()
                                .filter(currency -> objectMap.get(currency.getCharCode()))
                                .map(currency -> MessageFormat.format("{0} *{1}* {2}\nКурс: *{3}* \u20BD\n",
                                        BotUtils.getFlagUnicode(currency.getCharCode()),
                                        currency.getNominal(),
                                        currency.getName(),
                                        currency.getValue()))
                                .collect(Collectors.joining());
                        if (!collect.equals("")) {
                            sendMsg(message, MessageFormat.format("Курс валют на *{0}*:\n{1}",
                                    currencyCursComponent.getCurrencyDate(),
                                    collect));
                        } else {
                            sendMsg(message, """
                                    Вы не выбрали ни одной валюты.
                                    Используйте */settings*, чтобы выбрать.
                                    """);
                        }
                    }
                    case "/showsettings" -> {
                        Map<String, Boolean> objectMap = userSettingsService.findByUserId(userId).getCurrencyCode();
                        var collect = currencyCursComponent.getCurrencyMap().values().stream()
                                .filter(currency -> objectMap.get(currency.getCharCode()))
                                .map(currency -> MessageFormat.format("{0} {1}\n",
                                        BotUtils.getFlagUnicode(currency.getCharCode()),
                                        currency.getCharCode()))
                                .collect(Collectors.joining());
                        var result = "";
                        if (!collect.equals("")) {
                            result += MessageFormat.format("Выбранные валюты для отслеживания курса:\n{0}", collect);
                        } else {
                            result += """
                                    Вы не выбрали ни одной валюты.
                                    Используйте */settings*, чтобы выбрать.
                                    """;
                        }
                        Map<String, String> currencyValue = userSettingsService.findByUserId(userId).getCurrencyValue();
                        collect = currencyValue.entrySet().stream()
                                .map(currency -> MessageFormat.format("{0} {1} ({2}%)\n",
                                        BotUtils.getFlagUnicode(currency.getKey()),
                                        currency.getKey(),
                                        currency.getValue()))
                                .collect(Collectors.joining());
                        if ("".equals(collect)) {
                            result += """
                                    Вы не выбрали ни одной валюты для отслеживания отклонения.
                                    "Используйте */settings2*, чтобы выбрать.
                                    """;
                        } else {
                            result += MessageFormat.format("Выбранные валюты для отслеживания:\n{0}", collect);
                        }
                        sendMsg(message, result);
                    }
                    case "/help" -> sendMsg(message, """
                            Для того, чтобы просмотреть стоимость выбраных валют выберите */showrates*
                            Для того, чтобы выбрать валюты для отслеживания курса выберите */settings*
                            Для настройки отслеживания отклонений курса валют выберите */settings2*
                            Для того, чтобы посмотреть все выбранные вами настройки выберите */showsettings*
                            """);
                    case "/settings" -> {
                            userSettingsComponent.addOrUpdateUserSettings(userSettingsService.findByUserId(userId));
                            sendMsgWithButtons(message, "Выберите валюты", userSettingsComponent.updateAndGetKeyboardButtons(userId));
                        }
                    case "/settings2" -> sendMsgWithButtons(message, "Выберите валюту для отслеживания", userSettingsComponent.getInlineKeyboardButtons());
                    default -> sendMsg(message, "Не понимаю \uD83D\uDE14");
                }
            }
        } else if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();
            var callData = callbackQuery.getData();
            var messageId = callbackQuery.getMessage().getMessageId();
            var chatId = callbackQuery.getMessage().getChatId();
            var userId = callbackQuery.getFrom().getId();
            EditMessageText editMessageText;
            if ("Done".equals(callData)) {
                userSettingsService.updateUserSettings(new UserSettings()
                        .setUserId(userId)
                        .setCurrencyCode(userSettingsComponent.removeUserSettings(userId))
                );
                editMessageText = new EditMessageText()
                        .setChatId(chatId)
                        .setMessageId(messageId)
                        .setText(MessageFormat.format("Выбраны следующие валюты:\n{0}", getUserSettingsAsString(userId)));
            } else if ("Cancel".equals(callData)) {
                userSettingsComponent.removeUserSettings(userId);
                editMessageText = new EditMessageText()
                        .setChatId(chatId)
                        .setMessageId(messageId)
                        .setText("Выбранные валюты не изменились");
            } else if ("Cancel2".equals(callData)) {
                userSettingsComponent.removeUserValues(userId);
                editMessageText = new EditMessageText()
                        .setChatId(chatId)
                        .setMessageId(messageId)
                        .setText("Отслеживаемые валюты не изменились");
            } else if (currencyCursComponent.getCurrencyMap().containsKey(callData)) {
                userSettingsComponent.switchUserSettingsValue(userId, callData);
                editMessageText = new EditMessageText()
                        .setChatId(chatId)
                        .setMessageId(messageId)
                        .setReplyMarkup(userSettingsComponent.updateAndGetKeyboardButtons(userId))
                        .setText("Выберите валюты");
            } else {
                var textMessage = callbackQuery.getMessage().getText();
                switch (callData) {
                    case "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" -> {
                        String oldNumber = textMessage.substring(textMessage.lastIndexOf(" ") + 1, textMessage.lastIndexOf("%"));
                        String newNumber;
                        if (oldNumber.equals("0")) {
                            newNumber = callData;
                        } else {
                            newNumber = oldNumber + callData;
                        }
                        if (Integer.parseInt(newNumber) <= 500)
                            textMessage = textMessage.replace(oldNumber, newNumber);
                        editMessageText = new EditMessageText()
                                .setChatId(chatId)
                                .setMessageId(messageId)
                                .setReplyMarkup(userSettingsComponent.getInlineKeyboardNumbers())
                                .setText(textMessage);
                    }
                    case "c" -> {
                        String oldNumber = textMessage.substring(textMessage.lastIndexOf(" ") + 1, textMessage.lastIndexOf("%"));
                        String newNumber;
                        if (oldNumber.length() == 1) {
                            newNumber = "0";
                        } else {
                            newNumber = oldNumber.substring(0, oldNumber.length() - 1);
                        }
                        textMessage = textMessage.replace(oldNumber, newNumber);
                        editMessageText = new EditMessageText()
                                .setChatId(chatId)
                                .setMessageId(messageId)
                                .setReplyMarkup(userSettingsComponent.getInlineKeyboardNumbers())
                                .setText(textMessage);
                    }
                    case "ok" -> {
                        String oldNumber = textMessage.substring(textMessage.lastIndexOf(" ") + 1, textMessage.lastIndexOf("%"));
                        UserValue userValue = userSettingsComponent.addOrUpdateUserValues(userId, oldNumber);
                        Map<String, String> currencyValue = userSettingsService.findByUserId(userId).getCurrencyValue();
                        if (oldNumber.equals("0")) {
                            if (currencyValue.remove(userValue.getCharCode()) != null) {
                                userSettingsService.updateUserValues(new UserSettings()
                                        .setUserId(userId)
                                        .setCurrencyValue(currencyValue)
                                );
                                editMessageText = new EditMessageText()
                                        .setChatId(chatId)
                                        .setMessageId(messageId)
                                        .setText(MessageFormat.format("Валюта {0} {1} больше не отслеживается.", BotUtils.getFlagUnicode(userValue.getCharCode()), userValue.getCharCode()));
                            } else {
                                editMessageText = new EditMessageText()
                                        .setChatId(chatId)
                                        .setMessageId(messageId)
                                        .setText("Отслеживаемые валюты не изменились");
                            }
                        } else {
                            currencyValue.put(userValue.getCharCode(), userValue.getValue());
                            userSettingsService.updateUserValues(new UserSettings()
                                    .setUserId(userId)
                                    .setCurrencyValue(currencyValue)
                            );
                            editMessageText = new EditMessageText()
                                    .setChatId(chatId)
                                    .setMessageId(messageId)
                                    .setText(MessageFormat.format("Для валюты {0} {1} установлено значение {2}%", BotUtils.getFlagUnicode(userValue.getCharCode()), userValue.getCharCode(), userValue.getValue()));
                        }
                    }
                    default -> {
                        String charCode = callData.replace("2", "");
                        UserSettings userSettings = userSettingsService.findByUserId(userId);
                        String percent = "0";
                        if (userSettings.getCurrencyValue().containsKey(charCode)) {
                            percent = userSettings.getCurrencyValue().get(charCode);
                        }
                        userSettingsComponent.addUserValues(userId, charCode);
                        editMessageText = new EditMessageText()
                                .setChatId(chatId)
                                .setMessageId(messageId)
                                .setReplyMarkup(userSettingsComponent.getInlineKeyboardNumbers())
                                .setText(MessageFormat.format("Введите процент, на который должна измениться валюта {0} {1}: {2}%", BotUtils.getFlagUnicode(charCode), charCode, percent));
                    }
                }
            }
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private void sendMsg(Message message, String text) {
        var sendMessage = new SendMessage()
                .enableMarkdown(true)
                .setChatId(message.getChatId())
                .setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void sendMsgWithButtons(Message message, String text, ReplyKeyboard inlineKeyboardMarkup) {
        var sendMessage = new SendMessage()
                .enableMarkdown(true)
                .setChatId(message.getChatId())
                .setText(text)
                .setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Scheduled(cron = "${bot.cron.notify}")
    private synchronized void sendMsgWithNotification() {
        List<UserSettings> userSettings = userSettingsService.findAll();
        userSettings.forEach(userSetting -> {
            var stringValCurs = currencyCursComponent.getCurrencyMap().values().stream()
                    .filter(currency -> userSetting.getCurrencyCode().get(currency.getCharCode()))
                    .map(currency -> MessageFormat.format("{0} *{1}* {2}\nКурс: *{3}* \u20BD\n", BotUtils.getFlagUnicode(currency.getCharCode()), currency.getNominal(), currency.getName(), currency.getValue()))
                    .collect(Collectors.joining());
            if (!"".equals(stringValCurs)) {
                stringValCurs = MessageFormat.format("Курсы валют обновились!\nКурсы валют на *{0}*:\n{1}", currencyCursComponent.getCurrencyDate(), stringValCurs);
                var sendMessage = new SendMessage()
                        .enableMarkdown(true)
                        .enableNotification()
                        .setChatId((long) userSetting.getUserId())
                        .setText(stringValCurs);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        });
    }

    private String getUserSettingsAsString(int id) {
        return userSettingsService.findByUserId(id).getCurrencyCode().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(stringBooleanEntry -> MessageFormat.format("{0} {1}\n", BotUtils.getFlagUnicode(stringBooleanEntry.getKey()), stringBooleanEntry.getKey()))
                .collect(Collectors.joining());
    }

}
