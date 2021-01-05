package com.exchange;

import com.exchange.config.BotData;
import com.exchange.model.UserSettings;
import com.exchange.service.UserSettingsService;
import com.exchange.utils.BotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Bot extends TelegramLongPollingBot {
    private static final Logger log = LogManager.getLogger(Bot.class);
    private final BotData botData;
    private final UserSettingsComponent userSettingsComponent;
    private final UserSettingsService userSettingsService;

    public Bot(BotData botData, UserSettingsComponent userSettingsComponent, UserSettingsService userSettingsService) {
        this.botData = botData;
        this.userSettingsComponent = userSettingsComponent;
        this.userSettingsService = userSettingsService;
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
        log.info(update.toString());
        if (update.hasMessage()) {
            var message = update.getMessage();
            if (message != null && message.hasText()) {
                switch (message.getText()) {
                    case "/start" -> {
                        userSettingsService.save(new UserSettings()
                                .setUserId(message.getFrom().getId())
                        );
                        sendMsg(message, "You are welcome!!!");
                    }
                    case "/show" -> {
                        Map<String, Boolean> objectMap = userSettingsService.findByUserId(message.getFrom().getId()).getCurrencyCode();
                        var stringValCurs = userSettingsComponent.currencyMap.values().stream()
                                .filter(currency -> objectMap.get(currency.getCharCode()))
                                .map(currency -> String.format("%s *%s* %s\nСтоимость: *%s* \u20BD\n", BotUtils.getFlagUnicode(currency.getCharCode()), currency.getNominal(), currency.getName(), currency.getValue()))
                                .collect(Collectors.joining());
                        if (!stringValCurs.equals("")) {
                            sendMsg(message, "Курсы валют на сегодня:\n" + stringValCurs);
                        } else {
                            sendMsg(message, "Вы не выбрали ни одной валюты.\nИспользуйте */settings*, чтобы выбрать.");
                        }
                    }
                    case "/help" -> sendMsg(message, "Для настройки валют выберите */settings*\nДля того, чтобы просмотреть стоимость выбраных валют выберите */show*");
                    case "/settings" -> {
                        userSettingsComponent.addOrUpdateTempUser(userSettingsService.findByUserId(message.getFrom().getId()));
                        sendMsgWithButtons(message, "Выберите валюты", userSettingsComponent.updateAndGetKeyboardButtons(message.getFrom().getId()));
                    }
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
            switch (callData) {
                case "Done" -> {
                    userSettingsService.update(new UserSettings()
                            .setUserId(userId)
                            .setCurrencyCode(userSettingsComponent.removeTempUserSettings(userId))
                    );
                    editMessageText = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId(messageId)
                            .setText("Выбраны следующие валюты:\n" + getUserSettingsAsString(userId));
                }
                case "Cancel" -> {
                    userSettingsComponent.removeTempUserSettings(userId);
                    editMessageText = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId(messageId)
                            .setText("Выбранные валюты не изменились");
                }
                default -> {
                    userSettingsComponent.switchValueForTempUser(userId, callData);
                    editMessageText = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId(messageId)
                            .setReplyMarkup(userSettingsComponent.updateAndGetKeyboardButtons(userId))
                            .setText("Выберите валюты");
                }
            }
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
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
            log.error(e.getMessage());
        }
    }

    private void sendMsgWithNotification(Message message, String text) {
        var sendMessage = new SendMessage()
                .enableMarkdown(true)
                .enableNotification()
                .setChatId(message.getChatId())
                .setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
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
            log.error(e.getMessage());
        }
    }

    private String getUserSettingsAsString(int id) {
        return userSettingsService.findByUserId(id).getCurrencyCode().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(stringBooleanEntry -> String.format("%s %s\n", BotUtils.getFlagUnicode(stringBooleanEntry.getKey()), stringBooleanEntry.getKey()))
                .collect(Collectors.joining());
    }

}
