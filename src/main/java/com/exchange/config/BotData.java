package com.exchange.config;

public class BotData {
    private final String botUserName;
    private final String botToken;

    public BotData(String botUserName, String botToken) {
        this.botUserName = botUserName;
        this.botToken = botToken;
    }

    public String getBotUserName() {
        return botUserName;
    }

    public String getBotToken() {
        return botToken;
    }
}
