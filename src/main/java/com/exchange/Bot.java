package com.exchange;

import com.exchange.model.Valute;
import com.exchange.utils.BotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.stream.Collectors;

@Component
@PropertySource(value = "classpath:/application.properties")
public class Bot extends TelegramLongPollingBot {
    private static final Logger log = LogManager.getLogger(Bot.class);
    @Value("${bot.username}")
    private String botUserName;
    @Value("${bot.token}")
    private String botToken;

    public Bot() {}

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info(update.toString());
        if (update.hasMessage()) {
            var message = update.getMessage();
            if (message != null && message.hasText()) {
                switch (message.getText()) {
                    case "/start" -> {
                        BotUtils.addOrUpdateUser(message.getFrom().getId());
                        sendMsg(message, "You are welcome!!!");
                    }
                    case "/show" -> {
                        var stringValCurs = BotUtils.getValutesForUser(message.getFrom().getId())
                                .values().stream()
                                .filter(Valute::isSelected)
                                .map(valute -> String.format("%s *%s* %s\nСтоимость: *%s* \u20BD\n", BotUtils.getFlagUnicode(valute.getCharCode()), valute.getNominal(), valute.getName(), valute.getValue()))
                                .collect(Collectors.joining());
                        if (!stringValCurs.equals(""))
                            sendMsg(message, "Курсы валют на сегодня:\n" + stringValCurs);
                        else
                            sendMsg(message, "Вы не выбрали ни одной валюты.\nИспользуйте /settings, чтобы выбрать.");
                    }
                    case "/help" -> sendMsg(message, "Для настройки валют выберите /settings\nДля того, чтобы просмотреть стоимость выбраных валют выберите /show");
                    case "/settings" -> {
                        BotUtils.addOrUpdateTempUser(message.getFrom().getId());
                        sendMsgWithButtons(message, "Выберите валюты", BotUtils.updateAndGetKeyboardButtons(message.getFrom().getId()));
                    }
                    default -> sendMsg(message, "Не понимаю \uD83D\uDE14");
                }
            }
        } else if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();
            var callData = callbackQuery.getData();
            long messageId = callbackQuery.getMessage().getMessageId();
            long chatId = callbackQuery.getMessage().getChatId();
            EditMessageText editMessageText;
            switch (callData) {
                case "Done" -> {
                    BotUtils.saveSettings(callbackQuery.getFrom().getId());
                    editMessageText = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId((int) messageId)
                            .setText("Выбраны следующие валюты:\n" + BotUtils.getValuteMapAsString(callbackQuery.getFrom().getId()));
                }
                case "Cancel" -> {
                    BotUtils.cancelSettings(callbackQuery.getFrom().getId());
                    editMessageText = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId((int) messageId)
                            .setText("Выбранные валюты не изменились");
                }
                default -> {
                    BotUtils.getValutesForTempUser(callbackQuery.getFrom().getId()).get(callData).changeSelect();
                    editMessageText = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId((int) messageId)
                            .setReplyMarkup(BotUtils.updateAndGetKeyboardButtons(callbackQuery.getFrom().getId()))
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

}
